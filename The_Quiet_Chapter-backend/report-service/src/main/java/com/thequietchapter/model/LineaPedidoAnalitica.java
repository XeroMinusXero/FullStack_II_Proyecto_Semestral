package com.thequietchapter.model;

import java.math.BigDecimal;

public record LineaPedidoAnalitica(Long libroId, String tituloLibro, String autor, Long categoriaId,
        String categoriaNombre,
        int cantidad, BigDecimal precioUnitario) {
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
