package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record MedioPagoSolicitud(@NotBlank @Size(max = 120) String nombre, boolean activo) {
}
