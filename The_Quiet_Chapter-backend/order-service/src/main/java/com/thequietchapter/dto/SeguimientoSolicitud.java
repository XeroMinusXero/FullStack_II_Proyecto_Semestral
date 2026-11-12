package com.thequietchapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeguimientoSolicitud(@NotBlank @Size(max = 120) String numeroSeguimiento) {
}
