package com.thequietchapter.dto;

import com.thequietchapter.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTO estable para React: no expone entidades ni proxies de Hibernate. */
public record PedidoRespuesta(Long id, String numeroPedido, Long clienteId, LocalDateTime fechaCreacion,
        EstadoPedido estado, List<Detalle> detalles, Entrega entrega, Importes importes,
        String medioPago, String referenciaPago, boolean puedeCancelarse, String clienteNombre, String clienteEmail) {
    public record Detalle(Long id, Long libroId, String tituloLibro, int cantidad, BigDecimal precioUnitario,
            BigDecimal subtotal) {
        static Detalle desde(DetallePedido d) {
            return new Detalle(d.getId(), d.getLibroId(), d.getTituloLibro(), d.getCantidad(), d.getPrecioUnitario(),
                    d.calcularSubtotal());
        }
    }

    public record Entrega(String calle, String numero, String comuna, String region, String metodoEnvio,
            int plazoEstimadoDias, String numeroSeguimiento, String direccionFormateada) {
        static Entrega desde(DatosEntregaPedido d) {
            return new Entrega(d.getCalle(), d.getNumero(), d.getComuna(), d.getRegion(),
                    d.getMetodoEnvio(), d.getPlazoEstimadoDias(), d.getNumeroSeguimiento(), d.formatearDireccion());
        }
    }

    public record Importes(BigDecimal subtotal, BigDecimal costoEnvio, BigDecimal impuestos, BigDecimal descuento,
            BigDecimal total, BigDecimal ivaIncluido) {
        static Importes desde(ImportesPedido i) {
            return new Importes(i.getSubtotal(), i.getCostoEnvio(), i.getImpuestos(), i.getDescuento(),
                    i.calcularTotal(), i.calcularIvaIncluido());
        }
    }

    public static PedidoRespuesta desde(Pedido p) {
        return new PedidoRespuesta(p.getId(), p.getNumeroPedido(), p.getClienteId(), p.getFechaCreacion(),
                p.getEstado(), p.getDetalles().stream().map(Detalle::desde).toList(), Entrega.desde(p.getEntrega()),
                Importes.desde(p.getImportes()), p.getMedioPago(), p.getReferenciaPago(), p.puedeCancelarse(), null,
                null);
    }

    /**
     * Vista administrativa: agrega nombre y correo del cliente (HU listado/detalle
     * admin).
     */
    public PedidoRespuesta conCliente(String nombre, String email) {
        return new PedidoRespuesta(id, numeroPedido, clienteId, fechaCreacion, estado, detalles, entrega, importes,
                medioPago, referenciaPago, puedeCancelarse, nombre, email);
    }
}
