package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** DC-08 / HU-135..138. */
@Entity
@Table(name = "boletines")
public class Boletin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String asunto;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBoletin estado;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
    @Column(name = "cantidad_destinatarios", nullable = false)
    private int cantidadDestinatarios;

    protected Boletin() {
    }

    public Boletin(String asunto, String contenido) {
        validar(asunto, contenido);
        this.asunto = asunto.strip();
        this.contenido = contenido.strip();
        this.estado = EstadoBoletin.BORRADOR;
        this.fechaCreacion = LocalDateTime.now();
    }

    private static void validar(String asunto, String contenido) {
        if (asunto == null || asunto.isBlank() || asunto.strip().length() > 200)
            throw new IllegalArgumentException("El boletin requiere asunto (maximo 200)");
        if (contenido == null || contenido.isBlank())
            throw new IllegalArgumentException("El boletin requiere contenido");
    }

    /** HU-136: solo en borrador. */
    public boolean editar(String asunto, String contenido) {
        if (!estaEnBorrador())
            return false;
        validar(asunto, contenido);
        this.asunto = asunto.strip();
        this.contenido = contenido.strip();
        return true;
    }

    public boolean puedeEnviarse() {
        return estaEnBorrador();
    }

    /** HU-137: no se reenvia. */
    public boolean marcarEnviado(int cantidadDestinatarios) {
        if (!puedeEnviarse())
            return false;
        this.estado = EstadoBoletin.ENVIADO;
        this.fechaEnvio = LocalDateTime.now();
        this.cantidadDestinatarios = cantidadDestinatarios;
        return true;
    }

    public boolean estaEnBorrador() {
        return estado == EstadoBoletin.BORRADOR;
    }

    public Long getId() {
        return id;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public EstadoBoletin getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public int getCantidadDestinatarios() {
        return cantidadDestinatarios;
    }
}
