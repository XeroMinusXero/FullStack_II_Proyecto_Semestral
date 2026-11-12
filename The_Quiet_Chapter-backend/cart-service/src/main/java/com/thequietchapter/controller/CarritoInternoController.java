package com.thequietchapter.controller;

import com.thequietchapter.dto.CarritoRespuesta;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Solo checkout-service (X-TQC-Internal-Key). El gateway no expone
 * /internal/**.
 */
@RestController
@RequestMapping("/internal/carritos")
public class CarritoInternoController {
    private final CarritoService service;
    private final Autenticacion auth;

    public CarritoInternoController(CarritoService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/clientes/{clienteId}")
    public CarritoRespuesta carrito(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long clienteId) {
        auth.interna(k);
        return service.consultarCliente(clienteId);
    }

    @PostMapping("/clientes/{clienteId}/vaciar")
    public ResponseEntity<Void> vaciar(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long clienteId, @RequestParam Long carritoId) {
        auth.interna(k);
        service.vaciarCliente(clienteId, carritoId);
        return ResponseEntity.noContent().build();
    }
}
