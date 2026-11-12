package com.thequietchapter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoAnalitico(String numeroPedido, LocalDateTime fecha, Long clienteId, String clienteNombre,
        EstadoPedido estado,
        BigDecimal total, List<LineaPedidoAnalitica> lineas) {
    public boolean estaEnPeriodo(PeriodoReporte p) {
        return p.incluye(fecha);
    }

    public int cantidadUnidades() {
        return lineas.stream().mapToInt(LineaPedidoAnalitica::cantidad).sum();
    }

    /** Los pedidos cancelados no cuentan como venta. */
    public boolean esVentaContabilizable() {
        return estado != EstadoPedido.CANCELADO;
    }
}
