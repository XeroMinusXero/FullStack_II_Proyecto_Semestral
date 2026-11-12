package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DC-06: elemento de la lista de favoritos (composicion). Referencia al libro
 * solo por id (otro microservicio).
 */
@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(name = "uk_favorito_lista_libro", columnNames = {
        "lista_id", "libro_id" }))
public class Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    protected Favorito() {
    }

    public Favorito(Long libroId) {
        if (libroId == null || libroId <= 0)
            throw new IllegalArgumentException("Libro invalido");
        this.libroId = libroId;
        this.fechaAgregado = LocalDateTime.now();
    }

    public boolean correspondeALibro(Long libroId) {
        return this.libroId.equals(libroId);
    }

    public Long getId() {
        return id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }
}
