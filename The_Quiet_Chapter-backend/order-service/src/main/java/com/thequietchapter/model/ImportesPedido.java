package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Snapshot de importes del checkout, NO recálculo del precio vigente del
 * catálogo.
 */
@Embeddable
public class ImportesPedido {
    @Column(name = "importe_subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
    @Column(name = "importe_envio", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoEnvio;
    @Column(name = "importe_impuestos", nullable = false, precision = 14, scale = 2)
    private BigDecimal impuestos;
    @Column(name = "importe_descuento", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento;

    protected ImportesPedido() {
    }

    public ImportesPedido(BigDecimal subtotal, BigDecimal costoEnvio, BigDecimal impuestos, BigDecimal descuento) {
        this.subtotal = dinero(subtotal, "subtotal");
        this.costoEnvio = dinero(costoEnvio, "envío");
        this.impuestos = dinero(impuestos, "impuestos");
        this.descuento = dinero(descuento, "descuento");
        if (calcularTotal().signum() < 0)
            throw new IllegalArgumentException("El descuento supera los importes de la compra");
    }

    private static BigDecimal dinero(BigDecimal importe, String campo) {
        if (importe == null || importe.signum() < 0)
            throw new IllegalArgumentException("Importe inválido: " + campo);
        return importe.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        return subtotal.add(costoEnvio).add(impuestos).subtract(descuento);
    }

    /**
     * Precios con IVA incluido (19%): IVA contenido en el total, solo informativo.
     */
    public BigDecimal calcularIvaIncluido() {
        return calcularTotal().multiply(new BigDecimal("19")).divide(new BigDecimal("119"), 2, RoundingMode.HALF_UP);
    }

    public boolean esConsistente(List<DetallePedido> detalles) {
        return detalles != null && !detalles.isEmpty() && subtotal.compareTo(
                detalles.stream().map(DetallePedido::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)) == 0;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
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
