package com.thequietchapter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato de alta desde Checkout una vez CONFIRMADO el pago (en desarrollo,
 * confirmación manual).
 */
public record CrearPedidoSolicitud(
        @NotNull @Positive Long clienteId,
        @NotEmpty List<@Valid Detalle> detalles,
        @NotNull @Valid Entrega entrega,
        @NotNull @Valid Importes importes,
        @NotBlank @Size(max = 120) String medioPago,
        @NotBlank @Size(max = 150) String referenciaPago) {
    public record Detalle(@NotNull @Positive Long libroId,
            @NotBlank @Size(max = 250) String tituloLibro, @Positive int cantidad,
            @NotNull @DecimalMin("0.00") BigDecimal precioUnitario) {
    }

    public record Entrega(@NotBlank @Size(max = 150) String calle,
            @NotBlank @Size(max = 40) String numero, @NotBlank @Size(max = 120) String comuna,
            @NotBlank @Size(max = 120) String region, @NotBlank @Size(max = 120) String metodoEnvio,
            @Min(0) int plazoEstimadoDias) {
    }

    public record Importes(@NotNull @DecimalMin("0.00") BigDecimal subtotal,
            @NotNull @DecimalMin("0.00") BigDecimal costoEnvio,
            @NotNull @DecimalMin("0.00") BigDecimal impuestos,
            @NotNull @DecimalMin("0.00") BigDecimal descuento) {
    }
}
