package com.thequietchapter.controller;

import com.thequietchapter.dto.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.security.Identidad;
import com.thequietchapter.service.CarritoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * DC-02 para React. Visitante: carrito anonimo identificado por cookie HttpOnly
 * (HU-018).
 * Cliente: carrito de su cuenta identificado por el JWT (Authorization:
 * Bearer). No se aceptan IDs de cliente en cabeceras o cuerpo.
 */
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private static final String COOKIE = "tqc_cart_session";
    private final CarritoService service;
    private final Autenticacion auth;

    public CarritoController(CarritoService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    private record Quien(Long clienteId, String sesion) {
    }

    private Quien quien(String authorization, String cookie, HttpServletResponse response) {
        var identidad = auth.opcional(authorization);
        if (identidad.isPresent())
            return new Quien(identidad.get().usuarioId(), null);
        String sesion = (cookie != null && cookie.matches("[a-fA-F0-9-]{36}")) ? cookie : UUID.randomUUID().toString();
        if (!sesion.equals(cookie))
            escribirCookie(response, sesion);
        return new Quien(null, sesion);
    }

    private void escribirCookie(HttpServletResponse response, String sesion) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(COOKIE, sesion)
                .httpOnly(true).secure(false).sameSite("Lax").path("/api/carrito").maxAge(60L * 60 * 24 * 7).build()
                .toString());
    }

    @GetMapping
    public CarritoRespuesta consultar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response) {
        Quien q = quien(a, cookie, response);
        return service.consultar(q.clienteId(), q.sesion());
    }

    @PostMapping("/items")
    public CarritoRespuesta agregar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response,
            @Valid @RequestBody AgregarItemSolicitud s) {
        Quien q = quien(a, cookie, response);
        return service.agregar(q.clienteId(), q.sesion(), s.libroId(), s.cantidad());
    }

    @PutMapping("/items/{libroId}")
    public CarritoRespuesta actualizar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response,
            @PathVariable @Positive Long libroId, @Valid @RequestBody CantidadItemSolicitud s) {
        Quien q = quien(a, cookie, response);
        return service.actualizar(q.clienteId(), q.sesion(), libroId, s.cantidad());
    }

    @DeleteMapping("/items/{libroId}")
    public CarritoRespuesta eliminar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response,
            @PathVariable @Positive Long libroId) {
        Quien q = quien(a, cookie, response);
        return service.eliminar(q.clienteId(), q.sesion(), libroId);
    }

    @DeleteMapping("/items")
    public CarritoRespuesta vaciar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response) {
        Quien q = quien(a, cookie, response);
        return service.vaciar(q.clienteId(), q.sesion());
    }

    /**
     * HU-019: llamar justo despues del login. Usa la cookie anonima de este
     * navegador y el cliente del token.
     */
    @PostMapping("/fusionar")
    public CarritoRespuesta fusionar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @CookieValue(value = COOKIE, required = false) String cookie, HttpServletResponse response) {
        Identidad cliente = auth.requerida(a);
        String sesion = (cookie != null && cookie.matches("[a-fA-F0-9-]{36}")) ? cookie : null;
        CarritoRespuesta r = service.fusionar(sesion, cliente.usuarioId());
        escribirCookie(response, UUID.randomUUID().toString()); // la sesion anonima anterior queda consumida
        return r;
    }

    @PostMapping("/cupon")
    public CarritoRespuesta aplicarCupon(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
            @Valid @RequestBody CuponSolicitud s) {
        return service.aplicarCupon(auth.requerida(a).usuarioId(), s.codigo());
    }

    @DeleteMapping("/cupon")
    public CarritoRespuesta quitarCupon(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        return service.quitarCupon(auth.requerida(a).usuarioId());
    }
}
