package com.thequietchapter.controller;

import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.model.EstadoReporte;
import com.thequietchapter.model.EstadoResena;
import com.thequietchapter.model.OrdenResenas;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.security.Identidad;
import com.thequietchapter.service.SocialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

/**
 * DC-06. Favoritos y opiniones: cliente autenticado (JWT). Lectura de resenas:
 * publica. Moderacion: ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api")
public class SocialController {
    private final SocialService service;
    private final Autenticacion auth;

    public SocialController(SocialService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    private Long cliente(String a) {
        return auth.requerida(a).usuarioId();
    }

    // ---------------- favoritos
    @GetMapping("/favoritos")
    public List<FavoritoRespuesta> favoritos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.favoritos(cliente(a));
    }

    /** HU-078: ids para pintar el indicador en el catalogo. */
    @GetMapping("/favoritos/ids")
    public Set<Long> ids(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.idsFavoritos(cliente(a));
    }

    @PostMapping("/favoritos/{libroId}")
    public List<FavoritoRespuesta> agregar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId) {
        return service.agregarFavorito(cliente(a), libroId);
    }

    @DeleteMapping("/favoritos/{libroId}")
    public List<FavoritoRespuesta> quitar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId) {
        return service.quitarFavorito(cliente(a), libroId);
    }

    // ---------------- resenas y calificaciones
    @GetMapping("/resenas/libros/{libroId}")
    public OpinionesLibro opiniones(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @RequestParam(required = false) OrdenResenas orden,
            @RequestParam(required = false) Integer puntuacion) {
        Long yo = auth.opcional(a).map(Identidad::usuarioId).orElse(null);
        return service.opiniones(libroId, orden, puntuacion, yo);
    }

    @GetMapping("/resenas/promedios")
    public List<Promedio> promedios(@RequestParam List<Long> libros) {
        return service.promedios(libros);
    }

    @PostMapping("/resenas/libros/{libroId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaRespuesta publicar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @Valid @RequestBody ResenaSolicitud s) {
        return service.publicar(cliente(a), libroId, s.contenido(), s.puntuacion());
    }

    @PutMapping("/resenas/libros/{libroId}/calificacion")
    public OpinionesLibro calificar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @Valid @RequestBody CalificacionSolicitud s) {
        return service.calificar(cliente(a), libroId, s.puntuacion());
    }

    @GetMapping("/resenas/mias")
    public List<ResenaRespuesta> mias(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.misResenas(cliente(a));
    }

    @PutMapping("/resenas/{id}")
    public ResenaRespuesta editar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody EdicionSolicitud s) {
        return service.editar(cliente(a), id, s.contenido());
    }

    @DeleteMapping("/resenas/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        service.eliminarPropia(cliente(a), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resenas/{id}/util")
    public ResenaRespuesta util(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.marcarUtil(cliente(a), id);
    }

    @DeleteMapping("/resenas/{id}/util")
    public ResenaRespuesta quitarUtil(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.quitarUtil(cliente(a), id);
    }

    @PostMapping("/resenas/{id}/reportes")
    public ResponseEntity<Void> reportar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody ReporteSolicitud s) {
        service.reportar(cliente(a), id, s.motivo());
        return ResponseEntity.noContent().build();
    }

    // ---------------- moderacion (ADMINISTRADOR)
    @GetMapping("/resenas/admin/reportadas")
    public List<ResenaRespuesta> reportadas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.reportadas();
    }

    @PostMapping("/resenas/admin/{id}/aprobar")
    public ResenaRespuesta aprobar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.moderar(auth.admin(a).usuarioId(), id, EstadoReporte.APROBADO);
    }

    @PostMapping("/resenas/admin/{id}/eliminar")
    public ResenaRespuesta retirar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.moderar(auth.admin(a).usuarioId(), id, EstadoReporte.ELIMINADO);
    }

    @GetMapping("/resenas/admin")
    public PaginaResenas todas(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) EstadoResena estado, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return service.todas(estado, pagina, tamano);
    }
}
