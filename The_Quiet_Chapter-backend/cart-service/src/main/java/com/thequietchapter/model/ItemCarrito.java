package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Composicion Carrito -> ItemCarrito, segun DC-02. */
@Entity
@Table(name = "items_carrito", uniqueConstraints = @UniqueConstraint(name = "uk_carrito_libro", columnNames = {
        "carrito_id", "libro_id" }))
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Referencia tecnica para la composicion JPA (no nueva clase de negocio). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(nullable = false)
    private int cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 13, scale = 2)
    private BigDecimal precioUnitario;
    @Column(nullable = false)
    private boolean disponible;

    protected ItemCarrito() {
    }

    public ItemCarrito(Long libroId, BigDecimal precioUnitario) {
        if (libroId == null || libroId <= 0 || precioUnitario == null || precioUnitario.signum() <= 0)
            throw new IllegalArgumentException("Libro y precio deben ser validos");
        this.libroId = libroId;
        this.precioUnitario = precioUnitario;
        this.cantidad = 1;
        this.disponible = true;
    }

    public int ajustarCantidad(int nuevaCantidad, int stockDisponible) {
        if (nuevaCantidad <= 0 || stockDisponible < 0)
            throw new IllegalArgumentException("Cantidades invalidas");
        this.cantidad = Math.min(nuevaCantidad, stockDisponible);
        this.disponible = this.cantidad > 0;
        return this.cantidad;
    }

    public boolean sincronizarPrecio(BigDecimal precioVigente) {
        if (precioVigente == null || precioVigente.signum() <= 0)
            throw new IllegalArgumentException("Precio vigente invalido");
        boolean cambio = precioUnitario.compareTo(precioVigente) != 0;
        precioUnitario = precioVigente;
        return cambio;
    }

    public boolean verificarDisponibilidad(int stockDisponible, boolean activo) {
        disponible = activo && stockDisponible >= cantidad && cantidad > 0;
        return disponible;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    void vincularCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Long getId() {
        return id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
