package com.thequietchapter.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * DC-09. stockPromedio aproximado = stock actual + vendidas/2 (stock al inicio
 * ~ actual + vendidas en el periodo).
 */
public class LibroAnalitico {
    private final Long libroId;
    private final String titulo;
    private final String autor;
    private final boolean activo;
    private final int stockActual;
    private final BigDecimal precioVenta;
    private int unidadesVendidas;
    private BigDecimal montoTotal = BigDecimal.ZERO;

    public LibroAnalitico(Long libroId, String titulo, String autor, boolean activo, int stockActual,
            BigDecimal precioVenta) {
        this.libroId = libroId;
        this.titulo = titulo;
        this.autor = autor;
        this.activo = activo;
        this.stockActual = stockActual;
        this.precioVenta = precioVenta;
    }

    public void actualizarMetricas(int unidades, BigDecimal monto) {
        unidadesVendidas += unidades;
        montoTotal = montoTotal.add(monto);
    }

    public BigDecimal getStockPromedio() {
        return BigDecimal.valueOf(stockActual)
                .add(BigDecimal.valueOf(unidadesVendidas).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
    }

    /** HU-148: sin ventas = rotacion cero. */
    public Optional<BigDecimal> calcularRotacion() {
        if (unidadesVendidas == 0)
            return Optional.of(BigDecimal.ZERO.setScale(2));
        BigDecimal prom = getStockPromedio();
        return prom.signum() == 0 ? Optional.empty()
                : Optional.of(BigDecimal.valueOf(unidadesVendidas).divide(prom, 2, RoundingMode.HALF_UP));
    }

    public BigDecimal calcularValorizacion() {
        return precioVenta.multiply(BigDecimal.valueOf(stockActual)).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean tieneVentas() {
        return unidadesVendidas > 0;
    }

    public Long getLibroId() {
        return libroId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getStockActual() {
        return stockActual;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
}
