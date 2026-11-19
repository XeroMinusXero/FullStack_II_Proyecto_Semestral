package com.thequietchapter.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "[Público] Métodos de envío disponibles para una región", tags = "Opciones de envío y pago")
    @GetMapping("/opciones/envio")
    public List<MetodoEnvioRespuesta> envios(@RequestParam(required = false) String region) {
        return service.metodosEnvio(region);
    }

    @Operation(summary = "[Público] Medios de pago disponibles", tags = "Opciones de envío y pago")
    @GetMapping("/opciones/pago")
    public List<MedioPagoRespuesta> medios() {
        return service.mediosPago();
    }

    @Operation(summary = "[Cliente] Iniciar el proceso de compra desde el carrito", tags = "Checkout")
    @PostMapping
    public CheckoutRespuesta iniciar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody IniciarCheckoutSolicitud s) {
        return service.iniciar(cliente(a), s.carritoId());
    }

    @Operation(summary = "[Cliente] Mis procesos de compra", tags = "Checkout")
    @GetMapping("/mis-procesos")
    public List<CheckoutRespuesta> mios(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.misProcesos(cliente(a));
    }

    @Operation(summary = "[Cliente] Consultar un proceso de compra", tags = "Checkout")
    @GetMapping("/{id}")
    public CheckoutRespuesta consultar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.consultar(cliente(a), id);
    }

    @Operation(summary = "[Cliente] Paso 1: dirección de envío", tags = "Checkout")
    @PutMapping("/{id}/direccion")
    public CheckoutRespuesta direccion(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody DireccionEnvioSolicitud s) {
        return service.elegirDireccion(cliente(a), id, s);
    }

    @Operation(summary = "[Cliente] Paso 2: método de envío", tags = "Checkout")
    @PutMapping("/{id}/envio")
    public CheckoutRespuesta envio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody OpcionSolicitud s) {
        return service.elegirEnvio(cliente(a), id, s.id());
    }

    @Operation(summary = "[Cliente] Paso 3: medio de pago", tags = "Checkout")
    @PutMapping("/{id}/medio-pago")
    public CheckoutRespuesta medio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @Valid @RequestBody OpcionSolicitud s) {
        return service.elegirPago(cliente(a), id, s.id());
    }

    @Operation(summary = "[Cliente] Paso 4: calcular el resumen (IVA incluido)", tags = "Checkout")
    @PostMapping("/{id}/resumen")
    public CheckoutRespuesta resumen(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.prepararResumen(cliente(a), id);
    }

    /**
     * Pago simulado + creacion del pedido. Respuesta: estado FINALIZADO y
     * numeroPedidoConfirmado, o intento RECHAZADO con motivo.
     */
    @Operation(summary = "[Cliente] Pagar (pago simulado)", description = "Orquesta la compra: descuenta stock (catalog), crea el pedido (order), aprueba el pago, vacía el carrito, registra cupón y promociones y notifica. Si falla un paso, compensa (repone stock) y el pago queda RECHAZADO. simularRechazo=true fuerza un rechazo de prueba.", tags = "Checkout")
    @PostMapping("/{id}/pagar")
    public CheckoutRespuesta pagar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id, @RequestBody(required = false) PagarSolicitud s) {
        return service.pagar(cliente(a), id, s != null && s.simularRechazo());
    }

    @Operation(summary = "[Cliente] Cancelar el proceso de compra", tags = "Checkout")
    @PostMapping("/{id}/cancelar")
    public CheckoutRespuesta cancelar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable @Positive Long id) {
        return service.cancelar(cliente(a), id);
    }

    // ------------------------------------------------ administracion
    // (ADMINISTRADOR)
    @Operation(summary = "[Admin] Listar métodos de envío", tags = "Opciones de envío y pago")
    @GetMapping("/admin/metodos-envio")
    public List<MetodoEnvioRespuesta> adminEnvios(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.todosLosEnvios();
    }

    @Operation(summary = "[Admin] Crear un método de envío", tags = "Opciones de envío y pago")
    @PostMapping("/admin/metodos-envio")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoEnvioRespuesta crearEnvio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody MetodoEnvioSolicitud s) {
        auth.admin(a);
        return service.crearEnvio(s);
    }

    @Operation(summary = "[Admin] Editar un método de envío", tags = "Opciones de envío y pago")
    @PutMapping("/admin/metodos-envio/{id}")
    public MetodoEnvioRespuesta editarEnvio(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody MetodoEnvioSolicitud s) {
        auth.admin(a);
        return service.editarEnvio(id, s);
    }

    @Operation(summary = "[Admin] Listar medios de pago", tags = "Opciones de envío y pago")
    @GetMapping("/admin/medios-pago")
    public List<MedioPagoRespuesta> adminMedios(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        auth.admin(a);
        return service.todosLosMedios();
    }

    @Operation(summary = "[Admin] Crear un medio de pago", tags = "Opciones de envío y pago")
    @PostMapping("/admin/medios-pago")
    @ResponseStatus(HttpStatus.CREATED)
    public MedioPagoRespuesta crearMedio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody MedioPagoSolicitud s) {
        auth.admin(a);
        return service.crearMedio(s);
    }

    @Operation(summary = "[Admin] Editar un medio de pago", tags = "Opciones de envío y pago")
    @PutMapping("/admin/medios-pago/{id}")
    public MedioPagoRespuesta editarMedio(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @PathVariable Long id, @Valid @RequestBody MedioPagoSolicitud s) {
        auth.admin(a);
        return service.editarMedio(id, s);
    }
}
