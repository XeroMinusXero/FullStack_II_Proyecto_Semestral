package com.thequietchapter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClienteAnalitico {
    private final Long clienteId;
    private final String nombre;
    private final String email;
    private final LocalDateTime fechaRegistro;
    private long cantidadPedidosPeriodo;
    private BigDecimal montoCompradoPeriodo = BigDecimal.ZERO;

    public ClienteAnalitico(Long clienteId, String nombre, String email, LocalDateTime fechaRegistro) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
    }

    public void sumarPedido(BigDecimal total) {
        cantidadPedidosPeriodo++;
        montoCompradoPeriodo = montoCompradoPeriodo.add(total);
    }

    public boolean estaRegistradoEn(PeriodoReporte p) {
        return p.incluye(fechaRegistro);
    }

    public boolean registradoAntesDe(java.time.LocalDate fin) {
        return fechaRegistro != null && !fechaRegistro.toLocalDate().isAfter(fin);
    }

    public boolean tieneComprasEnPeriodo() {
        return cantidadPedidosPeriodo > 0;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public long getCantidadPedidosPeriodo() {
        return cantidadPedidosPeriodo;
    }

    public BigDecimal getMontoCompradoPeriodo() {
        return montoCompradoPeriodo;
    }
}
