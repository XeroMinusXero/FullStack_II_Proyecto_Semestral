package com.thequietchapter.client;

import com.thequietchapter.security.Autenticacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consulta a order-service si un libro tiene pedidos (regla de eliminacion). Si
 * no responde, NO se elimina.
 */
@Component
public class PedidosHttpClient {
    private final RestClient client;
    private final String clave;

    public PedidosHttpClient(@Value("${tqc.orders.base-url}") String baseUrl,
            @Value("${tqc.internal.key}") String clave) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(3000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
        this.clave = clave;
    }

    public boolean libroTienePedidos(Long libroId) {
        try {
            Boolean r = client.get().uri("/internal/pedidos/libros/{id}/existe", libroId)
                    .header(Autenticacion.CABECERA_INTERNA, clave).retrieve().body(Boolean.class);
            return r == null || r;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar si el libro tiene pedidos (order-service no disponible)");
        }
    }
}
