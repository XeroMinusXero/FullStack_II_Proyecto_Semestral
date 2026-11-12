package com.thequietchapter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RestablecerSolicitud(@NotBlank @Email String email, @NotBlank @Pattern(regexp = "\\d{6}") String codigo,
        @NotBlank String nueva) {
}
