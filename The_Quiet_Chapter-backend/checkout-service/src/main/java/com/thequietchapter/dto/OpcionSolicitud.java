package com.thequietchapter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OpcionSolicitud(@NotNull @Positive Long id) {
}
