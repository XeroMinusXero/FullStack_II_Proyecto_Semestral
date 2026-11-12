package com.thequietchapter.model;

import java.util.Set;

/**
 * DC-07: vista del libro para el recomendador (datos de catalog + popularidad
 * desde order).
 */
public record LibroRecomendable(Long libroId, Set<Long> autorIds, Long categoriaId, Set<Long> generoIds,
        boolean disponible, boolean destacado, int popularidad) {
    public boolean estaDisponible() {
        return disponible;
    }

    public boolean esRelacionadoCon(LibroRecomendable otro) {
        return puntaje(otro) > 0;
    }

    /** Mismo autor pesa mas que mismo genero, y este mas que misma categoria. */
    public int puntaje(LibroRecomendable otro) {
        if (otro == null || otro.libroId.equals(libroId))
            return 0;
        int p = 0;
        if (autorIds.stream().anyMatch(otro.autorIds::contains))
            p += 3;
        if (generoIds.stream().anyMatch(otro.generoIds::contains))
            p += 2;
        if (categoriaId != null && categoriaId.equals(otro.categoriaId))
            p += 1;
        return p;
    }
}
