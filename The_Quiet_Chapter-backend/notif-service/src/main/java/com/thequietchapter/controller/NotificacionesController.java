package com.thequietchapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.model.EstadoBoletin;
import com.thequietchapter.model.EstadoSuscripcion;
import com.thequietchapter.model.TipoNotificacion;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.NotificacionesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * DC-08. Correo SIMULADO: cada envio queda registrado en la tabla
 * notificaciones (bandeja).
 */
@RestController
@RequestMapping("/api")
public class NotificacionesController {
    private static final String AUTH = HttpHeaders.AUTHORIZATION;
    private final NotificacionesService service;
    private final Autenticacion auth;

    public NotificacionesController(NotificacionesService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    @Operation(summary = "[Cliente] Mis correos", tags = "Notificaciones")
    @GetMapping("/notificaciones/mias")
    public Pagina<NotificacionRespuesta> mias(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        return service.mias(auth.requerida(a).usuarioId(), pagina, tamano);
    }

    @Operation(summary = "[Admin] Bitácora de correos enviados", tags = "Notificaciones")
    @GetMapping("/notificaciones")
    public Pagina<NotificacionRespuesta> todas(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) TipoNotificacion tipo, @RequestParam(required = false) String destinatario,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return service.todas(tipo, destinatario, pagina, tamano);
    }

    // ---------------- avisos de reabastecimiento (HU-131)
    @Operation(summary = "[Cliente] Mis avisos de reabastecimiento", tags = "Notificaciones")
    @GetMapping("/notificaciones/reabastecimiento")
    public List<AvisoRespuesta> misAvisos(@RequestHeader(value = AUTH, required = false) String a) {
        return service.misAvisos(auth.requerida(a).usuarioId());
    }

    @Operation(summary = "[Cliente] Pedir aviso cuando un libro vuelva a tener stock", tags = "Notificaciones")
    @PostMapping("/notificaciones/reabastecimiento/{libroId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AvisoRespuesta pedirAviso(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long libroId) {
        return service.solicitarAviso(auth.requerida(a).usuarioId(), libroId);
    }

    @Operation(summary = "[Cliente] Cancelar un aviso de reabastecimiento", tags = "Notificaciones")
    @DeleteMapping("/notificaciones/reabastecimiento/{libroId}")
    public ResponseEntity<Void> cancelarAviso(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long libroId) {
        service.cancelarAviso(auth.requerida(a).usuarioId(), libroId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- boletin: publico (doble confirmacion por token)
    @Operation(summary = "[Público] Suscribirse al boletín", description = "Envía un correo con el enlace de confirmación (doble opt-in).", tags = "Boletines")
    @PostMapping("/boletines/suscripciones")
    public Mensaje suscribir(@Valid @RequestBody SuscribirSolicitud s) {
        return service.suscribir(s.email());
    }

    @Operation(summary = "[Público] Confirmar la suscripción (token del correo)", tags = "Boletines")
    @PostMapping("/boletines/suscripciones/confirmar")
    public Mensaje confirmar(@Valid @RequestBody TokenSolicitud s) {
        return service.confirmar(s.token());
    }

    @Operation(summary = "[Público] Cancelar la suscripción (token del correo)", tags = "Boletines")
    @PostMapping("/boletines/suscripciones/cancelar")
    public Mensaje cancelar(@Valid @RequestBody TokenSolicitud s) {
        return service.cancelar(s.token());
    }

    // ---------------- boletin: ADMINISTRADOR
    @Operation(summary = "[Admin] Listar suscriptores", tags = "Boletines")
    @GetMapping("/boletines/suscriptores")
    public Pagina<SuscriptorRespuesta> suscriptores(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) EstadoSuscripcion estado, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return service.suscriptores(estado, pagina, tamano);
    }

    @Operation(summary = "[Admin] Listar boletines", tags = "Boletines")
    @GetMapping("/boletines")
    public Pagina<BoletinRespuesta> boletines(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) EstadoBoletin estado, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return service.listarBoletines(estado, pagina, tamano);
    }

    @Operation(summary = "[Admin] Crear un borrador de boletín", tags = "Boletines")
    @PostMapping("/boletines")
    @ResponseStatus(HttpStatus.CREATED)
    public BoletinRespuesta crear(@RequestHeader(value = AUTH, required = false) String a,
            @Valid @RequestBody BoletinSolicitud s) {
        auth.admin(a);
        return service.crearBoletin(s);
    }

    @Operation(summary = "[Admin] Editar un borrador de boletín", tags = "Boletines")
    @PutMapping("/boletines/{id}")
    public BoletinRespuesta editar(@RequestHeader(value = AUTH, required = false) String a, @PathVariable Long id,
            @Valid @RequestBody BoletinSolicitud s) {
        auth.admin(a);
        return service.editarBoletin(id, s);
    }

    @Operation(summary = "[Admin] Enviar un boletín a los suscriptores confirmados", tags = "Boletines")
    @PostMapping("/boletines/{id}/enviar")
    public BoletinRespuesta enviar(@RequestHeader(value = AUTH, required = false) String a, @PathVariable Long id) {
        auth.admin(a);
        return service.enviarBoletin(id);
    }
}
