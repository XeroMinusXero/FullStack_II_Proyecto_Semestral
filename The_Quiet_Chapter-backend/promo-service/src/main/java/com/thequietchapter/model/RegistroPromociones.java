package com.thequietchapter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DC-07: reglas sobre el conjunto de promociones (la mejor promocion vigente
 * para un libro).
 */
public class RegistroPromociones {
    private final List<Promocion> promociones = new ArrayList<>();

    public RegistroPromociones(List<Promocion> promociones) {
        if (promociones != null)
            this.promociones.addAll(promociones);
    }

    public List<Promocion> listarVigentes(LocalDateTime fecha) {
        return promociones.stream().filter(p -> p.estaVigente(fecha)).toList();
    }

    public List<Promocion> buscarAplicables(Long libroId, Long categoriaId, List<Long> generoIds, Long editorialId,
            LocalDateTime fecha) {
        return promociones.stream().filter(p -> p.aplicaA(libroId, categoriaId, generoIds, editorialId, fecha))
                .toList();
    }

    /**
     * Si varias aplican, gana la de mayor descuento para el cliente (no se
     * acumulan).
     */
    public Optional<Promocion> mejorPara(Long libroId, Long categoriaId, List<Long> generoIds, Long editorialId,
            BigDecimal precio, LocalDateTime fecha) {
        return buscarAplicables(libroId, categoriaId, generoIds, editorialId, fecha).stream()
                .max(Comparator.comparing((Promocion p) -> p.calcularDescuento(precio))
                        .thenComparing(Promocion::getFechaFin));
    }
}
