package com.thequietchapter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperacionSolicitud(@NotBlank @Email String email) {
}
