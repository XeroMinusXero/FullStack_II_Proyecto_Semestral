package com.thequietchapter.controller;

import io.swagger.v3.oas.annotations.Operation;
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
  @Operation(summary = "[Público] Solicitar código de recuperación de contraseña", description = "Envía un código de 6 dígitos (correo simulado en notif-service).", tags = "Autenticación")
  @PostMapping("/recuperacion")
  public ResponseEntity<Map<String, String>> solicitarRecuperacion(@Valid @RequestBody RecuperacionSolicitud s) {
    service.solicitarRecuperacion(s);
    return ResponseEntity.accepted()
        .body(Map.of("mensaje", "Si el correo esta registrado, recibiras un codigo para restablecer tu contrasena"));
  }

  @Operation(summary = "[Público] Restablecer la contraseña con el código", tags = "Autenticación")
  @PostMapping("/recuperacion/restablecer")
  public ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecerSolicitud s) {
    service.restablecerContrasena(s);
    return ResponseEntity.noContent().build();
  }

  /** Administracion: listado paginado de clientes. */
  @Operation(summary = "[Admin] Listar usuarios", tags = "Usuarios")
  @GetMapping
  public PaginaUsuarios listar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
      @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
    auth.admin(a);
    return service.listarClientes(pagina, tamano);
  }

  @Operation(summary = "[Público] Registrar un cliente", tags = "Autenticación")
  @PostMapping("/registro")
  public ResponseEntity<PerfilRespuesta> registrar(@Valid @RequestBody RegistroSolicitud s) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(s));
  }

  @Operation(summary = "[Público] Iniciar sesión", description = "Devuelve accessToken (JWT HS256, 2 h). Copia ese valor y pégalo en el botón Authorize para probar las rutas protegidas.", tags = "Autenticación")
  @PostMapping("/login")
  public TokenRespuesta login(@Valid @RequestBody LoginSolicitud s) {
    return service.login(s);
  }

  @Operation(summary = "[Con sesión] Mi perfil", tags = "Mi cuenta")
  @GetMapping("/me")
  public PerfilRespuesta perfil(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
    return service.perfil(auth);
  }

  @Operation(summary = "[Con sesión] Actualizar mis datos personales", tags = "Mi cuenta")
  @PutMapping("/me")
  public PerfilRespuesta actualizar(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody ActualizarPerfilSolicitud s) {
    return service.actualizarPerfil(auth, s);
  }

  @Operation(summary = "[Con sesión] Cambiar mi correo", tags = "Mi cuenta")
  @PutMapping("/me/email")
  public PerfilRespuesta actualizarCorreo(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody ActualizarCorreoSolicitud s) {
    return service.actualizarCorreo(auth, s);
  }

  @Operation(summary = "[Con sesión] Cambiar mi contraseña", tags = "Mi cuenta")
  @PutMapping("/me/contrasena")
  public ResponseEntity<Void> contrasena(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody CambiarContrasenaSolicitud s) {
    service.cambiarContrasena(auth, s);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "[Cliente] Mis direcciones", tags = "Mi cuenta")
  @GetMapping("/me/direcciones")
  public List<DireccionRespuesta> direcciones(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
    return service.direcciones(auth);
  }

  @Operation(summary = "[Cliente] Agregar una dirección", tags = "Mi cuenta")
  @PostMapping("/me/direcciones")
  public ResponseEntity<DireccionRespuesta> agregarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
      @Valid @RequestBody DireccionSolicitud s) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarDireccion(auth, s));
  }

  @Operation(summary = "[Cliente] Editar una dirección", tags = "Mi cuenta")
  @PutMapping("/me/direcciones/{id}")
  public DireccionRespuesta editarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id,
      @Valid @RequestBody DireccionSolicitud s) {
    return service.editarDireccion(auth, id, s);
  }

  @Operation(summary = "[Cliente] Eliminar una dirección", tags = "Mi cuenta")
  @DeleteMapping("/me/direcciones/{id}")
  public ResponseEntity<Void> eliminarDireccion(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id) {
    service.eliminarDireccion(auth, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "[Cliente] Marcar una dirección como predeterminada", tags = "Mi cuenta")
  @PutMapping("/me/direcciones/{id}/predeterminada")
  public DireccionRespuesta predeterminada(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Long id) {
    return service.predeterminada(auth, id);
  }
}
