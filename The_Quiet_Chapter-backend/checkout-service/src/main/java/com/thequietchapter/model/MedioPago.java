package com.thequietchapter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "checkout_medios_pago")
public class MedioPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false)
    private boolean activo;

    protected MedioPago() {
    }

    public MedioPago(String nombre, boolean activo) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 120)
            throw new IllegalArgumentException("Nombre del medio de pago inválido");
        this.nombre = nombre.trim();
        this.activo = activo;
    }

    public void actualizar(String nombre, boolean activo) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 120)
            throw new IllegalArgumentException("Nombre del medio de pago inválido");
        this.nombre = nombre.trim();
        this.activo = activo;
    }

    public boolean estaDisponible() {
        return activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }
}
