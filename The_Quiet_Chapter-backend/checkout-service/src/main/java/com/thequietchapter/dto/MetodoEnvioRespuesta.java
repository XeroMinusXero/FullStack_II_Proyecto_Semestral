package com.thequietchapter.dto;

import com.thequietchapter.model.MetodoEnvio;
import java.math.BigDecimal;
import java.util.List;

public record MetodoEnvioRespuesta(Long id, String nombre, BigDecimal costoBase, int plazoEstimadoDias, boolean activo,
        List<String> regionesHabilitadas) {
    public static MetodoEnvioRespuesta desde(MetodoEnvio m) {
        return new MetodoEnvioRespuesta(m.getId(), m.getNombre(), m.getCostoBase(), m.getPlazoEstimadoDias(),
                m.isActivo(), m.getRegionesHabilitadas());
    }
}
