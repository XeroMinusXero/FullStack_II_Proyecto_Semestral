package com.thequietchapter.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Cart Service es dueño de su base; Checkout consume solo HTTP interno
 * (X-TQC-Internal-Key).
 */
@Component
public class CarritoHttpClient {
    private static final Logger log = LoggerFactory.getLogger(CarritoHttpClient.class);
    private final RestClient client;
    private final String clave;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Long libroId, String titulo, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
            boolean disponible, int stockDisponible) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CarritoRemoto(Long id, Long clienteId, boolean activo, List<Item> items, BigDecimal subtotal,
            Long cuponAplicadoId, String codigoCuponAplicado, BigDecimal descuentoCupon, BigDecimal total,
            boolean puedeComprar,
            List<Aviso> avisos) {
        public BigDecimal descuento() {
            return descuentoCupon == null ? BigDecimal.ZERO : descuentoCupon;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Aviso(Long libroId, String tipo, String mensaje, boolean bloqueante) {
    }

    public CarritoHttpClient(@Value("${tqc.cart.base-url}") String baseUrl,
            @Value("${tqc.internal.key}") String clave) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2500);
        factory.setReadTimeout(6000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.clave = clave;
    }

    /**
     * Carrito vigente del cliente, revisado contra catalogo, y verificado como
     * comprable.
     */
    public CarritoRemoto obtenerCliente(Long clienteId, Long carritoId) {
        CarritoRemoto c;
        try {
            c = client.get().uri("/internal/carritos/clientes/{id}", clienteId)
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve().body(CarritoRemoto.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el carrito", e);
        }
        if (c == null || c.id() == null || !c.id().equals(carritoId) || !clienteId.equals(c.clienteId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El carrito no pertenece a este cliente o ya cambio");
        if (!c.activo() || c.items() == null || c.items().isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El carrito esta vacio");
        if (!c.puedeComprar()
                || c.items().stream().anyMatch(i -> !i.disponible() || i.stockDisponible() < i.cantidad()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Hay libros sin stock suficiente en tu carrito: revisalo antes de pagar");
        return c;
    }

    /**
     * Tras crear el pedido: el carrito se vacia. Si falla, el pedido ya existe y
     * solo se registra el aviso.
     */
    public void vaciar(Long clienteId, Long carritoId) {
        try {
            client.post()
                    .uri(u -> u.path("/internal/carritos/clientes/{id}/vaciar").queryParam("carritoId", carritoId)
                            .build(clienteId))
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo vaciar el carrito {} del cliente {} tras la compra", carritoId, clienteId);
        }
    }
}
