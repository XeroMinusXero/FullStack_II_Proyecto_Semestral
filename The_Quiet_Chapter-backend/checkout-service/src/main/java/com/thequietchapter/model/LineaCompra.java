package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "checkout_lineas_compra")
public class LineaCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long libroId;
    @Column(nullable = false, length = 240)
    private String titulo;
    @Column(nullable = false)
    private int cantidad;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal precioUnitario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resumen_id", nullable = false)
    private ResumenCompra resumen;

    protected LineaCompra() {
    }

    public LineaCompra(Long libroId, String titulo, int cantidad, BigDecimal precioUnitario) {
        if (libroId == null || libroId <= 0 || titulo == null || titulo.isBlank() || titulo.length() > 240
                || cantidad <= 0 || precioUnitario == null || precioUnitario.signum() <= 0)
            throw new IllegalArgumentException("Línea de compra inválida");
        this.libroId = libroId;
        this.titulo = titulo.trim();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    void vincularResumen(ResumenCompra r) {
        this.resumen = r;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}
