package com.thequietchapter.service;

import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

/**
 * Integracion con promo-service (DC-07): precios con promocion (best effort) y
 * validacion de cupones (estricta).
 */
@Component
public class PromocionesHttpClient {
    private static final Logger log = LoggerFactory.getLogger(PromocionesHttpClient.class);
    private final RestClient client;
    private final String clave;

    public record PrecioPromo(Long libroId, BigDecimal precioFinal) {
    }

    public record CuponValidado(Long cuponId, String codigo, BigDecimal descuento) {
    }

    public PromocionesHttpClient(@Value("${tqc.promo.base-url}") String baseUrl,
            @Value("${tqc.internal.key}") String clave) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(1500);
        f.setReadTimeout(2500);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
        this.clave = clave;
    }

    /**
     * Si promo-service no responde se usa el precio de lista: el carrito NUNCA se
     * cae por promociones.
     */
    public Map<Long, BigDecimal> preciosFinales(Map<Long, BigDecimal> preciosLista) {
        if (preciosLista.isEmpty())
            return Map.of();
        try {
            List<Map<String, Object>> cuerpo = preciosLista.entrySet().stream()
                    .map(e -> Map.<String, Object>of("libroId", e.getKey(), "precio", e.getValue())).toList();
            List<PrecioPromo> r = client.post().uri("/internal/promociones/precios")
                    .header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve()
                    .body(new ParameterizedTypeReference<List<PrecioPromo>>() {
                    });
            Map<Long, BigDecimal> m = new HashMap<>();
            if (r != null)
                r.forEach(p -> m.put(p.libroId(), p.precioFinal()));
            return m;
        } catch (Exception e) {
            log.debug("promo-service no disponible; se usan precios de lista");
            return Map.of();
        }
    }

    public CuponValidado validarCupon(String codigo, BigDecimal subtotal) {
        try {
            return client.post().uri("/internal/cupones/validar").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("codigo", codigo, "subtotal", subtotal))
                    .retrieve().body(CuponValidado.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Cupon no valido: "
                    + e.getResponseBodyAsString().replaceAll("[{}\"]", "").replace("error:", "").trim());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar el cupon en este momento");
        }
    }
}
