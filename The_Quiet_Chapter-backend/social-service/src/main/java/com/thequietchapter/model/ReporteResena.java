package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_resena")
public class ReporteResena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_reportante_id", nullable = false)
    private Long clienteReportanteId;
    @Column(nullable = false, length = 500)
    private String motivo;
    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado;
    @Column(name = "administrador_id")
    private Long administradorId;
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    protected ReporteResena() {
    }

    public ReporteResena(Long clienteReportanteId, String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.strip().length() > 500)
            throw new IllegalArgumentException("Indica el motivo del reporte (maximo 500 caracteres)");
        this.clienteReportanteId = clienteReportanteId;
        this.motivo = motivo.strip();
        this.fechaReporte = LocalDateTime.now();
        this.estado = EstadoReporte.PENDIENTE;
    }

    public boolean resolver(EstadoReporte resultado, Long administradorId) {
        if (!estaPendiente() || resultado == null || resultado == EstadoReporte.PENDIENTE)
            return false;
        this.estado = resultado;
        this.administradorId = administradorId;
        this.fechaResolucion = LocalDateTime.now();
        return true;
    }

    public boolean estaPendiente() {
        return estado == EstadoReporte.PENDIENTE;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteReportanteId() {
        return clienteReportanteId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public Long getAdministradorId() {
        return administradorId;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }
}
