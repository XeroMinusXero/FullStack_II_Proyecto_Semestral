package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aplicaciones_promocion", uniqueConstraints = @UniqueConstraint(name = "uk_aplicacion", columnNames = {
        "promocion_id", "pedido_id", "libro_id" }))
public class AplicacionPromocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(name = "monto_descuento", nullable = false, precision = 13, scale = 2)
    private BigDecimal montoDescuento;
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDateTime fechaAplicacion;

    protected AplicacionPromocion() {
    }

    AplicacionPromocion(Long pedidoId, Long libroId, BigDecimal montoDescuento) {
        this.pedidoId = pedidoId;
        this.libroId = libroId;
        this.montoDescuento = montoDescuento;
        this.fechaAplicacion = LocalDateTime.now();
    }

    public boolean correspondeAPedido(Long pedidoId) {
        return this.pedidoId.equals(pedidoId);
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getLibroId() {
        return libroId;
    }

    public BigDecimal getMontoDescuento() {
        return montoDescuento;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }
}
