package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(name = "titulo_libro", nullable = false, length = 250)
    private String tituloLibro;
    @Column(nullable = false)
    private int cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    protected DetallePedido() {
    }

    public DetallePedido(Long libroId, String tituloLibro, int cantidad, BigDecimal precioUnitario) {
        if (libroId == null || libroId <= 0)
            throw new IllegalArgumentException("ID de libro inválido");
        if (tituloLibro == null || tituloLibro.isBlank() || tituloLibro.length() > 250)
            throw new IllegalArgumentException("Título de libro inválido");
        if (cantidad <= 0)
            throw new IllegalArgumentException("Cantidad debe ser positiva");
        if (precioUnitario == null || precioUnitario.signum() < 0)
            throw new IllegalArgumentException("Precio unitario inválido");
        this.libroId = libroId;
        this.tituloLibro = tituloLibro.trim();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    void vincularPedido(Pedido pedido) {
        this.pedido = Objects.requireNonNull(pedido);
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}
