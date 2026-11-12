package com.thequietchapter.dto;

import jakarta.validation.constraints.Positive;

public record IngresoStockSolicitud(@Positive int cantidad) {
}
