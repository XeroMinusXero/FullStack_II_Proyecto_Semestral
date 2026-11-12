package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record LoginSolicitud(@NotBlank @Email String email, @NotBlank String contrasena) {
}
