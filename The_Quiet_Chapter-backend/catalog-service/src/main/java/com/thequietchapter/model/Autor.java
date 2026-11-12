package com.thequietchapter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "autores", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String biografia;

    protected Autor() {
    }

    public Autor(String nombre, String biografia) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre obligatorio");
        this.nombre = nombre.strip();
        this.biografia = biografia == null ? "" : biografia;
    }

    public void actualizarBiografia(String biografia) {
        this.biografia = biografia == null ? "" : biografia.strip();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBiografia() {
        return biografia;
    }
}
