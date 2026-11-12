package com.thequietchapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionEnvioSolicitud(
        @NotBlank @Size(max = 180) String calle,
        @NotBlank @Size(max = 25) String numero,
        @NotBlank @Size(max = 100) String comuna,
        @NotBlank @Size(max = 100) String region) {
}
