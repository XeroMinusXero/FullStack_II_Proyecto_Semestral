package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkout_resumenes")
public class ResumenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "resumen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaCompra> lineas = new ArrayList<>();
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal costoEnvio;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal impuestos;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal descuento;

    protected ResumenCompra() {
    }

    public ResumenCompra(List<LineaCompra> lineas, BigDecimal costoEnvio, BigDecimal impuestos, BigDecimal descuento) {
        if (lineas == null || lineas.isEmpty() || costoEnvio == null || impuestos == null || descuento == null ||
                costoEnvio.signum() < 0 || impuestos.signum() < 0 || descuento.signum() < 0)
            throw new IllegalArgumentException("Resumen incompleto o importes negativos");
        for (LineaCompra l : lineas) {
            if (l == null)
                throw new IllegalArgumentException("Línea nula");
            l.vincularResumen(this);
            this.lineas.add(l);
        }
        this.costoEnvio = costoEnvio.setScale(2, RoundingMode.HALF_UP);
        this.impuestos = impuestos.setScale(2, RoundingMode.HALF_UP);
        this.descuento = descuento.setScale(2, RoundingMode.HALF_UP);
        if (!esValido())
            throw new IllegalArgumentException("El descuento supera el subtotal");
    }

    public BigDecimal calcularSubtotal() {
        return lineas.stream().map(LineaCompra::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2,
                RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        return calcularSubtotal().add(costoEnvio).add(impuestos).subtract(descuento).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Precios de catalogo incluyen IVA (Chile, 19%): se informa el IVA contenido en
     * el total, sin sumarlo otra vez.
     */
    public BigDecimal calcularIvaIncluido() {
        return calcularTotal().multiply(new BigDecimal("19")).divide(new BigDecimal("119"), 2, RoundingMode.HALF_UP);
    }

    public boolean esValido() {
        return !lineas.isEmpty() && calcularTotal().signum() >= 0 && descuento.compareTo(calcularSubtotal()) <= 0;
    }

    public Long getId() {
        return id;
    }

    public List<LineaCompra> getLineas() {
        return List.copyOf(lineas);
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }
}
