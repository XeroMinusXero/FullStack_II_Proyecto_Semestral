package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record AgregarItemSolicitud(@NotNull @Positive Long libroId,
        @Min(1) @Max(999) int cantidad) {
}
