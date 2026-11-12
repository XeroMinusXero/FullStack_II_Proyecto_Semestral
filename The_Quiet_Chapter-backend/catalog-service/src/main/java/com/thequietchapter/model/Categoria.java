package com.thequietchapter.model;

import jakarta.persistence.*;

/** Entidad de clasificacion de DC-01. */
@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    protected Categoria() {
    } // Requerido por JPA.

    public Categoria(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre obligatorio");
        this.nombre = nombre.strip();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
