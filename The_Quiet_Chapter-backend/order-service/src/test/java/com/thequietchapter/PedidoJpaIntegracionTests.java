package com.thequietchapter;
import com.thequietchapter.dto.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.PedidoRepository;
import com.thequietchapter.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class PedidoJpaIntegracionTests {
    @Autowired PedidoService service;
    @Autowired PedidoRepository repository;
    @Test void crearPersistirListarYAvanzar(){
        var sol=new CrearPedidoSolicitud(9001L,
            List.of(new CrearPedidoSolicitud.Detalle(1L,"Cien años de soledad",5,new BigDecimal("18900"))),
            new CrearPedidoSolicitud.Entrega("Calle Uno","123","Puerto Montt","Los Lagos","Despacho",3),
            new CrearPedidoSolicitud.Importes(new BigDecimal("94500"),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO),
            "SIMULADO","REF-JPA-ORDER-0001");
        PedidoRespuesta creado=service.crear(sol);
        assertNotNull(creado.id());assertEquals("TQC-"+String.format("%08d",creado.id()),creado.numeroPedido());
        assertEquals(1,creado.detalles().size());assertEquals(new BigDecimal("94500.00"),creado.importes().total());
        assertEquals(creado.id(),service.crear(sol).id());
        assertEquals(1,service.misPedidos(9001L,0,10).totalElementos());
        var avance=service.cambiarEstado(creado.id(),EstadoPedido.EN_PREPARACION);
        assertEquals(EstadoPedido.EN_PREPARACION,avance.estado());
        assertEquals(EstadoPedido.EN_PREPARACION,repository.findById(creado.id()).orElseThrow().getEstado());
        assertTrue(service.libroTienePedidos(1L));
        assertFalse(service.libroTienePedidos(999L));
        assertThrows(IllegalStateException.class,()->service.cambiarEstado(creado.id(),EstadoPedido.ENTREGADO));
        assertEquals(1,service.entre(null,null).size());
    }
}
