package com.thequietchapter.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * promo-service lee clasificacion/precios de catalog y ventas de order
 * (popularidad, compras del cliente).
 */
@Component
public class ServiciosHttpClient {
    private static final Logger log = LoggerFactory.getLogger(ServiciosHttpClient.class);
    private final RestClient catalogo, pedidos;
    private final String clave;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LibroInterno(Long id, String titulo, String autores, List<Long> autorIds, Long categoriaId,
            String categoriaNombre,
            Long editorialId, List<Long> generoIds, BigDecimal precio, int stock, boolean activo,
            LocalDateTime fechaRegistro, String portadaUrl) {
        public boolean disponible() {
            return activo && stock > 0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detalle(Long libroId, int cantidad) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pedido(String estado, List<Detalle> detalles) {
    }

    public ServiciosHttpClient(@Value("${tqc.catalog.base-url}") String catalogoUrl,
            @Value("${tqc.orders.base-url}") String pedidosUrl,
            @Value("${tqc.internal.key}") String clave) {
        this.catalogo = cliente(catalogoUrl);
        this.pedidos = cliente(pedidosUrl);
        this.clave = clave;
    }

    private static RestClient cliente(String url) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(5000);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    /**
     * ids vacio = catalogo completo. Estricto: sin catalogo no hay precios ni
     * recomendaciones.
     */
    public List<LibroInterno> libros(Collection<Long> ids) {
        try {
            List<LibroInterno> r = catalogo.get().uri(u -> {
                var b = u.path("/internal/libros");
                if (ids != null && !ids.isEmpty())
                    b.queryParam("ids", ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
                return b.build();
            }).header(Autenticacion.CABECERA_INTERNA, clave).retrieve()
                    .body(new ParameterizedTypeReference<List<LibroInterno>>() {
                    });
            return r == null ? List.of() : r;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo no disponible");
        }
    }

    /**
     * Unidades vendidas por libro (pedidos no cancelados). Best effort: sin
     * order-service, popularidad 0.
     */
    public Map<Long, Integer> unidadesVendidas() {
        try {
            List<Pedido> r = pedidos.get().uri("/internal/pedidos").header(Autenticacion.CABECERA_INTERNA, clave)
                    .retrieve().body(new ParameterizedTypeReference<List<Pedido>>() {
                    });
            Map<Long, Integer> m = new HashMap<>();
            if (r != null)
                r.stream().filter(p -> !"CANCELADO".equals(p.estado())).flatMap(p -> p.detalles().stream())
                        .forEach(d -> m.merge(d.libroId(), d.cantidad(), Integer::sum));
            return m;
        } catch (Exception e) {
            log.warn("order-service no disponible: popularidad en 0");
            return Map.of();
        }
    }

    public Set<Long> librosComprados(Long clienteId) {
        try {
            List<Long> r = pedidos.get().uri("/internal/pedidos/clientes/{id}/libros", clienteId)
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve()
                    .body(new ParameterizedTypeReference<List<Long>>() {
                    });
            return r == null ? Set.of() : new LinkedHashSet<>(r);
        } catch (Exception e) {
            log.warn("order-service no disponible: recomendaciones sin compras");
            return Set.of();
        }
    }
}
