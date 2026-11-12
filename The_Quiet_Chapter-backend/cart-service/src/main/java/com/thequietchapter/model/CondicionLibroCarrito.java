package com.thequietchapter.model;

import java.math.BigDecimal;

/**
 * Datos vigentes recibidos por HTTP desde Catalog Service; NO se persisten como
 * entidad local.
 */
public class CondicionLibroCarrito {
    private final Long libroId;
    private final BigDecimal precioVigente;
    private final int stockDisponible;
    private final boolean activo;

    public CondicionLibroCarrito(Long libroId, BigDecimal precioVigente, int stockDisponible, boolean activo) {
        if (libroId == null || libroId <= 0 || precioVigente == null || precioVigente.signum() < 0
                || stockDisponible < 0)
            throw new IllegalArgumentException("Condicion del libro invalida");
        this.libroId = libroId;
        this.precioVigente = precioVigente;
        this.stockDisponible = stockDisponible;
        this.activo = activo;
    }

    public boolean puedeAgregar() {
        return activo && stockDisponible > 0 && precioVigente.signum() > 0;
    }

    public boolean permiteCantidad(int cantidad) {
        return puedeAgregar() && cantidad > 0 && cantidad <= stockDisponible;
    }

    public Long getLibroId() {
        return libroId;
    }

    public BigDecimal getPrecioVigente() {
        return precioVigente;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public boolean isActivo() {
        return activo;
    }
}
