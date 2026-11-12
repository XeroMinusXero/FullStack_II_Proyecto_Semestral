package com.thequietchapter.dto;
import jakarta.validation.constraints.Min;
/** Actualizacion directa de stock (movimiento ACTUALIZACION). */
public record ActualizarStockSolicitud(@Min(0) int nuevoStock) {}
