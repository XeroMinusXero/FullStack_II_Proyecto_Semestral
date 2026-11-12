package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * DC-07: a que libros aplica una promocion (libro, categoria, genero o
 * editorial).
 */
@Entity
@Table(name = "alcances_promocion")
public class AlcancePromocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlcance tipo;
    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    protected AlcancePromocion() {
    }

    public AlcancePromocion(TipoAlcance tipo, Long referenciaId) {
        if (tipo == null || referenciaId == null || referenciaId <= 0)
            throw new IllegalArgumentException("Alcance invalido");
        this.tipo = tipo;
        this.referenciaId = referenciaId;
    }

    public boolean coincide(Long libroId, Long categoriaId, List<Long> generoIds, Long editorialId) {
        return switch (tipo) {
            case LIBRO -> referenciaId.equals(libroId);
            case CATEGORIA -> referenciaId.equals(categoriaId);
            case GENERO -> generoIds != null && generoIds.contains(referenciaId);
            case EDITORIAL -> referenciaId.equals(editorialId);
        };
    }

    public Long getId() {
        return id;
    }

    public TipoAlcance getTipo() {
        return tipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }
}
