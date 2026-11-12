package com.thequietchapter.dto;

import com.thequietchapter.model.Libro;
import java.math.BigDecimal;

/** Consulta para el futuro carrito (no accede a la BD del catalogo). */
public record CondicionLibroRespuesta(Long id, String titulo, BigDecimal precio,
        int stock, boolean activo, String portadaUrl, String autores) {
    public static CondicionLibroRespuesta desde(Libro l) {
        return new CondicionLibroRespuesta(l.getId(), l.getTitulo(), l.getPrecio(),
                l.getInventario().consultarStock(), l.isActivo(), l.getFicha().getPortadaUrl(),
                String.join(", ", l.getAutores().stream().map(a -> a.getNombre()).sorted().toList()));
    }
}
