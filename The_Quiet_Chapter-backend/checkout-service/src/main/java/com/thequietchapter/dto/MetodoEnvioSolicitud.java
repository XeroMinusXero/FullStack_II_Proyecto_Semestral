package com.thequietchapter.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record MetodoEnvioSolicitud(@NotBlank @Size(max = 120) String nombre,
        @NotNull @DecimalMin("0") BigDecimal costoBase,
        @Min(0) int plazoEstimadoDias, boolean activo, List<@NotBlank @Size(max = 100) String> regionesHabilitadas) {
}
