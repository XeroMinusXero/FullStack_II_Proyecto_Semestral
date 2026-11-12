package com.thequietchapter.controller;

import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.NotificacionesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Servicio-a-servicio (user, order, checkout, catalog). El gateway no expone
 * /internal/**.
 */
@RestController
@RequestMapping("/internal")
public class NotificacionesInternoController {
    private final NotificacionesService service;
    private final Autenticacion auth;

    public NotificacionesInternoController(NotificacionesService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    @PostMapping("/notificaciones")
    public NotificacionRespuesta enviar(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody EnviarSolicitud s) {
        auth.interna(k);
        return service.enviar(s);
    }

    @PostMapping("/reabastecimiento/libros/{libroId}/disponible")
    public Map<String, Integer> disponible(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long libroId, @RequestBody DisponibleSolicitud s) {
        auth.interna(k);
        return Map.of("avisosEnviados", service.libroDisponible(libroId, s.titulo() == null ? "Libro" : s.titulo()));
    }
}
