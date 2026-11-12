package com.thequietchapter.dto;

import com.thequietchapter.model.Direccion;

public record DireccionRespuesta(Long id, String calle, String numero, String comuna, String region,
        boolean predeterminada) {
    public static DireccionRespuesta desde(Direccion d) {
        return new DireccionRespuesta(d.getId(), d.getCalle(), d.getNumero(), d.getComuna(), d.getRegion(),
                d.isPredeterminada());
    }
}
