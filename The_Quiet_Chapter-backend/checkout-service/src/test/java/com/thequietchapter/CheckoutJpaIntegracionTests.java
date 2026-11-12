package com.thequietchapter;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class CheckoutJpaIntegracionTests {
    @Autowired ProcesoCompraRepository procesos;
    @Autowired MetodoEnvioRepository envios;
    @Autowired MedioPagoRepository medios;
    @Test void conservaProcesoDireccionResumenYLineas() {
        MetodoEnvio envio=envios.save(new MetodoEnvio("Despacho test",new BigDecimal("3900"),4,true,List.of("Los Lagos")));
        MedioPago medio=medios.save(new MedioPago("Pendiente de procesador",true));
        ProcesoCompra p=new ProcesoCompra(9L,55L);
        p.seleccionarDireccion(new DireccionEnvio("Los Alerces","5","Puerto Montt","Los Lagos"));
        p.seleccionarMetodoEnvio(envio);
        p.seleccionarMedioPago(medio);
        p.prepararResumen(List.of(new LineaCompra(10L,"Libro uno",2,new BigDecimal("10000"))),BigDecimal.ZERO,BigDecimal.ZERO);
        Long id=procesos.saveAndFlush(p).getId();
        assertNotNull(id);
        ProcesoCompra recuperado=procesos.findByIdAndClienteId(id,9L).orElseThrow();
        assertEquals(EstadoCheckout.LISTO_PARA_PAGO,recuperado.getEstado());
        assertEquals("Puerto Montt",recuperado.getDireccionEnvio().getComuna());
        assertEquals(1,recuperado.getResumen().getLineas().size());
        assertEquals(new BigDecimal("23900.00"),recuperado.getResumen().calcularTotal());
        assertEquals(new BigDecimal("3815.97"),recuperado.getResumen().calcularIvaIncluido());
        assertTrue(envios.count()>=4); // 3 opciones iniciales + la de la prueba
        recuperado.registrarIntentoPago(new Pago(recuperado.getResumen().calcularTotal()));
        procesos.saveAndFlush(recuperado);
        assertEquals(1,procesos.findById(id).orElseThrow().getIntentosPago().size());
        assertEquals(EstadoPago.PENDIENTE,recuperado.getIntentosPago().get(0).getEstado());
    }
}
