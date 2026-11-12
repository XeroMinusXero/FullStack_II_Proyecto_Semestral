package com.thequietchapter.controller;

import com.thequietchapter.dto.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.CheckoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * DC-03 para React. Cliente identificado SOLO por el JWT (HU-022: checkout
 * exige sesion).
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final CheckoutService service;
    private final Autenticacion auth;

    public CheckoutController(CheckoutService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    private Long cliente(String a) {
        return auth.requerida(a).usuarioId();
    }

    @GetMapping("/opciones/envio")
    public List<MetodoEnvioRespuesta> envios(@RequestParam(required = false) String region) {
        return service.metodosEnvio(region);
    }

    @GetMapping("/opciones/pago")
    public List<MedioPagoRespuesta> medios() {
        return service.mediosPago();
    }

    @PostMapping
    public CheckoutRespuesta iniciar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody IniciarCheckoutSolicitud s) {
        return service.iniciar(cliente(a), s.carritoId());
    }

    @GetMapping("/mis-procesos")
    public List<CheckoutRespuesta> mios(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.misProcesos(cliente(a));
    }

    @GetMapping("/{id}")
    public CheckoutRespuesta consultar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.consultar(cliente(a), id);
    }

    @PutMapping("/{id}/direccion")
    public CheckoutRespuesta direccion(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody DireccionEnvioSolicitud s) {
        return service.elegirDireccion(cliente(a), id, s);
    }

    @PutMapping("/{id}/envio")
    public CheckoutRespuesta envio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody OpcionSolicitud s) {
        return service.elegirEnvio(cliente(a), id, s.id());
    }

    @PutMapping("/{id}/medio-pago")
    public CheckoutRespuesta medio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody OpcionSolicitud s) {
        return service.elegirPago(cliente(a), id, s.id());
    }

    @PostMapping("/{id}/resumen")
    public CheckoutRespuesta resumen(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.prepararResumen(cliente(a), id);
    }

    /**
     * Pago simulado + creacion del pedido. Respuesta: estado FINALIZADO y
     * numeroPedidoConfirmado, o intento RECHAZADO con motivo.
     */
    @PostMapping("/{id}/pagar")
    public CheckoutRespuesta pagar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @RequestBody(required = false) PagarSolicitud s) {
        return service.pagar(cliente(a), id, s != null && s.simularRechazo());
    }

    @PostMapping("/{id}/cancelar")
    public CheckoutRespuesta cancelar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.cancelar(cliente(a), id);
    }

    // ------------------------------------------------ administracion
    // (ADMINISTRADOR)
    @GetMapping("/admin/metodos-envio")
    public List<MetodoEnvioRespuesta> adminEnvios(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.todosLosEnvios();
    }

    @PostMapping("/admin/metodos-envio")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoEnvioRespuesta crearEnvio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody MetodoEnvioSolicitud s) {
        auth.admin(a);
        return service.crearEnvio(s);
    }

    @PutMapping("/admin/metodos-envio/{id}")
    public MetodoEnvioRespuesta editarEnvio(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody MetodoEnvioSolicitud s) {
        auth.admin(a);
        return service.editarEnvio(id, s);
    }

    @GetMapping("/admin/medios-pago")
    public List<MedioPagoRespuesta> adminMedios(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.todosLosMedios();
    }

    @PostMapping("/admin/medios-pago")
    @ResponseStatus(HttpStatus.CREATED)
    public MedioPagoRespuesta crearMedio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody MedioPagoSolicitud s) {
        auth.admin(a);
        return service.crearMedio(s);
    }

    @PutMapping("/admin/medios-pago/{id}")
    public MedioPagoRespuesta editarMedio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody MedioPagoSolicitud s) {
        auth.admin(a);
        return service.editarMedio(id, s);
    }
}
