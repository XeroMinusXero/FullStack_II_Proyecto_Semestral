package com.thequietchapter.controller;

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

    @GetMapping("/mis-pedidos/{id}")
    public PedidoRespuesta miPedido(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.consultarPropio(auth.requerida(a).usuarioId(), id);
    }

    @GetMapping("/mis-pedidos/numero/{numero}")
    public PedidoRespuesta miPedidoPorNumero(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable String numero) {
        return service.buscarPorNumero(auth.requerida(a).usuarioId(), numero);
    }

    @PostMapping("/mis-pedidos/{id}/cancelar")
    public PedidoRespuesta cancelarPropio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        return service.cancelar(auth.requerida(a).usuarioId(), id);
    }

    // ------------------------------------------------------------ administrador
    @GetMapping
    public PaginaPedidosRespuesta listado(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "10") int tamano) {
        auth.admin(a);
        return service.listarAdmin(new CriteriosPedido(estado, fechaDesde, fechaHasta), pagina, tamano);
    }

    @GetMapping("/numero/{numero}")
    public PedidoRespuesta porNumero(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable String numero) {
        auth.admin(a);
        return service.buscarNumeroAdmin(numero);
    }

    @GetMapping("/{id}")
    public PedidoRespuesta detalle(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        return service.consultarAdmin(id);
    }

    @PutMapping("/{id}/estado")
    public PedidoRespuesta estado(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody EstadoPedidoSolicitud s) {
        auth.admin(a);
        return service.cambiarEstado(id, s.estado());
    }

    @PutMapping("/{id}/seguimiento")
    public PedidoRespuesta seguimiento(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody SeguimientoSolicitud s) {
        auth.admin(a);
        return service.seguimiento(id, s.numeroSeguimiento());
    }

    @PostMapping("/{id}/cancelar")
    public PedidoRespuesta cancelarAdmin(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id) {
        auth.admin(a);
        return service.cancelarAdmin(id);
    }
}
