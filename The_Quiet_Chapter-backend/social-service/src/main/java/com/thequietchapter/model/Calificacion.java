package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DC-06: una calificacion (1 a 5 estrellas) por cliente y libro; recalificar la
 * actualiza (HU-083).
 */
@Entity
@Table(name = "calificaciones", uniqueConstraints = @UniqueConstraint(name = "uk_calificacion_cliente_libro", columnNames = {
        "cliente_id", "libro_id" }))
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(nullable = false)
    private int puntuacion;
    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Calificacion() {
    }

    public Calificacion(Long clienteId, Long libroId, int puntuacion) {
        this.clienteId = clienteId;
        this.libroId = libroId;
        if (!actualizarPuntuacion(puntuacion))
            throw new IllegalArgumentException("La calificacion debe estar entre 1 y 5");
    }

    public boolean actualizarPuntuacion(int nueva) {
        if (nueva < 1 || nueva > 5)
            return false;
        this.puntuacion = nueva;
        this.fecha = LocalDateTime.now();
        return true;
    }

    public boolean esValida() {
        return puntuacion >= 1 && puntuacion <= 5;
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

    public int getPuntuacion() {
        return puntuacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
