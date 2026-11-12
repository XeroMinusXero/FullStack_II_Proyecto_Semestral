package com.thequietchapter.service;

import com.thequietchapter.model.CondicionLibroCarrito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter REST: Cart Service no conoce la BD de catalogo. UNA llamada por
 * operacion (todas las condiciones en lote).
 */
@Component
public class CatalogoHttpClient {
    private final RestClient client;
    private final PromocionesHttpClient promociones;

    public record CondicionRemota(Long id, String titulo, BigDecimal precio, int stock, boolean activo,
            String portadaUrl) {
        public CondicionLibroCarrito comoDominio() {
            if (precio == null || id == null)
                throw new IllegalStateException("Respuesta de catalogo incompleta");
            return new CondicionLibroCarrito(id, precio, Math.max(0, stock), activo);
        }
    }

    public CatalogoHttpClient(@Value("${tqc.catalog.base-url}") String baseUrl, PromocionesHttpClient promociones) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2500);
        factory.setReadTimeout(3500);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.promociones = promociones;
    }

    /**
     * Condicion vigente de los libros que EXISTEN; precio con promocion activa si
     * promo-service responde.
     */
    private Map<Long, CondicionRemota> remotas(Collection<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        List<CondicionRemota> lista;
        try {
            String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            lista = client.get().uri(u -> u.path("/api/libros/condiciones").queryParam("ids", csv).build())
                    .retrieve().body(new ParameterizedTypeReference<List<CondicionRemota>>() {
                    });
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog Service no disponible", e);
        }
        Map<Long, CondicionRemota> porId = new LinkedHashMap<>();
        if (lista != null)
            lista.forEach(r -> porId.put(r.id(), r));
        Map<Long, BigDecimal> promo = promociones.preciosFinales(porId.values().stream()
                .filter(CondicionRemota::activo)
                .collect(Collectors.toMap(CondicionRemota::id, CondicionRemota::precio)));
        promo.forEach((id, precio) -> {
            CondicionRemota r = porId.get(id);
            if (r != null && precio != null && precio.signum() > 0 && precio.compareTo(r.precio()) < 0)
                porId.put(id, new CondicionRemota(r.id(), r.titulo(), precio, r.stock(), r.activo(), r.portadaUrl()));
        });
        return porId;
    }

    /**
     * Para revisar carritos: un libro eliminado del catalogo se trata como inactivo
     * (HU-020).
     */
    public Map<Long, CondicionRemota> consultar(Collection<Long> ids, Map<Long, BigDecimal> preciosAnteriores) {
        Map<Long, CondicionRemota> porId = remotas(ids);
        Map<Long, CondicionRemota> resultado = new LinkedHashMap<>();
        for (Long id : ids)
            resultado.put(id, porId.getOrDefault(id,
                    new CondicionRemota(id, "Libro no disponible", preciosAnteriores.getOrDefault(id, BigDecimal.ONE),
                            0, false, null)));
        return resultado;
    }

    /** Un libro puntual que DEBE existir (agregar al carrito). */
    public CondicionRemota consultarExistente(Long libroId) {
        CondicionRemota r = remotas(List.of(libroId)).get(libroId);
        if (r == null)
            throw new NoSuchElementException("No existe el libro " + libroId + " en catalogo");
        return r;
    }
}
