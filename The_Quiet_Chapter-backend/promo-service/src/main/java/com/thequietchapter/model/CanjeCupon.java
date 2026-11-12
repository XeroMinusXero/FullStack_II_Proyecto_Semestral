package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "canjes_cupon", uniqueConstraints = @UniqueConstraint(name = "uk_canje_cupon_pedido", columnNames = {
        "cupon_id", "pedido_id" }))
public class CanjeCupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
    @Column(name = "fecha_canje", nullable = false)
    private LocalDateTime fechaCanje;
    @Column(name = "monto_descuento", nullable = false, precision = 13, scale = 2)
    private BigDecimal montoDescuento;

    protected CanjeCupon() {
    }

    CanjeCupon(Long clienteId, Long pedidoId, BigDecimal montoDescuento) {
        this.clienteId = clienteId;
        this.pedidoId = pedidoId;
        this.montoDescuento = montoDescuento;
        this.fechaCanje = LocalDateTime.now();
    }

    public boolean correspondeAPedido(Long pedidoId) {
        return this.pedidoId.equals(pedidoId);
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public LocalDateTime getFechaCanje() {
        return fechaCanje;
    }

    public BigDecimal getMontoDescuento() {
        return montoDescuento;
    }
}
