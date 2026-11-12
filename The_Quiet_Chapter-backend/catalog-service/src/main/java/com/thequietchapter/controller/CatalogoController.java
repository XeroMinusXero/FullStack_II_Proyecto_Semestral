package com.thequietchapter.controller;

import com.thequietchapter.client.PedidosHttpClient;
import com.thequietchapter.dto.*;
import com.thequietchapter.model.CriteriosCatalogo;
import com.thequietchapter.model.OrdenCatalogo;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.security.Identidad;
import com.thequietchapter.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DC-01 Catalogo e Inventario. Lectura publica; escritura solo ADMINISTRADOR
 * (JWT de user-service).
 * El responsable de los movimientos de stock se toma del token, nunca del
 * cuerpo.
 */
@RestController
@RequestMapping("/api/libros")
public class CatalogoController {
    private final CatalogoService service;
    private final Autenticacion auth;
    private final PedidosHttpClient pedidos;

    public CatalogoController(CatalogoService service, Autenticacion auth, PedidosHttpClient pedidos) {
        this.service = service;
        this.auth = auth;
        this.pedidos = pedidos;
    }

    /**
     * Catalogo publico: texto (titulo/autor/ISBN), filtros, orden y paginacion.
     * Solo libros activos.
     */
    @GetMapping
    public PaginaLibros catalogo(@RequestParam(required = false) String texto,
            @RequestParam(required = false) List<Long> categorias,
            @RequestParam(required = false) List<Long> generos,
            @RequestParam(required = false) BigDecimal minimo,
            @RequestParam(required = false) BigDecimal maximo,
            @RequestParam(required = false) Long autorId,
            @RequestParam(required = false) Long editorialId,
            @RequestParam(required = false) OrdenCatalogo orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamanio) {
        var c = new CriteriosCatalogo(categorias, generos, minimo, maximo, true, false, autorId, editorialId);
        return PaginaLibros.desde(service.buscar(texto, c, orden, pagina, tamanio));
    }

    /**
     * Mantenedor (HU admin): incluye inactivos, filtro por estado
     * (ACTIVOS/INACTIVOS/TODOS) y agotados.
     */
    @GetMapping("/admin")
    public PaginaLibros mantenedor(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "TODOS") String estado,
            @RequestParam(defaultValue = "false") boolean agotados,
            @RequestParam(required = false) OrdenCatalogo orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        auth.admin(a);
        Boolean activo = switch (estado.toUpperCase()) {
            case "ACTIVOS" -> Boolean.TRUE;
            case "INACTIVOS" -> Boolean.FALSE;
            case "TODOS" -> null;
            default -> throw new IllegalArgumentException("estado debe ser ACTIVOS, INACTIVOS o TODOS");
        };
        var c = new CriteriosCatalogo(null, null, null, null, activo, agotados, null, null);
        return PaginaLibros.desde(service.buscar(texto, c, orden, pagina, tamanio));
    }

    @GetMapping("/sugerencias")
    public List<LibroResumen> sugerir(@RequestParam String texto, @RequestParam(defaultValue = "5") int limite) {
        return service.sugerir(texto, limite);
    }

    /**
     * Estado vigente (precio/stock/activo) de varios libros en un request. Usado
     * por cart-service.
     */
    @GetMapping("/condiciones")
    public List<CondicionLibroRespuesta> condiciones(@RequestParam List<Long> ids) {
        return service.condiciones(ids);
    }

    @GetMapping("/categorias")
    public List<ClasificacionRespuesta> categorias() {
        return service.listarCategorias();
    }

    @GetMapping("/generos")
    public List<ClasificacionRespuesta> generos() {
        return service.listarGeneros();
    }

    @GetMapping("/autores")
    public List<ClasificacionRespuesta> autores() {
        return service.listarAutores();
    }

    @GetMapping("/editoriales")
    public List<ClasificacionRespuesta> editoriales() {
        return service.listarEditoriales();
    }

    @GetMapping("/autores/{id}")
    public AutorRespuesta autor(@PathVariable Long id) {
        return service.autor(id);
    }

    @PutMapping("/autores/{id}/biografia")
    public AutorRespuesta biografia(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody BiografiaSolicitud s) {
        auth.admin(a);
        return service.actualizarBiografia(id, s.biografia());
    }

    /**
     * Publico ve solo activos; un administrador autenticado tambien ve inactivos.
     */
    @GetMapping("/{id}")
    public LibroDetalle detalle(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        boolean admin = auth.opcional(a).map(Identidad::esAdmin).orElse(false);
        return service.detalle(id, admin);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibroDetalle crear(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody LibroSolicitud s) {
        auth.admin(a);
        return service.registrar(s);
    }

    @PutMapping("/{id}")
    public LibroDetalle editar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody LibroSolicitud s) {
        auth.admin(a);
        return service.actualizar(id, s);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        service.eliminar(id, pedidos.libroTienePedidos(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/precio")
    public LibroDetalle precio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody PrecioSolicitud s) {
        auth.admin(a);
        return service.actualizarPrecio(id, s.precio());
    }

    @PatchMapping("/{id}/estado")
    public LibroDetalle estado(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @RequestBody EstadoSolicitud s) {
        auth.admin(a);
        return service.cambiarEstado(id, s.activo());
    }

    @PutMapping("/{id}/stock")
    public MovimientoStockRespuesta actualizarStock(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody ActualizarStockSolicitud s) {
        Identidad admin = auth.admin(a);
        return service.actualizarStock(id, s.nuevoStock(), admin.usuarioId());
    }

    @PostMapping("/{id}/stock/ajustes")
    public MovimientoStockRespuesta ajuste(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody AjusteStockSolicitud s) {
        Identidad admin = auth.admin(a);
        return service.ajustarStock(id, s.nuevoStock(), s.motivo(), admin.usuarioId());
    }

    @PostMapping("/{id}/stock/ingresos")
    public MovimientoStockRespuesta ingreso(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody IngresoStockSolicitud s) {
        Identidad admin = auth.admin(a);
        return service.registrarIngreso(id, s.cantidad(), admin.usuarioId());
    }

    @GetMapping("/{id}/stock/movimientos")
    public List<MovimientoStockRespuesta> movimientos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        return service.consultarMovimientos(id);
    }
}
