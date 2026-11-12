package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record RegistroSolicitud(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido,
        @NotBlank @Email @Size(max = 180) String email,
        @Size(max = 30) String telefono,
        @NotBlank String contrasena) {
}
