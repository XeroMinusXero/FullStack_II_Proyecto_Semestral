package com.thequietchapter.controller;

import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.PromocionesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Servicio-a-servicio (cart, checkout). El gateway no expone /internal/**. */
@RestController
@RequestMapping("/internal")
public class PromocionesInternoController {
    private final PromocionesService promos;
    private final Autenticacion auth;

    public PromocionesInternoController(PromocionesService promos, Autenticacion auth) {
        this.promos = promos;
        this.auth = auth;
    }

    @PostMapping("/promociones/precios")
    public List<PrecioPromo> precios(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @RequestBody List<@Valid PrecioLista> lista) {
        auth.interna(k);
        return promos.preciosInternos(lista);
    }

    @PostMapping("/promociones/aplicaciones")
    public ResponseEntity<Void> aplicaciones(
            @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody AplicacionesSolicitud s) {
        auth.interna(k);
        promos.registrarAplicaciones(s);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cupones/validar")
    public CuponValidado validar(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @Valid @RequestBody ValidarCuponSolicitud s) {
        auth.interna(k);
        return promos.validar(s.codigo(), s.subtotal());
    }

    @PostMapping("/cupones/{id}/canjes")
    public CanjeRespuesta canje(@RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String k,
            @PathVariable Long id, @Valid @RequestBody CanjeSolicitud s) {
        auth.interna(k);
        return promos.registrarCanje(id, s);
    }
}
