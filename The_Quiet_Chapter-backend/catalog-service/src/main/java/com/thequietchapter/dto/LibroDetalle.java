package com.thequietchapter.dto;

import com.thequietchapter.model.Libro;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Ficha completa (HU-008). Autores/generos con id para navegar a la vista de
 * autor y filtros.
 */
public record LibroDetalle(Long id, String isbn, String titulo, BigDecimal precio, boolean activo, int stock,
        boolean disponible, String sinopsis, String formato, int paginas, String idioma,
        int anioPublicacion, String encuadernacion, String portadaUrl, String extractoUrl,
        boolean tieneExtracto, ClasificacionRespuesta categoria, ClasificacionRespuesta editorial,
        List<ClasificacionRespuesta> autores, List<ClasificacionRespuesta> generos,
        LocalDateTime fechaRegistro) {
    public static LibroDetalle desde(Libro l) {
        var f = l.getFicha();
        int stock = l.getInventario().consultarStock();
        return new LibroDetalle(l.getId(), l.getIsbn(), l.getTitulo(), l.getPrecio(), l.isActivo(), stock,
                l.isActivo() && stock > 0, f.getSinopsis(), f.getFormato(), f.getPaginas(), f.getIdioma(),
                f.getAnioPublicacion(), f.getEncuadernacion(), f.getPortadaUrl(), f.getExtractoUrl(), f.tieneExtracto(),
                new ClasificacionRespuesta(l.getCategoria().getId(), l.getCategoria().getNombre()),
                new ClasificacionRespuesta(l.getEditorial().getId(), l.getEditorial().getNombre()),
                l.getAutores().stream().map(a -> new ClasificacionRespuesta(a.getId(), a.getNombre()))
                        .sorted(Comparator.comparing(ClasificacionRespuesta::nombre)).toList(),
                l.getGeneros().stream().map(g -> new ClasificacionRespuesta(g.getId(), g.getNombre()))
                        .sorted(Comparator.comparing(ClasificacionRespuesta::nombre)).toList(),
                l.getFechaRegistro());
    }
}
