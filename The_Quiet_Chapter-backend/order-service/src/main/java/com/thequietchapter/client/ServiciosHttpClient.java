package com.thequietchapter.client;

import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Llamadas internas de order-service a catalog (stock), user (nombres/correos)
 * y notif (avisos).
 */
@Component
public class ServiciosHttpClient {
    private static final Logger log = LoggerFactory.getLogger(ServiciosHttpClient.class);
    private final RestClient catalogo, usuarios, notificaciones;
    private final String clave;

    public record UsuarioBasico(Long id, String nombreCompleto, String email) {
    }

    public record Linea(Long libroId, int cantidad) {
    }

    public ServiciosHttpClient(@Value("${tqc.catalog.base-url}") String catalogoUrl,
            @Value("${tqc.users.base-url}") String usuariosUrl,
            @Value("${tqc.notif.base-url}") String notifUrl, @Value("${tqc.internal.key}") String clave) {
        this.catalogo = cliente(catalogoUrl, 4000);
        this.usuarios = cliente(usuariosUrl, 2500);
        this.notificaciones = cliente(notifUrl, 2500);
        this.clave = clave;
    }

    private static RestClient cliente(String url, int lectura) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(lectura);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    /**
     * Estricto: si catalogo no repone, la cancelacion se revierte (no queda stock
     * "perdido").
     */
    public void reponerStock(String referencia, List<Linea> lineas) {
        try {
            catalogo.post().uri("/internal/libros/stock/reponer").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("referencia", referencia, "lineas", lineas))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo reponer el stock en catalogo; intenta nuevamente");
        }
    }

    /**
     * Best effort: si user-service no responde, el listado se muestra sin nombres.
     */
    public Map<Long, UsuarioBasico> usuarios(Collection<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        try {
            String csv = ids.stream().distinct().map(String::valueOf).collect(Collectors.joining(","));
            List<UsuarioBasico> r = usuarios.get().uri(u -> u.path("/internal/usuarios").queryParam("ids", csv).build())
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve()
                    .body(new ParameterizedTypeReference<List<UsuarioBasico>>() {
                    });
            Map<Long, UsuarioBasico> m = new HashMap<>();
            if (r != null)
                r.forEach(x -> m.put(x.id(), x));
            return m;
        } catch (Exception e) {
            log.warn("user-service no disponible para nombres de clientes");
            return Map.of();
        }
    }

    /** Best effort (DC-08): una falla de notif-service nunca revierte un pedido. */
    public void notificar(Long clienteId, String tipo, String asunto, String mensaje) {
        try {
            UsuarioBasico u = usuarios(List.of(clienteId)).get(clienteId);
            if (u == null)
                return;
            notificaciones.post().uri("/internal/notificaciones").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("destinatario", u.email(), "tipo", tipo, "asunto", asunto, "mensaje", mensaje))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("notif-service no disponible ({})", tipo);
        }
    }
}
