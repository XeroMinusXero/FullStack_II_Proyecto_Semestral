package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Copia de la dirección elegida; no asume propiedad de la libreta de User
 * Service.
 */
@Entity
@Table(name = "checkout_direcciones")
public class DireccionEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String calle;
    @Column(nullable = false, length = 25)
    private String numero;
    @Column(nullable = false, length = 100)
    private String comuna;
    @Column(nullable = false, length = 100)
    private String region;

    protected DireccionEnvio() {
    }

    public DireccionEnvio(String calle, String numero, String comuna, String region) {
        this.calle = limpiar(calle);
        this.numero = limpiar(numero);
        this.comuna = limpiar(comuna);
        this.region = limpiar(region);
        if (!esValida())
            throw new IllegalArgumentException("Se requiere calle, número, comuna y región");
    }

    private static String limpiar(String x) {
        return x == null ? "" : x.trim();
    }

    public boolean esValida() {
        return !calle.isBlank() && !numero.isBlank() && !comuna.isBlank() && !region.isBlank()
                && calle.length() <= 180 && numero.length() <= 25 && comuna.length() <= 100 && region.length() <= 100;
    }

    public String formatearDireccion() {
        return calle + " " + numero + ", " + comuna + ", " + region;
    }

    public Long getId() {
        return id;
    }

    public String getCalle() {
        return calle;
    }

    public String getNumero() {
        return numero;
    }

    public String getComuna() {
        return comuna;
    }

    public String getRegion() {
        return region;
    }
}
