package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"))
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RolUsuario rol;
  @Column(nullable = false, length = 100)
  private String nombre;
  @Column(nullable = false, length = 100)
  private String apellido;
  @Column(nullable = false, length = 180)
  private String email;
  @Column(length = 30)
  private String telefono;
  @Column(nullable = false, length = 100)
  private String passwordHash;
  @Column(nullable = false, updatable = false)
  private LocalDateTime fechaRegistro;
  @Version
  private long version;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "libreta_id", unique = true)
  private LibretaDirecciones libreta;
  /**
   * HU-033 Recuperar contrasena: solo se guarda el HASH del codigo de un solo uso
   * y su vencimiento.
   */
  @Column(name = "codigo_recuperacion_hash", length = 100)
  private String codigoRecuperacionHash;
  @Column(name = "codigo_recuperacion_expira")
  private LocalDateTime codigoRecuperacionExpira;

  protected Usuario() {
  }

  public Usuario(String nombre, String apellido, String email, String telefono, String hash) {
    if (!actualizarDatos(nombre, apellido, telefono))
      throw new IllegalArgumentException("Datos personales invalidos");
    if (!actualizarCorreo(email))
      throw new IllegalArgumentException("Correo invalido");
    if (hash == null || hash.isBlank())
      throw new IllegalArgumentException("Hash obligatorio");
    this.passwordHash = hash;
    this.rol = RolUsuario.CLIENTE;
    this.fechaRegistro = LocalDateTime.now();
    this.libreta = LibretaDirecciones.crear();
  }

  /**
   * Administrador inicial: solo se crea desde configuracion del servidor, nunca
   * desde el registro publico.
   */
  public static Usuario administrador(String nombre, String apellido, String email, String hash) {
    Usuario u = new Usuario(nombre, apellido, email, "", hash);
    u.rol = RolUsuario.ADMINISTRADOR;
    return u;
  }

  public boolean actualizarDatos(String nombre, String apellido, String telefono) {
    String n = nombre == null ? "" : nombre.trim(), a = apellido == null ? "" : apellido.trim(),
        t = telefono == null ? "" : telefono.trim();
    if (n.isEmpty() || n.length() > 100 || a.isEmpty() || a.length() > 100 || t.length() > 30)
      return false;
    this.nombre = n;
    this.apellido = a;
    this.telefono = t;
    return true;
  }

  public boolean actualizarCorreo(String email) {
    if (email == null)
      return false;
    String e = email.trim().toLowerCase(Locale.ROOT);
    if (e.length() > 180 || !e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
      return false;
    this.email = e;
    return true;
  }

  public String obtenerNombreCompleto() {
    return nombre + " " + apellido;
  }

  /**
   * HU-037 (ex HU-033): minimo 8 caracteres y al menos un caracter especial.
   * Maximo 72 por limite de BCrypt.
   */
  public boolean cumplePoliticaContrasena(String contrasena) {
    return contrasena != null && contrasena.length() >= 8 && contrasena.length() <= 72 && !contrasena.isBlank()
        && contrasena.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
  }

  public void registrarCodigoRecuperacion(String hash, LocalDateTime expira) {
    if (hash == null || hash.isBlank() || expira == null)
      throw new IllegalArgumentException("Codigo invalido");
    this.codigoRecuperacionHash = hash;
    this.codigoRecuperacionExpira = expira;
  }

  public boolean tieneCodigoRecuperacionVigente(LocalDateTime ahora) {
    return codigoRecuperacionHash != null && codigoRecuperacionExpira != null
        && ahora.isBefore(codigoRecuperacionExpira);
  }

  public void limpiarCodigoRecuperacion() {
    codigoRecuperacionHash = null;
    codigoRecuperacionExpira = null;
  }

  public String getCodigoRecuperacionHash() {
    return codigoRecuperacionHash;
  }

  public void actualizarHashContrasena(String nuevoHash) {
    if (nuevoHash == null || nuevoHash.isBlank())
      throw new IllegalArgumentException("Hash invalido");
    this.passwordHash = nuevoHash;
  }

  public Long getId() {
    return id;
  }

  public RolUsuario getRol() {
    return rol;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public String getEmail() {
    return email;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public LibretaDirecciones getLibreta() {
    return libreta;
  }
}
