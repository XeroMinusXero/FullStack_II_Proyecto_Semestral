package com.thequietchapter.model;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutDominioTests {
    private static BigDecimal dinero(int n){return BigDecimal.valueOf(n);}
    private static ProcesoCompra listo() {
        ProcesoCompra p=new ProcesoCompra(7L,12L);
        p.seleccionarDireccion(new DireccionEnvio("Calle Uno","123","Puerto Montt","Los Lagos"));
        p.seleccionarMetodoEnvio(new MetodoEnvio("Despacho",dinero(2000),3,true,List.of("Los Lagos")));
        p.seleccionarMedioPago(new MedioPago("Proveedor futuro",true));
        p.prepararResumen(List.of(new LineaCompra(1L,"Libro",2,dinero(10000))),BigDecimal.ZERO,BigDecimal.ZERO);
        return p;
    }
    @Test void calculaSubtotalesEnvioYTotalSinInventarIva() {
        ProcesoCompra p=listo();
        assertEquals(EstadoCheckout.LISTO_PARA_PAGO,p.getEstado());
        assertEquals(new BigDecimal("20000.00"),p.getResumen().calcularSubtotal());
        assertEquals(new BigDecimal("22000.00"),p.getResumen().calcularTotal());
        assertTrue(p.puedePagar());
    }
    @Test void bloqueaProcesoSinDireccionOConRegionIncompatible() {
        ProcesoCompra p=new ProcesoCompra(7L,12L);
        assertThrows(IllegalStateException.class,()->p.prepararResumen(List.of(new LineaCompra(1L,"Libro",1,dinero(100))),BigDecimal.ZERO,BigDecimal.ZERO));
        p.seleccionarDireccion(new DireccionEnvio("Calle Uno","123","Santiago","Metropolitana"));
        MetodoEnvio soloLagos=new MetodoEnvio("Lagos",dinero(1000),2,true,List.of("Los Lagos"));
        assertThrows(IllegalArgumentException.class,()->p.seleccionarMetodoEnvio(soloLagos));
    }
    @Test void registraSoloPendienteYNuncaFinalizaSinAprobacionVerificable() {
        ProcesoCompra p=listo();
        p.registrarIntentoPago(new Pago(p.getResumen().calcularTotal()));
        assertEquals(EstadoPago.PENDIENTE,p.getIntentosPago().get(0).getEstado());
        assertFalse(p.puedePagar());
        assertThrows(IllegalStateException.class,()->p.finalizar("PED-100"));
        assertThrows(IllegalStateException.class,()->p.registrarIntentoPago(new Pago(dinero(22000))));
    }
    @Test void cancelarNoGeneraPedidoNiCobro() {
        ProcesoCompra p=listo();
        p.registrarIntentoPago(new Pago(p.getResumen().calcularTotal()));
        p.cancelar();
        assertEquals(EstadoCheckout.CANCELADO,p.getEstado());
        assertEquals(EstadoPago.CANCELADO,p.getIntentosPago().get(0).getEstado());
        assertNull(p.getNumeroPedidoConfirmado());
    }
    @Test void direccionesMalFormadasBloqueadas() {
        assertThrows(IllegalArgumentException.class,()->new DireccionEnvio("","1","A","B"));
    }
}
