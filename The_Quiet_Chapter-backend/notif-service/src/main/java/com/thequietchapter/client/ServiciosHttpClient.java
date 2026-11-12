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
import java.util.*;
import java.util.stream.Collectors;

/** notif-service resuelve correos (user) y stock vigente (catalog). */
@Component
public class ServiciosHttpClient {
    private final RestClient usuarios, catalogo;
    private final String clave;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioBasico(Long id, String nombre, String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condicion(Long id, String titulo, int stock, boolean activo) {
    }

    public ServiciosHttpClient(@Value("${tqc.users.base-url}") String usuariosUrl,
            @Value("${tqc.catalog.base-url}") String catalogoUrl,
            @Value("${tqc.internal.key}") String clave) {
        this.usuarios = cliente(usuariosUrl);
        this.catalogo = cliente(catalogoUrl);
        this.clave = clave;
    }

    private static RestClient cliente(String url) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(3000);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

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
                r.forEach(u -> m.put(u.id(), u));
            return m;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "user-service no disponible");
        }
    }

    public String correo(Long clienteId) {
        UsuarioBasico u = usuarios(List.of(clienteId)).get(clienteId);
        if (u == null)
            throw new NoSuchElementException("Cliente no encontrado");
        return u.email();
    }

    public Condicion condicion(Long libroId) {
        try {
            List<Condicion> r = catalogo.get()
                    .uri(u -> u.path("/api/libros/condiciones").queryParam("ids", libroId).build())
                    .retrieve().body(new ParameterizedTypeReference<List<Condicion>>() {
                    });
            if (r == null || r.isEmpty())
                throw new NoSuchElementException("Libro no encontrado");
            return r.get(0);
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo no disponible");
        }
    }
}
