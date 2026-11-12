package com.thequietchapter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "direcciones")
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String calle;
    @Column(nullable = false, length = 30)
    private String numero;
    @Column(nullable = false, length = 100)
    private String comuna;
    @Column(nullable = false, length = 100)
    private String region;
    @Column(nullable = false)
    private boolean predeterminada;

    protected Direccion() {
    }

    public Direccion(String calle, String numero, String comuna, String region) {
        actualizarDatos(calle, numero, comuna, region);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public boolean actualizarDatos(String calle, String numero, String comuna, String region) {
        String a = limpiar(calle), b = limpiar(numero), c = limpiar(comuna), d = limpiar(region);
        if (a.isEmpty() || a.length() > 160 || b.isEmpty() || b.length() > 30 || c.isEmpty() || c.length() > 100
                || d.isEmpty() || d.length() > 100)
            return false;
        this.calle = a;
        this.numero = b;
        this.comuna = c;
        this.region = d;
        return true;
    }

    public boolean esValida() {
        return !limpiar(calle).isEmpty() && !limpiar(numero).isEmpty() && !limpiar(comuna).isEmpty()
                && !limpiar(region).isEmpty();
    }

    void cambiarPredeterminada(boolean valor) {
        this.predeterminada = valor;
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

    public boolean isPredeterminada() {
        return predeterminada;
    }
}
