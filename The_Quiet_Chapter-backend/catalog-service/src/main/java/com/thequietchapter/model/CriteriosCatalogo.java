package com.thequietchapter.model;

import java.math.BigDecimal;
import java.util.List;

/** Objeto de criterios (DC-01). Los nulos indican filtro no solicitado. */
public final class CriteriosCatalogo {
    private final List<Long> categoriaIds;
    private final List<Long> generoIds;
    private final BigDecimal precioMinimo;
    private final BigDecimal precioMaximo;
    private final Boolean activo;
    private final boolean soloAgotados;
    private final Long autorId;
    private final Long editorialId;

    public CriteriosCatalogo(List<Long> categoriaIds, List<Long> generoIds,
            BigDecimal precioMinimo, BigDecimal precioMaximo,
            Boolean activo, boolean soloAgotados, Long autorId, Long editorialId) {
        this.categoriaIds = categoriaIds == null ? List.of() : List.copyOf(categoriaIds);
        this.generoIds = generoIds == null ? List.of() : List.copyOf(generoIds);
        this.precioMinimo = precioMinimo;
        this.precioMaximo = precioMaximo;
        this.activo = activo;
        this.soloAgotados = soloAgotados;
        this.autorId = autorId;
        this.editorialId = editorialId;
    }

    public boolean esRangoPrecioValido() {
        return (precioMinimo == null || precioMinimo.signum() >= 0)
                && (precioMaximo == null || precioMaximo.signum() >= 0)
                && (precioMinimo == null || precioMaximo == null || precioMinimo.compareTo(precioMaximo) <= 0);
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public List<Long> getGeneroIds() {
        return generoIds;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo;
    }

    public BigDecimal getPrecioMaximo() {
        return precioMaximo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public boolean isSoloAgotados() {
        return soloAgotados;
    }

    public Long getAutorId() {
        return autorId;
    }

    public Long getEditorialId() {
        return editorialId;
    }
}
