package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record CambiarContrasenaSolicitud(@NotBlank String actual, @NotBlank String nueva) {
}
