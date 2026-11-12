package com.thequietchapter.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thequietchapter.security.Autenticacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * report-service NO tiene BD: lee order (ventas), catalog (libros y
 * movimientos) y user (clientes) por HTTP interno.
 */
@Component
public class DatosHttpClient {
    private final RestClient pedidos, catalogo, usuarios;
    private final String clave;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detalle(Long libroId, String tituloLibro, int cantidad, BigDecimal precioUnitario) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Importes(BigDecimal total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pedido(Long id, String numeroPedido, Long clienteId, LocalDateTime fechaCreacion, String estado,
            List<Detalle> detalles, Importes importes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Libro(Long id, String titulo, String autores, Long categoriaId, String categoriaNombre,
            BigDecimal precio,
            int stock, boolean activo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Movimiento(Long movimientoId, Long libroId, String tituloLibro, LocalDateTime fecha, String tipo,
            int cantidad, Long responsableId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usuario(Long id, String nombreCompleto, String email, LocalDateTime fechaRegistro) {
    }

    public DatosHttpClient(@Value("${tqc.orders.base-url}") String pedidosUrl,
            @Value("${tqc.catalog.base-url}") String catalogoUrl,
            @Value("${tqc.users.base-url}") String usuariosUrl, @Value("${tqc.internal.key}") String clave) {
        this.pedidos = cliente(pedidosUrl);
        this.catalogo = cliente(catalogoUrl);
        this.usuarios = cliente(usuariosUrl);
        this.clave = clave;
    }

    private static RestClient cliente(String url) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(8000);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    private <T> List<T> leer(RestClient c, String servicio,
            java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uri,
            ParameterizedTypeReference<List<T>> tipo) {
        try {
            List<T> r = c.get().uri(uri).header(Autenticacion.CABECERA_INTERNA, clave).retrieve().body(tipo);
            return r == null ? List.of() : r;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    servicio + " no disponible: no se puede generar el reporte");
        }
    }

    public List<Pedido> pedidos(LocalDate desde, LocalDate hasta) {
        return leer(pedidos, "order-service",
                u -> u.path("/internal/pedidos").queryParam("desde", desde).queryParam("hasta", hasta).build(),
                new ParameterizedTypeReference<List<Pedido>>() {
                });
    }

    public List<Libro> libros() {
        return leer(catalogo, "catalog-service", u -> u.path("/internal/libros").build(),
                new ParameterizedTypeReference<List<Libro>>() {
                });
    }

    public List<Movimiento> movimientos(LocalDate desde, LocalDate hasta) {
        return leer(catalogo, "catalog-service",
                u -> u.path("/internal/libros/movimientos").queryParam("desde", desde).queryParam("hasta", hasta)
                        .build(),
                new ParameterizedTypeReference<List<Movimiento>>() {
                });
    }

    public List<Usuario> clientes() {
        return leer(usuarios, "user-service", u -> u.path("/internal/usuarios/clientes").build(),
                new ParameterizedTypeReference<List<Usuario>>() {
                });
    }

    public Map<Long, String> nombres(Collection<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        String csv = ids.stream().distinct().map(String::valueOf).collect(Collectors.joining(","));
        Map<Long, String> m = new HashMap<>();
        leer(usuarios, "user-service", u -> u.path("/internal/usuarios").queryParam("ids", csv).build(),
                new ParameterizedTypeReference<List<Usuario>>() {
                }).forEach(x -> m.put(x.id(), x.nombreCompleto()));
        return m;
    }
}
