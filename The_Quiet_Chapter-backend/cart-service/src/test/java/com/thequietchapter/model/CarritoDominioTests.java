package com.thequietchapter.model;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class CarritoDominioTests {
    private static CondicionLibroCarrito c(long id,int stock,int precio) {
        return new CondicionLibroCarrito(id,BigDecimal.valueOf(precio),stock,true);
    }
    @Test void agregarModificarYNoSuperarStock() {
        Carrito carrito=new Carrito(null,"sesion-uno");
        assertTrue(carrito.agregarLibro(c(1,2,100)).isExitosa());
        assertFalse(carrito.actualizarCantidad(1L,4,c(1,2,100)).isExitosa());
        assertEquals(1,carrito.contarUnidades());
        assertTrue(carrito.actualizarCantidad(1L,2,c(1,2,100)).isExitosa());
        assertEquals(new BigDecimal("200.00"),carrito.calcularTotal());
        assertFalse(carrito.puedeComprar());
    }
    @Test void fusionSumaHastaMaximoDisponible() {
        Carrito cliente=new Carrito(15L,null);
        cliente.agregarLibro(c(1,10,100));
        Carrito anonimo=new Carrito(null,"sesion-dos");
        anonimo.agregarLibro(c(1,10,100));
        anonimo.actualizarCantidad(1L,4,c(1,10,100));
        ResultadoOperacionCarrito resultado=cliente.fusionarDesde(anonimo,List.of(c(1,3,100)));
        assertEquals(3,cliente.contarUnidades());
        assertTrue(resultado.tieneAvisos());
        assertEquals(TipoAvisoCarrito.CANTIDAD_AJUSTADA,resultado.getAvisos().get(0).getTipo());
    }
    @Test void cambiosDePrecioYStockSeDetectan() {
        Carrito carrito=new Carrito(2L,null);
        carrito.agregarLibro(c(1,5,100));
        ResultadoOperacionCarrito resultado=carrito.revisar(List.of(c(1,0,150)));
        assertTrue(resultado.hayBloqueos());
        assertEquals(new BigDecimal("150.00"),carrito.calcularTotal());
        assertFalse(carrito.puedeComprar());
    }
    @Test void noPermiteDescuentoNegativo() {
        Carrito carrito=new Carrito(2L,null);
        assertFalse(carrito.aplicarCuponValidado(1L,"X",new BigDecimal("-1")));
    }
}
