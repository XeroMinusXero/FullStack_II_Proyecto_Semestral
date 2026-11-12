package com.thequietchapter.dto;

import com.thequietchapter.model.Libro;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interno: vista plana para promo-service (recomendaciones/alcances) y
 * report-service (DC-09 LibroAnalitico).
 */
public record LibroInterno(Long id, String titulo, String autores, List<Long> autorIds, Long categoriaId,
        String categoriaNombre, Long editorialId, List<Long> generoIds, BigDecimal precio,
        int stock, boolean activo, LocalDateTime fechaRegistro, String portadaUrl) {
    public static LibroInterno desde(Libro l) {
        return new LibroInterno(l.getId(), l.getTitulo(),
                String.join(", ", l.getAutores().stream().map(a -> a.getNombre()).sorted().toList()),
                l.getAutores().stream().map(a -> a.getId()).sorted().toList(),
                l.getCategoria().getId(), l.getCategoria().getNombre(), l.getEditorial().getId(),
                l.getGeneros().stream().map(g -> g.getId()).sorted().toList(),
                l.getPrecio(), l.getInventario().consultarStock(), l.isActivo(), l.getFechaRegistro(),
                l.getFicha().getPortadaUrl());
    }
}
