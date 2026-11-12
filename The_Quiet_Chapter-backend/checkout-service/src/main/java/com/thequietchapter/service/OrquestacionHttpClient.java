package com.thequietchapter.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Pasos remotos de la compra (HU-028): stock en catalog, pedido en order y
 * canje de cupon en promo.
 */
@Component
public class OrquestacionHttpClient {
    private static final Logger log = LoggerFactory.getLogger(OrquestacionHttpClient.class);
    private final RestClient catalogo, pedidos, promo, notif;
    private final String clave;

    public record Linea(Long libroId, int cantidad) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PedidoCreado(Long id, String numeroPedido) {
    }

    /** Falla de negocio de un paso remoto, con mensaje para el cliente. */
    public static class PasoFallido extends RuntimeException {
        public PasoFallido(String m) {
            super(m);
        }
    }

    public OrquestacionHttpClient(@Value("${tqc.catalog.base-url}") String catalogoUrl,
            @Value("${tqc.orders.base-url}") String pedidosUrl,
            @Value("${tqc.promo.base-url}") String promoUrl, @Value("${tqc.notif.base-url}") String notifUrl,
            @Value("${tqc.internal.key}") String clave) {
        this.catalogo = cliente(catalogoUrl);
        this.pedidos = cliente(pedidosUrl);
        this.promo = cliente(promoUrl);
        this.notif = cliente(notifUrl);
        this.clave = clave;
    }

    private static RestClient cliente(String url) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2500);
        f.setReadTimeout(6000);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    private static String mensaje(HttpClientErrorException e, String porDefecto) {
        String cuerpo = e.getResponseBodyAsString();
        int i = cuerpo.indexOf("\"error\":\"");
        if (i >= 0) {
            int fin = cuerpo.indexOf('"', i + 9);
            if (fin > i)
                return cuerpo.substring(i + 9, fin);
        }
        return porDefecto;
    }

    public void descontarStock(String referencia, List<Linea> lineas) {
        try {
            catalogo.post().uri("/internal/libros/stock/descontar").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("referencia", referencia, "lineas", lineas))
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new PasoFallido(mensaje(e, "Stock insuficiente para completar la compra"));
        } catch (Exception e) {
            throw new PasoFallido("El catalogo no esta disponible; no se realizo ningun cobro");
        }
    }

    /**
     * Compensacion: devuelve el stock si un paso posterior fallo. Idempotente en
     * catalog por referencia.
     */
    public void reponerStock(String referencia, List<Linea> lineas) {
        try {
            catalogo.post().uri("/internal/libros/stock/reponer").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("referencia", referencia, "lineas", lineas))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.error("COMPENSACION PENDIENTE: no se pudo reponer stock de la referencia {}", referencia);
        }
    }

    public PedidoCreado crearPedido(Map<String, Object> solicitud) {
        try {
            PedidoCreado p = pedidos.post().uri("/internal/pedidos").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(solicitud).retrieve().body(PedidoCreado.class);
            if (p == null || p.numeroPedido() == null)
                throw new PasoFallido("Order Service no devolvio el pedido");
            return p;
        } catch (HttpClientErrorException e) {
            throw new PasoFallido(mensaje(e, "No se pudo registrar el pedido"));
        } catch (PasoFallido e) {
            throw e;
        } catch (Exception e) {
            throw new PasoFallido("Order Service no disponible; no se realizo ningun cobro");
        }
    }

    /**
     * Best effort: registra AplicacionPromocion por cada linea con promocion
     * vigente (DC-07).
     */
    public void registrarAplicaciones(Long pedidoId, List<Linea> lineas) {
        try {
            promo.post().uri("/internal/promociones/aplicaciones").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("pedidoId", pedidoId, "lineas", lineas))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudieron registrar aplicaciones de promociones del pedido {}", pedidoId);
        }
    }

    /** Best effort: registra el canje del cupon (DC-07 CanjeCupon). */
    public void registrarCanje(Long cuponId, Long clienteId, Long pedidoId, BigDecimal descuento) {
        if (cuponId == null)
            return;
        try {
            promo.post().uri("/internal/cupones/{id}/canjes", cuponId).header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("clienteId", clienteId, "pedidoId", pedidoId, "descuento", descuento))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo registrar el canje del cupon {} del pedido {}", cuponId, pedidoId);
        }
    }

    /**
     * HU-125: correo con el resultado del pago (best effort; notif-service resuelve
     * el correo del cliente).
     */
    public void notificarPago(Long clienteId, String asunto, String mensaje) {
        try {
            notif.post().uri("/internal/notificaciones").header(Autenticacion.CABECERA_INTERNA, clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("clienteId", clienteId, "tipo", "RESULTADO_PAGO", "asunto", asunto, "mensaje",
                            mensaje))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("notif-service no disponible (RESULTADO_PAGO cliente {})", clienteId);
        }
    }
}
