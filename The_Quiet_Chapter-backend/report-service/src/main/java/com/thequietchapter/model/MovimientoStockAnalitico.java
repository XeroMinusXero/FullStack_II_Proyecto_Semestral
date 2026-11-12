package com.thequietchapter.model;

import java.time.LocalDateTime;

public record MovimientoStockAnalitico(Long movimientoId, Long libroId, String tituloLibro, LocalDateTime fecha,
        TipoMovimiento tipo,
        int cantidad, Long responsableId, String responsableNombre) {
    /**
     * HU-150: ingresos y ajustes (incluye actualizaciones manuales del
     * administrador).
     */
    public boolean esIngresoOAjuste() {
        return tipo == TipoMovimiento.INGRESO || tipo == TipoMovimiento.AJUSTE || tipo == TipoMovimiento.ACTUALIZACION;
    }

    public boolean ocurrioEn(PeriodoReporte p) {
        return p.incluye(fecha);
    }
}
