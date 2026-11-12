package com.thequietchapter.dto;

import com.thequietchapter.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Contrato seguro para React: nunca serializa entidades Hibernate. */
public record CheckoutRespuesta(Long id, Long clienteId, Long carritoId, EstadoCheckout estado,
        Direccion direccion, Envio envio, Medio medioPago, Resumen resumen,
        List<IntentoPago> intentosPago, String numeroPedidoConfirmado, boolean puedePagar) {
    public record Direccion(Long id, String calle, String numero, String comuna, String region, String formateada) {
        public static Direccion desde(DireccionEnvio d) {
            return d == null ? null
                    : new Direccion(d.getId(), d.getCalle(), d.getNumero(), d.getComuna(), d.getRegion(),
                            d.formatearDireccion());
        }
    }

    public record Envio(Long id, String nombre, BigDecimal costo, int plazoEstimadoDias) {
        public static Envio desde(MetodoEnvio m, ResumenCompra r) {
            return m == null ? null
                    : new Envio(m.getId(), m.getNombre(), r != null ? r.getCostoEnvio() : m.getCostoBase(),
                            m.getPlazoEstimadoDias());
        }
    }

    public record Medio(Long id, String nombre) {
        public static Medio desde(MedioPago m) {
            return m == null ? null : new Medio(m.getId(), m.getNombre());
        }
    }

    public record Linea(Long libroId, String titulo, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        public static Linea desde(LineaCompra l) {
            return new Linea(l.getLibroId(), l.getTitulo(), l.getCantidad(), l.getPrecioUnitario(),
                    l.calcularSubtotal());
        }
    }

    /**
     * impuestos = impuestos adicionales (0: precios con IVA incluido); ivaIncluido
     * = IVA contenido en el total.
     */
    public record Resumen(List<Linea> lineas, BigDecimal subtotal, BigDecimal costoEnvio,
            BigDecimal impuestos, BigDecimal descuento, BigDecimal total, BigDecimal ivaIncluido) {
        public static Resumen desde(ResumenCompra r) {
            return r == null ? null
                    : new Resumen(
                            r.getLineas().stream().map(Linea::desde).toList(), r.calcularSubtotal(), r.getCostoEnvio(),
                            r.getImpuestos(), r.getDescuento(), r.calcularTotal(), r.calcularIvaIncluido());
        }
    }

    public record IntentoPago(Long id, BigDecimal monto, EstadoPago estado, LocalDateTime fecha, String referencia,
            String motivoRechazo) {
        public static IntentoPago desde(Pago p) {
            return new IntentoPago(p.getId(), p.getMonto(), p.getEstado(), p.getFecha(), p.getReferencia(),
                    p.getMotivoRechazo());
        }
    }

    public static CheckoutRespuesta desde(ProcesoCompra p) {
        return new CheckoutRespuesta(
                p.getId(), p.getClienteId(), p.getCarritoId(), p.getEstado(), Direccion.desde(p.getDireccionEnvio()),
                Envio.desde(p.getMetodoEnvio(), p.getResumen()), Medio.desde(p.getMedioPago()),
                Resumen.desde(p.getResumen()), p.getIntentosPago().stream().map(IntentoPago::desde).toList(),
                p.getNumeroPedidoConfirmado(), p.puedePagar());
    }
}
