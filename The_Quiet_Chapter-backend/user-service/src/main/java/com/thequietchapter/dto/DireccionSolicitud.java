package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record DireccionSolicitud(
        @NotBlank @Size(max = 160) String calle,
        @NotBlank @Size(max = 30) String numero,
        @NotBlank @Size(max = 100) String comuna,
        @NotBlank @Size(max = 100) String region) {
}
