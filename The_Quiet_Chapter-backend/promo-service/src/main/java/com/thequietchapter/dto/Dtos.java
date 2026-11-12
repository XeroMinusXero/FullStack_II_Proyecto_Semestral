package com.thequietchapter.dto;

import com.thequietchapter.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos JSON de promo-service. */
public final class Dtos {
    private Dtos() {
    }

    // ---- cupones
    public record CuponSolicitud(@NotBlank @Size(min = 3, max = 40) String codigo, @NotNull TipoDescuento tipoDescuento,
            @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull @DecimalMin("0") BigDecimal montoMinimo,
            @NotNull LocalDateTime fechaInicio, @NotNull LocalDateTime fechaFin) {
    }

    public record CuponRespuesta(Long id, String codigo, TipoDescuento tipoDescuento, BigDecimal valor,
            BigDecimal montoMinimo,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean activo, boolean vigente, int canjes) {
        public static CuponRespuesta desde(Cupon c) {
            LocalDateTime ahora = LocalDateTime.now();
            return new CuponRespuesta(c.getId(), c.getCodigo(), c.getTipoDescuento(), c.getValor(), c.getMontoMinimo(),
                    c.getFechaInicio(), c.getFechaFin(), c.isActivo(),
                    c.isActivo() && !ahora.isBefore(c.getFechaInicio()) && !ahora.isAfter(c.getFechaFin()),
                    c.listarCanjes().size());
        }
    }

    public record CanjeRespuesta(Long id, Long clienteId, Long pedidoId, LocalDateTime fechaCanje,
            BigDecimal montoDescuento) {
        public static CanjeRespuesta desde(CanjeCupon c) {
            return new CanjeRespuesta(c.getId(), c.getClienteId(), c.getPedidoId(), c.getFechaCanje(),
                    c.getMontoDescuento());
        }
    }

    public record EstadoSolicitud(boolean activo) {
    }

    public record ValidarCuponSolicitud(@NotBlank String codigo, @NotNull @DecimalMin("0") BigDecimal subtotal) {
    }

    public record CuponValidado(Long cuponId, String codigo, BigDecimal descuento) {
    }

    public record CanjeSolicitud(@NotNull Long clienteId, @NotNull Long pedidoId,
            @NotNull @DecimalMin("0") BigDecimal descuento) {
    }

    // ---- promociones
    public record AlcanceDto(@NotNull TipoAlcance tipo, @NotNull @Positive Long referenciaId) {
    }

    public record PromocionSolicitud(@NotBlank @Size(max = 120) String nombre, @NotNull TipoDescuento tipoDescuento,
            @NotNull @DecimalMin("0.01") BigDecimal valor, @NotNull LocalDateTime fechaInicio,
            @NotNull LocalDateTime fechaFin, @NotEmpty List<@Valid AlcanceDto> alcances, Boolean activo) {
    }

    public record PromocionRespuesta(Long id, String nombre, TipoDescuento tipoDescuento, BigDecimal valor,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin, boolean activo, boolean vigente, List<AlcanceDto> alcances, int aplicaciones) {
        public static PromocionRespuesta desde(Promocion p) {
            return new PromocionRespuesta(p.getId(), p.getNombre(), p.getTipoDescuento(), p.getValor(),
                    p.getFechaInicio(), p.getFechaFin(),
                    p.isActivo(), p.estaVigente(LocalDateTime.now()),
                    p.getAlcances().stream().map(a -> new AlcanceDto(a.getTipo(), a.getReferenciaId())).toList(),
                    p.contarAplicaciones());
        }
    }

    public record PrecioLista(@NotNull Long libroId, @NotNull BigDecimal precio) {
    }

    public record PrecioPromo(Long libroId, BigDecimal precioOriginal, BigDecimal precioFinal, BigDecimal descuento,
            int porcentaje, String promocion, LocalDateTime vigenteHasta) {
    }

    public record Oferta(Long libroId, String titulo, String autores, String portadaUrl, boolean disponible,
            BigDecimal precioOriginal,
            BigDecimal precioFinal, int porcentaje, String promocion, LocalDateTime vigenteHasta) {
    }

    public record LineaAplicacion(@NotNull Long libroId, @Positive int cantidad) {
    }

    public record AplicacionesSolicitud(@NotNull Long pedidoId, @NotEmpty List<@Valid LineaAplicacion> lineas) {
    }

    // ---- recomendaciones
    public record LibroSugerido(Long id, String titulo, String autores, BigDecimal precio, String portadaUrl,
            boolean disponible, Long categoriaId) {
    }

    public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
    }
}
