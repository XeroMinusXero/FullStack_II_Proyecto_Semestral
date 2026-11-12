package com.thequietchapter.config;

import com.thequietchapter.model.RolUsuario;
import com.thequietchapter.model.Usuario;
import com.thequietchapter.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * HU-045: crea el ADMINISTRADOR inicial UNA vez, si no existe ninguno.
 * Credenciales desde variables de entorno.
 */
@Configuration
public class AdministradorInicial {
  private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

  @Bean
  CommandLineRunner crearAdministrador(UsuarioRepository repo,
      @Value("${tqc.admin.email}") String email, @Value("${tqc.admin.password}") String clave,
      @Value("${tqc.admin.nombre:Administrador}") String nombre, @Value("${tqc.admin.apellido:TQC}") String apellido) {
    return args -> {
      if (repo.existsByRol(RolUsuario.ADMINISTRADOR))
        return;
      String correo = email.trim().toLowerCase();
      if (repo.existsByEmail(correo)) {
        log.warn("No se creo administrador: el correo {} ya existe como cliente", correo);
        return;
      }
      Usuario admin = Usuario.administrador(nombre, apellido, correo, new BCryptPasswordEncoder().encode(clave));
      if (!admin.cumplePoliticaContrasena(clave))
        throw new IllegalStateException("tqc.admin.password no cumple la politica de contrasena");
      repo.saveAndFlush(admin);
      log.info("Administrador inicial creado: {}", correo);
    };
  }
}
