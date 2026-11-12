package com.thequietchapter.client;

import com.thequietchapter.security.Autenticacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

/**
 * Envio "best effort" a notif-service: si esta caido, la operacion del usuario
 * NO falla (solo se registra en log).
 */
@Component
public class NotificacionesHttpClient {
  private static final Logger log = LoggerFactory.getLogger(NotificacionesHttpClient.class);
  private final RestClient client;
  private final String clave;

  public NotificacionesHttpClient(@Value("${tqc.notif.base-url}") String baseUrl,
      @Value("${tqc.internal.key}") String clave) {
    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(1500);
    f.setReadTimeout(2500);
    this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
    this.clave = clave;
  }

  public void enviar(String destinatario, String tipo, String asunto, String mensaje) {
    try {
      client.post().uri("/internal/notificaciones").header(Autenticacion.CABECERA_INTERNA, clave)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("destinatario", destinatario, "tipo", tipo, "asunto", asunto, "mensaje", mensaje))
          .retrieve().toBodilessEntity();
    } catch (Exception e) {
      log.warn("notif-service no disponible ({}): {} para {}", tipo, asunto, destinatario);
    }
  }
}
