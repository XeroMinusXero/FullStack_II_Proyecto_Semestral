package com.thequietchapter.controller;

import com.thequietchapter.dto.CrearPedidoSolicitud;
import com.thequietchapter.dto.PedidoRespuesta;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio-a-servicio: checkout crea pedidos ya pagados; catalog consulta
 * pedidos por libro; report lee ventas.
 */
@RestController
@RequestMapping("/internal/pedidos")
public class PedidoInternoController {
    private final PedidoService service;
    private final Autenticacion auth;

    public PedidoInternoController(PedidoService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoRespuesta crear(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody CrearPedidoSolicitud s) {
        auth.interna(k);
        return service.crear(s);
    }

    @GetMapping("/libros/{libroId}/existe")
    public boolean libroTienePedidos(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long libroId) {
        auth.interna(k);
        return service.libroTienePedidos(libroId);
    }

    @GetMapping("/clientes/{clienteId}/libros")
    public List<Long> librosComprados(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long clienteId) {
        auth.interna(k);
        return service.librosComprados(clienteId);
    }

    @GetMapping
    public List<PedidoRespuesta> entre(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        auth.interna(k);
        return service.entre(desde, hasta);
    }
}
