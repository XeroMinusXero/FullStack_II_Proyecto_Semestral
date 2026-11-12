package com.thequietchapter.controller;

import com.thequietchapter.dto.LibroInterno;
import com.thequietchapter.dto.LineasStockSolicitud;
import com.thequietchapter.dto.MovimientoConsolidado;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Solo servicio-a-servicio con X-TQC-Internal-Key. El API Gateway no enruta
 * /internal/**.
 */
@RestController
@RequestMapping("/internal/libros")
public class CatalogoInternoController {
    private final CatalogoService service;
    private final Autenticacion auth;

    public CatalogoInternoController(CatalogoService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    @PostMapping("/stock/descontar")
    public ResponseEntity<Void> descontar(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody LineasStockSolicitud s) {
        auth.interna(k);
        service.descontarLote(s);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stock/reponer")
    public ResponseEntity<Void> reponer(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody LineasStockSolicitud s) {
        auth.interna(k);
        service.reponerLote(s);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<LibroInterno> todos(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @RequestParam(required = false) List<Long> ids) {
        auth.interna(k);
        return service.todosInterno(ids);
    }

    @GetMapping("/movimientos")
    public List<MovimientoConsolidado> movimientos(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        auth.interna(k);
        return service.movimientosEntre(desde, hasta);
    }
}
