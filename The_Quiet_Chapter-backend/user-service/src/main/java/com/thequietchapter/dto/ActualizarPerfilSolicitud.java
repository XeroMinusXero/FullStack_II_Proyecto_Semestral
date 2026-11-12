package com.thequietchapter.dto;

import jakarta.validation.constraints.*;

public record ActualizarPerfilSolicitud(@NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido, @Size(max = 30) String telefono) {
}
