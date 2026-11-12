package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DC-08 / HU-132..134: doble confirmacion y cancelacion por token sin
 * autenticarse.
 */
@Entity
@Table(name = "suscripciones_boletin", uniqueConstraints = {
        @UniqueConstraint(name = "uk_suscripcion_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_token_confirmacion", columnNames = "token_confirmacion"),
        @UniqueConstraint(name = "uk_token_cancelacion", columnNames = "token_cancelacion") })
public class SuscripcionBoletin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String email;
    @Column(name = "fecha_suscripcion", nullable = false)
    private LocalDateTime fechaSuscripcion;
    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSuscripcion estado;
    @Column(name = "token_confirmacion", nullable = false, length = 64)
    private String tokenConfirmacion;
    @Column(name = "token_cancelacion", nullable = false, length = 64)
    private String tokenCancelacion;

    protected SuscripcionBoletin() {
    }

    public SuscripcionBoletin(String email, String tokenConfirmacion, String tokenCancelacion) {
        if (email == null || !email.strip().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Correo con formato invalido");
        this.email = email.strip().toLowerCase();
        reiniciar(tokenConfirmacion, tokenCancelacion);
    }

    /**
     * Re-suscripcion de un correo cancelado: vuelve a PENDIENTE con tokens nuevos.
     */
    public void reiniciar(String tokenConfirmacion, String tokenCancelacion) {
        this.tokenConfirmacion = tokenConfirmacion;
        this.tokenCancelacion = tokenCancelacion;
        this.fechaSuscripcion = LocalDateTime.now();
        this.fechaConfirmacion = null;
        this.estado = EstadoSuscripcion.PENDIENTE;
    }

    public boolean confirmar(String token) {
        if (estado != EstadoSuscripcion.PENDIENTE || !tokenConfirmacion.equals(token))
            return false;
        estado = EstadoSuscripcion.CONFIRMADA;
        fechaConfirmacion = LocalDateTime.now();
        return true;
    }

    public boolean cancelar(String token) {
        if (estado == EstadoSuscripcion.CANCELADA || !tokenCancelacion.equals(token))
            return false;
        estado = EstadoSuscripcion.CANCELADA;
        return true;
    }

    public boolean estaActiva() {
        return estado == EstadoSuscripcion.CONFIRMADA;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getFechaSuscripcion() {
        return fechaSuscripcion;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public EstadoSuscripcion getEstado() {
        return estado;
    }

    public String getTokenConfirmacion() {
        return tokenConfirmacion;
    }

    public String getTokenCancelacion() {
        return tokenCancelacion;
    }
}
