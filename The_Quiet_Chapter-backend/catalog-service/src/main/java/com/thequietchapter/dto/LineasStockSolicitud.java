package com.thequietchapter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Interno (checkout/order): descuento o reposicion de varias lineas en UNA
 * transaccion, idempotente por referencia.
 */
public record LineasStockSolicitud(@NotBlank @Size(max = 150) String referencia, @NotEmpty List<@Valid Linea> lineas) {
    public record Linea(@NotNull @Positive Long libroId, @Positive int cantidad) {
    }
}
