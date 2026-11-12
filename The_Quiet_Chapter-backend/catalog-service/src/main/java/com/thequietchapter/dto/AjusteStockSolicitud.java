package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

/**
 * HU de ajuste: motivo obligatorio. El responsable sale del token del
 * administrador.
 */
public record AjusteStockSolicitud(@Min(0) int nuevoStock, @NotBlank @Size(max = 400) String motivo) {
}
