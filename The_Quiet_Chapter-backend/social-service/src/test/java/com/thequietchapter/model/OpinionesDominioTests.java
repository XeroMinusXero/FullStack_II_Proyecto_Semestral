package com.thequietchapter.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OpinionesDominioTests {
    @Test void favoritosSinDuplicados() {
        var l = new ListaFavoritos(1L);
        assertTrue(l.agregarLibro(10L));
        assertFalse(l.agregarLibro(10L));
        assertTrue(l.quitarLibro(10L));
        assertFalse(l.quitarLibro(10L));
        assertTrue(l.estaVacia());
    }
    @Test void reglasDeResena() {
        var r = new Resena(1L, 10L, "Un libro excelente de principio a fin");
        assertFalse(r.marcarUtil(1L));            // el autor no puede
        assertTrue(r.marcarUtil(2L));
        assertFalse(r.marcarUtil(2L));            // no dos veces
        assertEquals(1, r.contarVotosUtiles());
        assertFalse(r.editarTexto(2L, "Texto de otra persona"));
        assertTrue(r.reportar(3L, "Spoiler"));
        assertFalse(r.reportar(3L, "Otra vez"));
        assertEquals(EstadoResena.REPORTADA, r.getEstado());
        assertTrue(r.resolverReporte(99L, EstadoReporte.ELIMINADO));
        assertEquals(EstadoResena.RETIRADA, r.getEstado());
        assertFalse(r.estaVisible());
        assertThrows(IllegalArgumentException.class, () -> new Resena(1L, 10L, "corto"));
    }
    @Test void registroExigeCompraYUnaResenaVigente() {
        var registro = new RegistroOpiniones(new ArrayList<>(), new ArrayList<>());
        assertFalse(registro.publicarResena(new Resena(1L, 10L, "Contenido suficiente aqui"), false));
        assertTrue(registro.publicarResena(new Resena(1L, 10L, "Contenido suficiente aqui"), true));
        assertFalse(registro.publicarResena(new Resena(1L, 10L, "Segunda resena del mismo"), true));
        assertTrue(registro.registrarCalificacion(new Calificacion(1L, 10L, 5), true));
        assertTrue(registro.registrarCalificacion(new Calificacion(2L, 10L, 4), true));
        assertEquals(new BigDecimal("4.5"), registro.calcularPromedio(10L));
        assertNull(registro.calcularPromedio(99L));
        assertEquals(1, registro.filtrarPorCalificacion(10L, 5).size());
        assertEquals(List.of(), registro.filtrarPorCalificacion(10L, 1));
    }
}
