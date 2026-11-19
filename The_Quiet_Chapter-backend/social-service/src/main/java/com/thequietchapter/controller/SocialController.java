package com.thequietchapter.controller;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "[Cliente] Mis favoritos", tags = "Favoritos")
    @GetMapping("/favoritos")
    public List<FavoritoRespuesta> favoritos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.favoritos(cliente(a));
    }

    /** HU-078: ids para pintar el indicador en el catalogo. */
    @Operation(summary = "[Cliente] Ids de mis favoritos", tags = "Favoritos")
    @GetMapping("/favoritos/ids")
    public Set<Long> ids(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.idsFavoritos(cliente(a));
    }

    @Operation(summary = "[Cliente] Agregar un libro a favoritos", tags = "Favoritos")
    @PostMapping("/favoritos/{libroId}")
    public List<FavoritoRespuesta> agregar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId) {
        return service.agregarFavorito(cliente(a), libroId);
    }

    @Operation(summary = "[Cliente] Quitar un libro de favoritos", tags = "Favoritos")
    @DeleteMapping("/favoritos/{libroId}")
    public List<FavoritoRespuesta> quitar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId) {
        return service.quitarFavorito(cliente(a), libroId);
    }

    // ---------------- resenas y calificaciones
    @Operation(summary = "[Público] Calificaciones y reseñas de un libro", description = "Con sesión de cliente indica además si puede opinar (puedoOpinar: solo si compró el libro, consultado a order-service).", tags = "Reseñas")
    @GetMapping("/resenas/libros/{libroId}")
    public OpinionesLibro opiniones(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @RequestParam(required = false) OrdenResenas orden,
            @RequestParam(required = false) Integer puntuacion) {
        Long yo = auth.opcional(a).map(Identidad::usuarioId).orElse(null);
        return service.opiniones(libroId, orden, puntuacion, yo);
    }

    @Operation(summary = "[Público] Promedio de calificación de varios libros", tags = "Reseñas")
    @GetMapping("/resenas/promedios")
    public List<Promedio> promedios(@RequestParam List<Long> libros) {
        return service.promedios(libros);
    }

    @Operation(summary = "[Cliente] Publicar una reseña (solo libros comprados)", tags = "Reseñas")
    @PostMapping("/resenas/libros/{libroId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaRespuesta publicar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @Valid @RequestBody ResenaSolicitud s) {
        return service.publicar(cliente(a), libroId, s.contenido(), s.puntuacion());
    }

    @Operation(summary = "[Cliente] Calificar un libro (1 a 5 estrellas)", tags = "Reseñas")
    @PutMapping("/resenas/libros/{libroId}/calificacion")
    public OpinionesLibro calificar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long libroId, @Valid @RequestBody CalificacionSolicitud s) {
        return service.calificar(cliente(a), libroId, s.puntuacion());
    }

    @Operation(summary = "[Cliente] Mis reseñas", tags = "Reseñas")
    @GetMapping("/resenas/mias")
    public List<ResenaRespuesta> mias(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.misResenas(cliente(a));
    }

    @Operation(summary = "[Cliente] Editar mi reseña", tags = "Reseñas")
    @PutMapping("/resenas/{id}")
    public ResenaRespuesta editar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody EdicionSolicitud s) {
        return service.editar(cliente(a), id, s.contenido());
    }

    @Operation(summary = "[Cliente] Eliminar mi reseña", tags = "Reseñas")
    @DeleteMapping("/resenas/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        service.eliminarPropia(cliente(a), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[Cliente] Marcar una reseña como útil", tags = "Reseñas")
    @PostMapping("/resenas/{id}/util")
    public ResenaRespuesta util(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.marcarUtil(cliente(a), id);
    }

    @Operation(summary = "[Cliente] Quitar mi voto de útil", tags = "Reseñas")
    @DeleteMapping("/resenas/{id}/util")
    public ResenaRespuesta quitarUtil(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.quitarUtil(cliente(a), id);
    }

    @Operation(summary = "[Cliente] Reportar una reseña", tags = "Reseñas")
    @PostMapping("/resenas/{id}/reportes")
    public ResponseEntity<Void> reportar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody ReporteSolicitud s) {
        service.reportar(cliente(a), id, s.motivo());
        return ResponseEntity.noContent().build();
    }

    // ---------------- moderacion (ADMINISTRADOR)
    @Operation(summary = "[Admin] Reseñas reportadas pendientes", tags = "Moderación de reseñas")
    @GetMapping("/resenas/admin/reportadas")
    public List<ResenaRespuesta> reportadas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.reportadas();
    }

    @Operation(summary = "[Admin] Aprobar una reseña reportada", tags = "Moderación de reseñas")
    @PostMapping("/resenas/admin/{id}/aprobar")
    public ResenaRespuesta aprobar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.moderar(auth.admin(a).usuarioId(), id, EstadoReporte.APROBADO);
    }

    @Operation(summary = "[Admin] Retirar una reseña", tags = "Moderación de reseñas")
    @PostMapping("/resenas/admin/{id}/eliminar")
    public ResenaRespuesta retirar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.moderar(auth.admin(a).usuarioId(), id, EstadoReporte.ELIMINADO);
    }

    @Operation(summary = "[Admin] Listar todas las reseñas", tags = "Moderación de reseñas")
    @GetMapping("/resenas/admin")
    public PaginaResenas todas(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) EstadoResena estado, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return service.todas(estado, pagina, tamano);
    }
}
