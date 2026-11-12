package com.thequietchapter.dto;

import com.thequietchapter.model.MovimientoInventario;
import java.time.LocalDateTime;

/** HU-063: tipo, cantidad, fecha y usuario responsable. */
public record MovimientoStockRespuesta(Long id, String tipo, int cantidad, int variacion, int stockAnterior,
        int stockPosterior, LocalDateTime fecha, String motivo, Long responsableId) {
    public static MovimientoStockRespuesta desde(MovimientoInventario m) {
        return new MovimientoStockRespuesta(m.getId(), m.getTipo().name(), m.getCantidad(), m.calcularVariacion(),
                m.getStockAnterior(), m.getStockPosterior(), m.getFecha(), m.getMotivo(), m.getResponsableId());
    }
}
