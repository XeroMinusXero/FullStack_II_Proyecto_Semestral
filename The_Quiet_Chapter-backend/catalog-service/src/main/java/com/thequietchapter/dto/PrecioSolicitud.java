package com.thequietchapter.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PrecioSolicitud(@NotNull @DecimalMin("0.01") BigDecimal precio) {
}
