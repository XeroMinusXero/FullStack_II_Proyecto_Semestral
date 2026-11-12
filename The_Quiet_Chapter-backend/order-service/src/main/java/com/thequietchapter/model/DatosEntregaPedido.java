package com.thequietchapter.model;

import jakarta.persistence.*;

/**
 * Foto histórica de la dirección y método de despacho: no depende de User
 * Service.
 */
@Embeddable
public class DatosEntregaPedido {
    @Column(name = "entrega_calle", nullable = false, length = 150)
    private String calle;
    @Column(name = "entrega_numero", nullable = false, length = 40)
    private String numero;
    @Column(name = "entrega_comuna", nullable = false, length = 120)
    private String comuna;
    @Column(name = "entrega_region", nullable = false, length = 120)
    private String region;
    @Column(name = "entrega_metodo", nullable = false, length = 120)
    private String metodoEnvio;
    @Column(name = "entrega_plazo_dias", nullable = false)
    private int plazoEstimadoDias;
    @Column(name = "entrega_seguimiento", length = 120)
    private String numeroSeguimiento;

    protected DatosEntregaPedido() {
    }

    public DatosEntregaPedido(String calle, String numero, String comuna, String region, String metodoEnvio,
            int plazoEstimadoDias) {
        this.calle = exigir(calle, "calle", 150);
        this.numero = exigir(numero, "número", 40);
        this.comuna = exigir(comuna, "comuna", 120);
        this.region = exigir(region, "región", 120);
        this.metodoEnvio = exigir(metodoEnvio, "método de envío", 120);
        if (plazoEstimadoDias < 0)
            throw new IllegalArgumentException("Plazo de entrega inválido");
        this.plazoEstimadoDias = plazoEstimadoDias;
    }

    private static String exigir(String texto, String nombre, int max) {
        if (texto == null || texto.isBlank() || texto.trim().length() > max)
            throw new IllegalArgumentException("Campo inválido: " + nombre);
        return texto.trim();
    }

    public String formatearDireccion() {
        return calle + " " + numero + ", " + comuna + ", " + region;
    }

    public void registrarSeguimiento(String numero) {
        this.numeroSeguimiento = exigir(numero, "seguimiento", 120);
    }

    public boolean tieneSeguimiento() {
        return numeroSeguimiento != null && !numeroSeguimiento.isBlank();
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

    public String getMetodoEnvio() {
        return metodoEnvio;
    }

    public int getPlazoEstimadoDias() {
        return plazoEstimadoDias;
    }

    public String getNumeroSeguimiento() {
        return numeroSeguimiento;
    }
}
