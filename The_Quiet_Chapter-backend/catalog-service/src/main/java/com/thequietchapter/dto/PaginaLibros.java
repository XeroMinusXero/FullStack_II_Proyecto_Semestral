package com.thequietchapter.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/** HU-002: incluye total de paginas para los controles de paginacion. */
public record PaginaLibros(List<LibroResumen> libros, int pagina, int tamanio, long totalElementos, int totalPaginas) {
    public static PaginaLibros desde(Page<LibroResumen> p) {
        return new PaginaLibros(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
