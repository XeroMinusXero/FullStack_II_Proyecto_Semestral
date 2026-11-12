package com.thequietchapter.dto;

import java.time.LocalDateTime;

/** Interno para report-service (DC-09 MovimientoStockAnalitico). */
public record MovimientoConsolidado(Long movimientoId, Long libroId, String tituloLibro, LocalDateTime fecha,
        String tipo, int cantidad, Long responsableId) {
}
