package com.thequietchapter.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PromocionesDominioTests {
    private final LocalDateTime ahora = LocalDateTime.now();

    @Test void cuponValidaVigenciaMinimoYCalculaDescuento() {
        var c = new Cupon("verano10", TipoDescuento.PORCENTAJE, new BigDecimal("10"), new BigDecimal("20000"), ahora.minusDays(1), ahora.plusDays(5));
        assertEquals("VERANO10", c.getCodigo());
        assertTrue(c.motivoRechazo(new BigDecimal("15000"), ahora).orElse("").contains("minimo"));
        assertTrue(c.esCanjeable(new BigDecimal("25000"), ahora));
        assertEquals(new BigDecimal("2500.00"), c.calcularDescuento(new BigDecimal("25000")));
        c.cambiarEstado(false);
        assertFalse(c.esCanjeable(new BigDecimal("25000"), ahora));
        assertThrows(IllegalArgumentException.class, () -> new Cupon("X1Y", TipoDescuento.PORCENTAJE, new BigDecimal("150"), BigDecimal.ZERO, ahora, ahora.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new Cupon("X1Y", TipoDescuento.MONTO_FIJO, new BigDecimal("1000"), BigDecimal.ZERO, ahora.plusDays(2), ahora));
    }

    @Test void promocionRequiereAlcanceYAplicaPorCategoria() {
        var p = new Promocion("Cyber", TipoDescuento.MONTO_FIJO, new BigDecimal("2000"), ahora.minusDays(1), ahora.plusDays(1));
        assertThrows(IllegalStateException.class, () -> p.cambiarEstado(true));
        p.reemplazarAlcances(List.of(new AlcancePromocion(TipoAlcance.CATEGORIA, 7L)));
        p.cambiarEstado(true);
        assertTrue(p.aplicaA(1L, 7L, List.of(), 3L, ahora));
        assertFalse(p.aplicaA(1L, 8L, List.of(), 3L, ahora));
        assertEquals(new BigDecimal("1500.00"), p.calcularDescuento(new BigDecimal("1500")));
    }

    @Test void recomendadorPrefiereMismoAutorYExcluyeAgotados() {
        var base = new LibroRecomendable(1L, Set.of(10L), 1L, Set.of(5L), true, false, 0);
        var mismoAutor = new LibroRecomendable(2L, Set.of(10L), 2L, Set.of(), true, false, 0);
        var mismaCategoria = new LibroRecomendable(3L, Set.of(11L), 1L, Set.of(), true, false, 9);
        var agotado = new LibroRecomendable(4L, Set.of(10L), 1L, Set.of(5L), false, false, 0);
        var r = new RecomendadorLibros();
        assertEquals(List.of(2L, 3L), r.recomendarRelacionados(1L, List.of(base, mismoAutor, mismaCategoria, agotado), 5));
        assertEquals(List.of(), r.recomendarComplementarios(Set.of(), List.of(base), 5));
        assertEquals(Long.valueOf(3), r.recomendarGenerales(List.of(base, mismoAutor, mismaCategoria), 1).get(0));
    }
}
