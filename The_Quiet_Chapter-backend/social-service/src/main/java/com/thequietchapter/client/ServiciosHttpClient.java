package com.thequietchapter.client;

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
import java.util.*;
import java.util.stream.Collectors;

/**
 * social-service consulta order (compra verificada) y user (nombre del autor).
 */
@Component
public class ServiciosHttpClient {
    private static final Logger log = LoggerFactory.getLogger(ServiciosHttpClient.class);
    private final RestClient pedidos, usuarios;
    private final String clave;

    public record UsuarioBasico(Long id, String nombre, String apellido, String nombreCompleto, String email) {
    }

    public ServiciosHttpClient(@Value("${tqc.orders.base-url}") String pedidosUrl,
            @Value("${tqc.users.base-url}") String usuariosUrl,
            @Value("${tqc.internal.key}") String clave) {
        this.pedidos = cliente(pedidosUrl);
        this.usuarios = cliente(usuariosUrl);
        this.clave = clave;
    }

    private static RestClient cliente(String url) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(3000);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    /**
     * Estricto: sin order-service no se puede verificar la compra, asi que no se
     * publica.
     */
    public Set<Long> librosComprados(Long clienteId) {
        try {
            List<Long> r = pedidos.get().uri("/internal/pedidos/clientes/{id}/libros", clienteId)
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve()
                    .body(new ParameterizedTypeReference<List<Long>>() {
                    });
            return r == null ? Set.of() : new HashSet<>(r);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar tu compra en este momento");
        }
    }

    /** Best effort: sin user-service se muestra "Cliente". */
    public Map<Long, String> nombres(Collection<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        try {
            String csv = ids.stream().distinct().map(String::valueOf).collect(Collectors.joining(","));
            List<UsuarioBasico> r = usuarios.get().uri(u -> u.path("/internal/usuarios").queryParam("ids", csv).build())
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve()
                    .body(new ParameterizedTypeReference<List<UsuarioBasico>>() {
                    });
            Map<Long, String> m = new HashMap<>();
            if (r != null)
                r.forEach(u -> m.put(u.id(), u.nombre() + " "
                        + (u.apellido() == null || u.apellido().isBlank() ? "" : u.apellido().charAt(0) + ".")));
            return m;
        } catch (Exception e) {
            log.warn("user-service no disponible para nombres de autores");
            return Map.of();
        }
    }
}
