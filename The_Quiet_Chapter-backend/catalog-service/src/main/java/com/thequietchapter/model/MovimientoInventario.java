package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Registro historico; una vez creado no se modifica (DC-01). */
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;
    @Column(name = "stock_anterior", nullable = false)
    private int stockAnterior;
    @Column(name = "stock_posterior", nullable = false)
    private int stockPosterior;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(nullable = false, length = 500)
    private String motivo;
    @Column(name = "responsable_id")
    private Long responsableId;

    protected MovimientoInventario() { } // JPA

    MovimientoInventario(TipoMovimiento tipo, int anterior, int posterior,
                         String motivo, Long responsableId) {
        this.tipo = java.util.Objects.requireNonNull(tipo);
        this.stockAnterior = anterior;
        this.stockPosterior = posterior;
        this.cantidad = Math.abs(posterior - anterior);
        this.fecha = LocalDateTime.now();
        this.motivo = motivo == null ? "" : motivo;
        this.responsableId = responsableId;
    }

    public int calcularVariacion() { return stockPosterior - stockAnterior; }
    public Long getId() { return id; }
    public TipoMovimiento getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public int getStockAnterior() { return stockAnterior; }
    public int getStockPosterior() { return stockPosterior; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public Long getResponsableId() { return responsableId; }
}
