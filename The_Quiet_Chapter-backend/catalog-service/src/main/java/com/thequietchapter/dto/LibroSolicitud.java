package com.thequietchapter.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Datos editables de un libro (crear y editar). Precio y stock inicial solo al
 * crear; despues usan rutas propias.
 */
public record LibroSolicitud(
        @NotBlank @Size(max = 40) String isbn,
        @NotBlank @Size(max = 255) String titulo,
        @NotEmpty List<@NotBlank @Size(max = 200) String> autores,
        @NotBlank @Size(max = 150) String editorial,
        @NotBlank @Size(max = 150) String categoria,
        List<@NotBlank @Size(max = 150) String> generos,
        String sinopsis,
        @Size(max = 80) String formato,
        @PositiveOrZero Integer paginas,
        @Size(max = 80) String idioma,
        @PositiveOrZero Integer anioPublicacion,
        @Size(max = 80) String encuadernacion,
        String portadaUrl,
        String extractoUrl,
        @DecimalMin("0.01") java.math.BigDecimal precio,
        @PositiveOrZero Integer stockInicial) {
}
