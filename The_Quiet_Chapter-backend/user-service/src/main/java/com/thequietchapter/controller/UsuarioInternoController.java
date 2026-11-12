package com.thequietchapter.controller;

import com.thequietchapter.dto.UsuarioBasico;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Solo servicio-a-servicio (order, report, notif). El gateway NO enruta
 * /internal/**.
 */
@RestController
@RequestMapping("/internal/usuarios")
public class UsuarioInternoController {
  private final UsuarioService service;
  private final Autenticacion auth;

  public UsuarioInternoController(UsuarioService service, Autenticacion auth) {
    this.service = service;
    this.auth = auth;
  }

  @GetMapping
  public List<UsuarioBasico> basicos(
      @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String clave,
      @RequestParam List<Long> ids) {
    auth.interna(clave);
    return service.basicos(ids);
  }

  @GetMapping("/clientes")
  public List<UsuarioBasico> clientes(
      @RequestHeader(value = Autenticacion.CABECERA_INTERNA, required = false) String clave) {
    auth.interna(clave);
    return service.todosLosClientes();
  }
}
