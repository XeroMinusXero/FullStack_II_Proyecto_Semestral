package com.thequietchapter.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** DC-09 / HU-140: KPIs del dashboard. */
public class PanelIndicadores {
    private final ReporteVentas reporteVentas;
    private final ReporteClientes reporteClientes;

    public PanelIndicadores(ReporteVentas v, ReporteClientes c) {
        this.reporteVentas = v;
        this.reporteClientes = c;
    }

    public BigDecimal obtenerVentasPeriodo() {
        return reporteVentas.calcularMontoTotal();
    }

    public long obtenerCantidadPedidos() {
        return reporteVentas.contarPedidos();
    }

    public BigDecimal calcularTicketPromedio() {
        long n = obtenerCantidadPedidos();
        return n == 0 ? BigDecimal.ZERO : obtenerVentasPeriodo().divide(BigDecimal.valueOf(n), 0, RoundingMode.HALF_UP);
    }

    public long obtenerClientesRegistrados() {
        return reporteClientes.contarClientesAcumulados();
    }
}
