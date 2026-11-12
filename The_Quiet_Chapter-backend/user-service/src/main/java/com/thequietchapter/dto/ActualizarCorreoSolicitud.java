package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record ActualizarCorreoSolicitud(@NotBlank @Email String email) {
}
