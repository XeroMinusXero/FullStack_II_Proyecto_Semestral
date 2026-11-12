package com.thequietchapter.model;

import java.time.LocalDate;

public class CriteriosPedido {
    private final EstadoPedido estado;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;

    public CriteriosPedido(EstadoPedido estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        this.estado = estado;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        if (!esRangoFechasValido())
            throw new IllegalArgumentException("fechaDesde no puede superar fechaHasta");
    }

    public boolean esRangoFechasValido() {
        return fechaDesde == null || fechaHasta == null || !fechaDesde.isAfter(fechaHasta);
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }
}
