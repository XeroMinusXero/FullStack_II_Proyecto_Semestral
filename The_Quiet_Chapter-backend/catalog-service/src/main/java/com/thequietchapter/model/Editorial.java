package com.thequietchapter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "editoriales", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Editorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    protected Editorial() {
    }

    public Editorial(String nombre) {
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
