package com.thequietchapter.dto;

import com.thequietchapter.model.MedioPago;

public record MedioPagoRespuesta(Long id, String nombre, boolean activo) {
    public static MedioPagoRespuesta desde(MedioPago m) {
        return new MedioPagoRespuesta(m.getId(), m.getNombre(), m.isActivo());
    }
}
