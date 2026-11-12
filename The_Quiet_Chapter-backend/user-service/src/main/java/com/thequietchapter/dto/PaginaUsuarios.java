package com.thequietchapter.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PaginaUsuarios(List<UsuarioBasico> usuarios, int pagina, int tamano, long totalElementos,
        int totalPaginas) {
    public static PaginaUsuarios desde(Page<UsuarioBasico> p) {
        return new PaginaUsuarios(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
