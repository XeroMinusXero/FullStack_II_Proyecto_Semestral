package com.thequietchapter.dto;

import com.thequietchapter.model.*;
import java.math.BigDecimal;
import java.util.List;

/** Contrato REST para React; no devuelve las entidades JPA. */
public record CarritoRespuesta(Long id, Long clienteId, boolean activo,
        List<ItemRespuesta> items, int unidades, BigDecimal subtotal,
        Long cuponAplicadoId, String codigoCuponAplicado, BigDecimal descuentoCupon, BigDecimal total,
        boolean puedeComprar, boolean exitosa, List<AvisoRespuesta> avisos) {

    public record ItemRespuesta(Long libroId, String titulo, String portadaUrl,
            int cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
            boolean disponible, int stockDisponible) {
    }

    public record AvisoRespuesta(Long libroId, TipoAvisoCarrito tipo,
            String mensaje, boolean bloqueante) {
        public static AvisoRespuesta desde(AvisoCarrito a) {
            return new AvisoRespuesta(a.getLibroId(), a.getTipo(), a.getMensaje(), a.esBloqueante());
        }
    }
}
