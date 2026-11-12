package com.thequietchapter.model;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class PedidoDominioTests {
    private static Pedido pedido(){
        var linea=new DetallePedido(1L,"Cien años de soledad",5,new BigDecimal("18900"));
        var entrega=new DatosEntregaPedido("Calle Uno","123","Puerto Montt","Los Lagos","Despacho",3);
        var importe=new ImportesPedido(new BigDecimal("94500"),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
        return new RegistroPedidos().crearPedido(9001L,List.of(linea),entrega,importe,"SIMULADO","REF-DOMINIO-01");
    }
    @Test void transicionesYSeguimiento(){
        Pedido p=pedido();assertEquals(EstadoPedido.PAGADO,p.getEstado());
        assertFalse(p.avanzarEstado(EstadoPedido.ENTREGADO));
        assertThrows(IllegalStateException.class,()->p.registrarSeguimiento("TRACK-01"));
        assertTrue(p.avanzarEstado(EstadoPedido.EN_PREPARACION));
        assertTrue(p.avanzarEstado(EstadoPedido.ENVIADO));
        p.registrarSeguimiento("TRACK-01");assertTrue(p.getEntrega().tieneSeguimiento());
        assertTrue(p.avanzarEstado(EstadoPedido.ENTREGADO));assertFalse(p.cancelar());
    }
    @Test void cancelacionYValidacionDeImportes(){
        Pedido p=pedido();assertTrue(p.puedeCancelarse());assertTrue(p.cancelar());
        assertEquals(EstadoPedido.CANCELADO,p.getEstado());assertFalse(p.avanzarEstado(EstadoPedido.ENVIADO));
        assertThrows(IllegalArgumentException.class,()->new ImportesPedido(new BigDecimal("1"),
            BigDecimal.ZERO,BigDecimal.ZERO,new BigDecimal("2")));
        assertTrue(p.contieneLibro(1L));assertFalse(p.contieneLibro(2L));
    }
    @Test void filtroFechas(){
        assertThrows(IllegalArgumentException.class,()->new CriteriosPedido(null,LocalDate.parse("2026-10-30"),LocalDate.parse("2026-10-01")));
    }
}
