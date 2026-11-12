package com.thequietchapter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IniciarCheckoutSolicitud(@NotNull @Positive Long carritoId) {
}
