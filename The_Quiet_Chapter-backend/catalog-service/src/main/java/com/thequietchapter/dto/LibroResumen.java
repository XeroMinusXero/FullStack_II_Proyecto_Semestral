package com.thequietchapter.dto;

import com.thequietchapter.model.Libro;
import java.math.BigDecimal;
import java.util.List;

/**
 * Tarjeta de catalogo (HU-001): portada, titulo, autor, precio y
 * disponibilidad.
 */
public record LibroResumen(Long id, String titulo, BigDecimal precio, int stock, String isbn, String portadaUrl,
        Long categoriaId, String categoria, Long editorialId, String editorial,
        List<String> autores, List<String> generos, boolean activo, boolean disponible) {
    public static LibroResumen desde(Libro l) {
        int stock = l.getInventario().consultarStock();
        return new LibroResumen(l.getId(), l.getTitulo(), l.getPrecio(), stock, l.getIsbn(),
                l.getFicha().getPortadaUrl(), l.getCategoria().getId(), l.getCategoria().getNombre(),
                l.getEditorial().getId(), l.getEditorial().getNombre(),
                l.getAutores().stream().map(a -> a.getNombre()).sorted().toList(),
                l.getGeneros().stream().map(g -> g.getNombre()).sorted().toList(),
                l.isActivo(), l.isActivo() && stock > 0);
    }
}
