package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** DC-08 / HU-131: aviso cuando un libro agotado vuelve a tener stock. */
@Entity
@Table(name = "solicitudes_reabastecimiento", indexes = @Index(name = "ix_reab_libro", columnList = "libro_id,activa"))
public class SolicitudReabastecimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;
    @Column(nullable = false)
    private boolean activa;

    protected SolicitudReabastecimiento() {
    }

    public SolicitudReabastecimiento(Long clienteId, Long libroId) {
        this.clienteId = clienteId;
        this.libroId = libroId;
        this.fechaSolicitud = LocalDateTime.now();
        this.activa = true;
    }

    public void marcarAtendida() {
        this.activa = false;
    }

    public boolean estaActiva() {
        return activa;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getLibroId() {
        return libroId;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
}
