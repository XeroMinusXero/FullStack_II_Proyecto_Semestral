package com.thequietchapter.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoDominioTests {
    private Libro crearLibro(String isbn) {
        Libro l = new Libro(isbn, "Libro prueba", new BigDecimal("1000"), new Inventario(2));
        l.asignarCategoria(new Categoria("Narrativa"));
        l.asignarEditorial(new Editorial("Editorial prueba"));
        l.reemplazarAutores(List.of(new Autor("Autor prueba", "")));
        return l;
    }
    @Test void elIsbnDebeSerUnico() {
        var c = new Catalogo();
        c.registrarLibro(crearLibro("ISBN-1"));
        assertThrows(IllegalStateException.class, () -> c.registrarLibro(crearLibro("ISBN-1")));
    }
    @Test void unLibroTieneInventarioYNoStockDuplicado() {
        var libro = crearLibro("ISBN-2");
        assertEquals(2, libro.getInventario().consultarStock());
        var movimiento = libro.getInventario().registrarIngreso(3, 1L);
        assertEquals(5, libro.getInventario().consultarStock());
        assertEquals(3, movimiento.calcularVariacion());
        assertThrows(IllegalStateException.class, () -> libro.getInventario().descontarStock(99));
    }
    @Test void filtroPorEstadoYPaginacion() {
        var catalogo = new Catalogo();
        var activo = crearLibro("ISBN-3");
        var inactivo = crearLibro("ISBN-4");
        inactivo.cambiarEstado(false);
        catalogo.registrarLibro(activo); catalogo.registrarLibro(inactivo);
        assertEquals(1, catalogo.listarLibros(false).size());
        assertEquals(2, catalogo.listarLibros(true).size());
        assertEquals(1, catalogo.paginar(catalogo.listarLibros(true), 0, 1).size());
    }
}
