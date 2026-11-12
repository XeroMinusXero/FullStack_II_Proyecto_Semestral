package com.thequietchapter.controller;

import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.security.Identidad;
import com.thequietchapter.service.PromocionesService;
import com.thequietchapter.service.RecomendacionesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * DC-07. Administracion de cupones y promociones (ADMINISTRADOR); precios,
 * ofertas y recomendaciones (publico/cliente).
 */
@RestController
@RequestMapping("/api")
public class PromocionesController {
    private final PromocionesService promos;
    private final RecomendacionesService recomendaciones;
    private final Autenticacion auth;

    public PromocionesController(PromocionesService promos, RecomendacionesService recomendaciones,
            Autenticacion auth) {
        this.promos = promos;
        this.recomendaciones = recomendaciones;
        this.auth = auth;
    }

    private static final String AUTH = HttpHeaders.AUTHORIZATION;

    // ---------------- cupones (ADMINISTRADOR)
    @GetMapping("/cupones")
    public Pagina<CuponRespuesta> cupones(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return promos.listarCupones(pagina, tamano);
    }

    @PostMapping("/cupones")
    @ResponseStatus(HttpStatus.CREATED)
    public CuponRespuesta crearCupon(@RequestHeader(value = AUTH, required = false) String a,
            @Valid @RequestBody CuponSolicitud s) {
        auth.admin(a);
        return promos.crearCupon(s);
    }

    @PutMapping("/cupones/{id}")
    public CuponRespuesta editarCupon(@RequestHeader(value = AUTH, required = false) String a, @PathVariable Long id,
            @Valid @RequestBody CuponSolicitud s) {
        auth.admin(a);
        return promos.editarCupon(id, s);
    }

    @PatchMapping("/cupones/{id}/estado")
    public CuponRespuesta estadoCupon(@RequestHeader(value = AUTH, required = false) String a, @PathVariable Long id,
            @RequestBody EstadoSolicitud s) {
        auth.admin(a);
        return promos.estadoCupon(id, s.activo());
    }

    @DeleteMapping("/cupones/{id}")
    public ResponseEntity<Void> eliminarCupon(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        promos.eliminarCupon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cupones/{id}/canjes")
    public List<CanjeRespuesta> canjes(@RequestHeader(value = AUTH, required = false) String a, @PathVariable Long id) {
        auth.admin(a);
        return promos.canjes(id);
    }

    // ---------------- promociones
    @GetMapping("/promociones/admin")
    public Pagina<PromocionRespuesta> promociones(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        auth.admin(a);
        return promos.listarPromociones(pagina, tamano);
    }

    @PostMapping("/promociones")
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionRespuesta crearPromocion(@RequestHeader(value = AUTH, required = false) String a,
            @Valid @RequestBody PromocionSolicitud s) {
        auth.admin(a);
        return promos.crearPromocion(s);
    }

    @PutMapping("/promociones/{id}")
    public PromocionRespuesta editarPromocion(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody PromocionSolicitud s) {
        auth.admin(a);
        return promos.editarPromocion(id, s);
    }

    @PatchMapping("/promociones/{id}/estado")
    public PromocionRespuesta estadoPromocion(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long id, @RequestBody EstadoSolicitud s) {
        auth.admin(a);
        return promos.estadoPromocion(id, s.activo());
    }

    @DeleteMapping("/promociones/{id}")
    public ResponseEntity<Void> eliminarPromocion(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        promos.eliminarPromocion(id);
        return ResponseEntity.noContent().build();
    }

    /** HU-111/113 (publico). */
    @GetMapping("/promociones/precios")
    public List<PrecioPromo> precios(@RequestParam List<Long> libros) {
        return promos.precios(libros);
    }

    /** HU-112 (publico). */
    @GetMapping("/promociones/ofertas")
    public List<Oferta> ofertas() {
        return promos.ofertas();
    }

    // ---------------- recomendaciones
    @GetMapping("/recomendaciones/inicio")
    public List<LibroSugerido> inicio(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "8") int limite) {
        return recomendaciones.inicio(auth.opcional(a).map(Identidad::usuarioId).orElse(null), limite);
    }

    @GetMapping("/recomendaciones/perfil")
    public List<LibroSugerido> perfil(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "8") int limite) {
        return recomendaciones.perfil(auth.requerida(a).usuarioId(), limite);
    }

    @GetMapping("/recomendaciones/compras")
    public List<LibroSugerido> compras(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(defaultValue = "8") int limite) {
        return recomendaciones.segunCompras(auth.requerida(a).usuarioId(), limite);
    }

    @GetMapping("/recomendaciones/categorias/{categoriaId}")
    public List<LibroSugerido> categoria(@PathVariable Long categoriaId, @RequestParam(defaultValue = "6") int limite) {
        return recomendaciones.porCategoria(categoriaId, limite);
    }

    @GetMapping("/recomendaciones/libros/{libroId}/relacionados")
    public List<LibroSugerido> relacionados(@PathVariable Long libroId, @RequestParam(defaultValue = "6") int limite) {
        return recomendaciones.relacionados(libroId, limite);
    }

    @GetMapping("/recomendaciones/complementarios")
    public List<LibroSugerido> complementarios(@RequestParam List<Long> libros,
            @RequestParam(defaultValue = "4") int limite) {
        return recomendaciones.complementarios(libros, limite);
    }

    @PostMapping("/recomendaciones/visitas/{libroId}")
    public ResponseEntity<Void> visita(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable Long libroId) {
        recomendaciones.registrarVisita(auth.requerida(a).usuarioId(), libroId);
        return ResponseEntity.noContent().build();
    }
}
