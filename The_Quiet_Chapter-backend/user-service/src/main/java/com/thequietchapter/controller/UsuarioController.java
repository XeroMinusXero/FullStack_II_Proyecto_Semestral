package com.thequietchapter.controller;

import com.thequietchapter.security.Autenticacion;
import java.util.Map;
import com.thequietchapter.service.UsuarioService;
import com.thequietchapter.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final UsuarioService service;
  private final Autenticacion auth;

  public UsuarioController(UsuarioService service, Autenticacion auth) {
    this.service = service;
    this.auth = auth;
  }

  /** HU-033: respuesta identica exista o no el correo. */
  @PostMapping("/recuperacion")
  public ResponseEntity<Map<String, String>> solicitarRecuperacion(@Valid @RequestBody RecuperacionSolicitud s) {
    service.solicitarRecuperacion(s);
    return ResponseEntity.accepted()
        .body(Map.of("mensaje", "Si el correo esta registrado, recibiras un codigo para restablecer tu contrasena"));
  }

  @PostMapping("/recuperacion/restablecer")
  public ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecerSolicitud s) {
    service.restablecerContrasena(s);
    return ResponseEntity.noContent().build();
  }

  /** Administracion: listado paginado de clientes. */
  @GetMapping
  public PaginaUsuarios listar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
      @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
    auth.admin(a);
    return service.listarClientes(pagina, tamano);
  }

  @PostMapping("/registro")
  public ResponseEntity<PerfilRespuesta> registrar(@Valid @RequestBody RegistroSolicitud s) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(s));
  }

  @PostMapping("/login")
  public TokenRespuesta login(@Valid @RequestBody LoginSolicitud s) {
    return service.login(s);
  }

  @GetMapping("/me")
  public PerfilRespuesta perfil(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
    return service.perfil(auth);
  }

  @PutMapping("/me")
  public PerfilRespuesta actualizar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody ActualizarPerfilSolicitud s) {
    return service.actualizarPerfil(auth, s);
  }

  @PutMapping("/me/email")
  public PerfilRespuesta actualizarCorreo(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody ActualizarCorreoSolicitud s) {
    return service.actualizarCorreo(auth, s);
  }

  @PutMapping("/me/contrasena")
  public ResponseEntity<Void> contrasena(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody CambiarContrasenaSolicitud s) {
    service.cambiarContrasena(auth, s);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me/direcciones")
  public List<DireccionRespuesta> direcciones(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
    return service.direcciones(auth);
  }

  @PostMapping("/me/direcciones")
  public ResponseEntity<DireccionRespuesta> agregarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody DireccionSolicitud s) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarDireccion(auth, s));
  }

  @PutMapping("/me/direcciones/{id}")
  public DireccionRespuesta editarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id,
      @Valid @RequestBody DireccionSolicitud s) {
    return service.editarDireccion(auth, id, s);
  }

  @DeleteMapping("/me/direcciones/{id}")
  public ResponseEntity<Void> eliminarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id) {
    service.eliminarDireccion(auth, id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/me/direcciones/{id}/predeterminada")
  public DireccionRespuesta predeterminada(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id) {
    return service.predeterminada(auth, id);
  }
}
