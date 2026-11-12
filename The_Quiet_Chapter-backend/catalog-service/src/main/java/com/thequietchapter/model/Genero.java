package com.thequietchapter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "generos", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Genero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    protected Genero() {
    }

    public Genero(String nombre) {
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
