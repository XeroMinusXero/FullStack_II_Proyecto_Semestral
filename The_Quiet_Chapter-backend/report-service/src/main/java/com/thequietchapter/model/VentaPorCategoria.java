package com.thequietchapter.model;

import java.math.BigDecimal;

public class VentaPorCategoria {
    private final Long categoriaId;
    private final String categoriaNombre;
    private int unidadesVendidas;
    private BigDecimal montoTotal = BigDecimal.ZERO;

    public VentaPorCategoria(Long categoriaId, String categoriaNombre) {
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
    }

    public void sumarLinea(LineaPedidoAnalitica l) {
        unidadesVendidas += l.cantidad();
        montoTotal = montoTotal.add(l.calcularSubtotal());
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
}
