package com.thequietchapter.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class NotificacionesDominioTests {
    @Test void suscripcionDobleConfirmacionYCancelacion() {
        var s = new SuscripcionBoletin("Lector@Example.cl", "c1", "x1");
        assertEquals("lector@example.cl", s.getEmail());
        assertFalse(s.estaActiva());
        assertFalse(s.confirmar("otro"));
        assertTrue(s.confirmar("c1"));
        assertFalse(s.confirmar("c1"));
        assertTrue(new RegistroSuscriptores().registrarSuscripcion(Optional.of(s), s.getEmail(), "c2", "x2").isEmpty());
        assertTrue(s.cancelar("x1"));
        assertTrue(new RegistroSuscriptores().registrarSuscripcion(Optional.of(s), s.getEmail(), "c2", "x2").isPresent());
        assertThrows(IllegalArgumentException.class, () -> new SuscripcionBoletin("no-es-correo", "a", "b"));
    }
    @Test void boletinSoloSeEnviaUnaVezYSoloAActivos() {
        var b = new Boletin("Novedades", "Nuevos libros este mes");
        var activo = new SuscripcionBoletin("a@x.cl", "c", "x"); activo.confirmar("c");
        var pendiente = new SuscripcionBoletin("b@x.cl", "c2", "x2");
        var correos = new RegistroBoletines().prepararEnvio(b, List.of(activo, pendiente), s -> "http://f/cancelar?token=" + s.getTokenCancelacion());
        assertEquals(1, correos.size());
        assertTrue(b.marcarEnviado(correos.size()));
        assertFalse(b.editar("X", "Y"));
        assertThrows(IllegalStateException.class, () -> new RegistroBoletines().prepararEnvio(b, List.of(activo), s -> ""));
    }
    @Test void avisoSoloParaAgotadosYSinDuplicar() {
        var r = new RegistroAvisosStock(List.of());
        assertThrows(IllegalStateException.class, () -> r.solicitarAviso(1L, 5L, 3));
        assertTrue(r.solicitarAviso(1L, 5L, 0).isPresent());
        assertTrue(r.solicitarAviso(1L, 5L, 0).isEmpty());
        assertEquals(1, r.listarPorLibro(5L).size());
    }
}
