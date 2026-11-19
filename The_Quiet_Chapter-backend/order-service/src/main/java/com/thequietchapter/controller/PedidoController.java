package com.thequietchapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import com.thequietchapter.dto.*;
import com.thequietchapter.model.CriteriosPedido;
import com.thequietchapter.model.EstadoPedido;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * DC-04. Cliente: sus propios pedidos (id del JWT). Administrador: gestion
 * operativa completa.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    private final Autenticacion auth;

    public PedidoController(PedidoService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    // ------------------------------------------------------------ cliente
    @Operation(summary = "[Cliente] Mis pedidos (paginado, filtro por estado)", tags = "Mis pedidos")
    @GetMapping("/mis-pedidos")
    public PaginaPedidosRespuesta misPedidos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "10") int tamano) {
        Long cliente = auth.requerida(a).usuarioId();
        return service.filtrar(cliente, new CriteriosPedido(estado, fechaDesde, fechaHasta), pagina, tamano);
    }

    @Operation(summary = "[Cliente] Detalle de uno de mis pedidos", tags = "Mis pedidos")
    @GetMapping("/mis-pedidos/{id}")
    public PedidoRespuesta miPedido(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.consultarPropio(auth.requerida(a).usuarioId(), id);
    }

    @Operation(summary = "[Cliente] Buscar uno de mis pedidos por número", tags = "Mis pedidos")
    @GetMapping("/mis-pedidos/numero/{numero}")
    public PedidoRespuesta miPedidoPorNumero(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable String numero) {
        return service.buscarPorNumero(auth.requerida(a).usuarioId(), numero);
    }

    @Operation(summary = "[Cliente] Cancelar mi pedido (antes del envío)", description = "Repone el stock y notifica por correo.", tags = "Mis pedidos")
    @PostMapping("/mis-pedidos/{id}/cancelar")
    public PedidoRespuesta cancelarPropio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.cancelar(auth.requerida(a).usuarioId(), id);
    }

    // ------------------------------------------------------------ administrador
    @Operation(summary = "[Admin] Listar pedidos (filtro por estado y fechas)", tags = "Gestión de pedidos")
    @GetMapping
    public PaginaPedidosRespuesta listado(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "10") int tamano) {
        auth.admin(a);
        return service.listarAdmin(new CriteriosPedido(estado, fechaDesde, fechaHasta), pagina, tamano);
    }

    @Operation(summary = "[Admin] Buscar un pedido por número", tags = "Gestión de pedidos")
    @GetMapping("/numero/{numero}")
    public PedidoRespuesta porNumero(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable String numero) {
        auth.admin(a);
        return service.buscarNumeroAdmin(numero);
    }

    @Operation(summary = "[Admin] Detalle de un pedido", tags = "Gestión de pedidos")
    @GetMapping("/{id}")
    public PedidoRespuesta detalle(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        return service.consultarAdmin(id);
    }

    @Operation(summary = "[Admin] Cambiar el estado de un pedido", description = "Transiciones válidas: PAGADO → EN_PREPARACION → ENVIADO → ENTREGADO. Notifica al cliente.", tags = "Gestión de pedidos")
    @PutMapping("/{id}/estado")
    public PedidoRespuesta estado(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody EstadoPedidoSolicitud s) {
        auth.admin(a);
        return service.cambiarEstado(id, s.estado());
    }

    @Operation(summary = "[Admin] Registrar el número de seguimiento", tags = "Gestión de pedidos")
    @PutMapping("/{id}/seguimiento")
    public PedidoRespuesta seguimiento(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody SeguimientoSolicitud s) {
        auth.admin(a);
        return service.seguimiento(id, s.numeroSeguimiento());
    }

    @Operation(summary = "[Admin] Cancelar un pedido", tags = "Gestión de pedidos")
    @PostMapping("/{id}/cancelar")
    public PedidoRespuesta cancelarAdmin(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        return service.cancelarAdmin(id);
    }
}
