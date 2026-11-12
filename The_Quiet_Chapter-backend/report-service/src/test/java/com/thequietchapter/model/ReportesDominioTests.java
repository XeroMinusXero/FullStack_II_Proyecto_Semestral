package com.thequietchapter.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReportesDominioTests {
    private final LocalDate hoy = LocalDate.of(2026, 11, 11);
    private PedidoAnalitico pedido(String n, int dia, EstadoPedido e, String total, long libro, int cant) {
        return new PedidoAnalitico(n, LocalDateTime.of(2026, 11, dia, 10, 0), 1L, "Ana", e, new BigDecimal(total),
            List.of(new LineaPedidoAnalitica(libro, "Libro " + libro, "Autor", 7L, "Novela", cant, new BigDecimal(total).divide(BigDecimal.valueOf(cant)))));
    }
    @Test void ventasExcluyenCanceladosYSerieSinHuecos() {
        var p = new PeriodoReporte(hoy.minusDays(2), hoy);
        var r = new ReporteVentas(p, List.of(pedido("A", 10, EstadoPedido.PAGADO, "10000", 1, 1),
            pedido("B", 11, EstadoPedido.CANCELADO, "5000", 2, 1), pedido("C", 1, EstadoPedido.PAGADO, "999", 3, 1)));
        assertEquals(1, r.contarPedidos());
        assertEquals(new BigDecimal("10000"), r.calcularMontoTotal());
        assertEquals(3, r.calcularEvolucion(AgrupacionTemporal.DIA).size());
        assertEquals(Long.valueOf(1), r.contarPedidosPorEstado().get("CANCELADO"));
        assertEquals(1, r.ventasPorCategoria().size());
        assertThrows(IllegalArgumentException.class, () -> new PeriodoReporte(hoy, hoy.minusDays(1)));
    }
    @Test void librosRotacionYCsv() {
        var p = new PeriodoReporte(hoy.minusDays(30), hoy);
        var vendido = new LibroAnalitico(1L, "Uno", "A", true, 4, new BigDecimal("10000"));
        var quieto = new LibroAnalitico(2L, "Dos", "B", true, 3, new BigDecimal("5000"));
        var r = new ReporteLibros(p, List.of(vendido, quieto));
        r.actualizarMetricas(List.of(pedido("A", 10, EstadoPedido.PAGADO, "20000", 1, 2)));
        assertEquals(List.of(vendido), r.obtenerMasVendidos(10));
        assertEquals(List.of(quieto), r.obtenerSinVentas());
        assertEquals(new BigDecimal("0.40"), vendido.calcularRotacion().orElseThrow()); // 2 / (4 + 1)
        assertEquals(new BigDecimal("0.00"), quieto.calcularRotacion().orElseThrow());
        var inv = new ReporteInventario(p, List.of(vendido, quieto), List.of());
        assertEquals(new BigDecimal("55000.00"), inv.calcularValorTotal());
        String csv = new ExportadorCsv(';', true).exportarInventario(inv).orElseThrow();
        assertTrue(csv.startsWith("libro_id;titulo"));
        assertTrue(new ExportadorCsv(';', true).exportarVentas(new ReporteVentas(p, List.of())).isEmpty());
    }
}
