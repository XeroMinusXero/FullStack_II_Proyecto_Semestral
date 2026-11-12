package com.thequietchapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuponSolicitud(@NotBlank @Size(max = 100) String codigo) {
}
