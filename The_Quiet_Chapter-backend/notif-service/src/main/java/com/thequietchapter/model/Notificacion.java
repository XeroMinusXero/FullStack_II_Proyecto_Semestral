package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** DC-08: un correo (simulado) con su estado de envio. */
@Entity
@Table(name = "notificaciones", indexes = { @Index(name = "ix_notif_destinatario", columnList = "destinatario"),
        @Index(name = "ix_notif_estado", columnList = "estado") })
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String destinatario;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;
    @Column(nullable = false, length = 200)
    private String asunto;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;
    @Column(name = "enlace_accion", length = 500)
    private String enlaceAccion;
    @Column(name = "enlace_adjunto", length = 500)
    private String enlaceAdjunto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNotificacion estado;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    protected Notificacion() {
    }

    public Notificacion(String destinatario, TipoNotificacion tipo, String asunto, String mensaje) {
        if (destinatario == null || !destinatario.strip().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Destinatario invalido");
        if (tipo == null || asunto == null || asunto.isBlank() || mensaje == null || mensaje.isBlank())
            throw new IllegalArgumentException("Tipo, asunto y mensaje obligatorios");
        this.destinatario = destinatario.strip().toLowerCase();
        this.tipo = tipo;
        this.asunto = asunto.strip().length() > 200 ? asunto.strip().substring(0, 200) : asunto.strip();
        this.mensaje = mensaje.strip();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void asignarEnlaceAccion(String url) {
        this.enlaceAccion = url;
    }

    public void asignarEnlaceAdjunto(String url) {
        this.enlaceAdjunto = url;
    }

    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.fechaEnvio = LocalDateTime.now();
        this.detalleError = null;
    }

    public void registrarFallo(String detalle) {
        this.estado = EstadoNotificacion.FALLIDA;
        this.detalleError = detalle == null ? "Error" : detalle;
    }

    public boolean estaPendiente() {
        return estado == EstadoNotificacion.PENDIENTE;
    }

    public Long getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getEnlaceAccion() {
        return enlaceAccion;
    }

    public String getEnlaceAdjunto() {
        return enlaceAdjunto;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public String getDetalleError() {
        return detalleError;
    }
}
