package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkout_pagos", indexes = { @Index(name = "ix_checkout_pago_proceso", columnList = "proceso_id") })
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal monto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoPago estado;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(length = 160, unique = true)
    private String referencia;
    @Column(length = 500)
    private String motivoRechazo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoCompra proceso;

    protected Pago() {
    }

    public Pago(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0)
            throw new IllegalArgumentException("Monto de pago debe ser positivo");
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
        this.estado = EstadoPago.PENDIENTE;
        this.fecha = LocalDateTime.now();
    }

    void vincularProceso(ProcesoCompra p) {
        this.proceso = p;
    }

    /**
     * Solo llamar tras recibir aprobación verificable del proveedor de pagos. Sin
     * endpoint público en esta etapa.
     */
    public void aprobar(String referencia) {
        if (estado != EstadoPago.PENDIENTE || referencia == null || referencia.isBlank())
            throw new IllegalStateException("Pago no aprobable sin confirmación verificable");
        this.referencia = referencia.trim();
        this.estado = EstadoPago.APROBADO;
    }

    public void rechazar(String motivo) {
        if (estado != EstadoPago.PENDIENTE)
            throw new IllegalStateException("Pago no pendiente");
        this.motivoRechazo = (motivo == null || motivo.isBlank()) ? "Pago rechazado" : motivo.trim();
        this.estado = EstadoPago.RECHAZADO;
    }

    public void cancelar() {
        if (estado != EstadoPago.PENDIENTE || referencia != null)
            throw new IllegalStateException("No se puede cancelar un pago confirmado o enviado al proveedor");
        estado = EstadoPago.CANCELADO;
    }

    public boolean estaAprobado() {
        return estado == EstadoPago.APROBADO;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }
}
