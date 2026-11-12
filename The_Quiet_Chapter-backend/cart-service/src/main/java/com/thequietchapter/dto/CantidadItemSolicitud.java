package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record CantidadItemSolicitud(@Min(1) @Max(999) int cantidad) {
}
