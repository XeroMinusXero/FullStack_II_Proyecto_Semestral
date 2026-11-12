package com.thequietchapter.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/** Reglas compartidas por Cupon y Promocion (pesos chilenos: sin decimales). */
final class Descuentos {
    private Descuentos() {
    }

    static BigDecimal validarValor(TipoDescuento tipo, BigDecimal valor) {
        if (tipo == null || valor == null)
            throw new IllegalArgumentException("Tipo y valor de descuento obligatorios");
        if (tipo == TipoDescuento.PORCENTAJE
                && (valor.compareTo(BigDecimal.ONE) < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0))
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        if (tipo == TipoDescuento.MONTO_FIJO && valor.signum() <= 0)
            throw new IllegalArgumentException("El monto fijo debe ser mayor a cero");
        return valor;
    }

    static void validarVigencia(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null)
            throw new IllegalArgumentException("Fecha de inicio y fin obligatorias");
        if (!inicio.isBefore(fin))
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
    }

    static BigDecimal calcular(TipoDescuento tipo, BigDecimal valor, BigDecimal base) {
        if (base == null || base.signum() <= 0)
            return BigDecimal.ZERO;
        BigDecimal d = tipo == TipoDescuento.PORCENTAJE
                ? base.multiply(valor).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : valor.setScale(0, RoundingMode.HALF_UP);
        return d.min(base).setScale(2, RoundingMode.HALF_UP);
    }
}
