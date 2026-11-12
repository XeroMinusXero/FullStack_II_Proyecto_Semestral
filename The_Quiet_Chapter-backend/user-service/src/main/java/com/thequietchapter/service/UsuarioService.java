package com.thequietchapter.service;

import com.thequietchapter.client.NotificacionesHttpClient;
import com.thequietchapter.model.RolUsuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.thequietchapter.model.*;
import com.thequietchapter.repository.UsuarioRepository;
import com.thequietchapter.dto.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UsuarioService {
    private static final int MINUTOS_CODIGO = 15;
    private final UsuarioRepository repo;
    private final TokenService tokens;
    private final NotificacionesHttpClient notificaciones;
    private final PasswordEncoder claves = new BCryptPasswordEncoder();
    private final SecureRandom aleatorio = new SecureRandom();

    public UsuarioService(UsuarioRepository repo, TokenService tokens, NotificacionesHttpClient notificaciones) {
        this.repo = repo;
        this.tokens = tokens;
        this.notificaciones = notificaciones;
    }

    private static String correo(String mail) {
        return mail == null ? "" : mail.trim().toLowerCase(Locale.ROOT);
    }

    public PerfilRespuesta registrar(RegistroSolicitud s) {
        String email = correo(s.email());
        if (repo.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
        // Validacion de politica ANTES de calcular el hash (8-72 caracteres y al menos
        // un caracter especial).
        Usuario u = new Usuario(s.nombre(), s.apellido(), email, s.telefono(), "pendiente");
        if (!u.cumplePoliticaContrasena(s.contrasena()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, POLITICA);
        u.actualizarHashContrasena(claves.encode(s.contrasena()));
        PerfilRespuesta creado = PerfilRespuesta.desde(repo.saveAndFlush(u));
        notificaciones.enviar(email, "BIENVENIDA", "Bienvenido a The Quiet Chapter",
                "Hola " + u.getNombre() + ", tu cuenta fue creada correctamente.");
        return creado;
    }

    public static final String POLITICA = "La contrasena debe tener entre 8 y 72 caracteres e incluir al menos un caracter especial";

    public TokenRespuesta login(LoginSolicitud s) {
        Usuario u = repo.findByEmail(correo(s.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (!claves.matches(s.contrasena(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        return new TokenRespuesta(tokens.generar(u), "Bearer", tokens.duracionSegundos(), PerfilRespuesta.desde(u));
    }

    private Usuario usuario(String authorization) {
        return repo.findById(tokens.idAutenticado(authorization))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inexistente"));
    }

    public PerfilRespuesta perfil(String auth) {
        return PerfilRespuesta.desde(usuario(auth));
    }

    public PerfilRespuesta actualizarPerfil(String auth, ActualizarPerfilSolicitud s) {
        Usuario u = usuario(auth);
        if (!u.actualizarDatos(s.nombre(), s.apellido(), s.telefono()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos personales invalidos");
        return PerfilRespuesta.desde(repo.save(u));
    }

    public PerfilRespuesta actualizarCorreo(String auth, ActualizarCorreoSolicitud s) {
        Usuario u = usuario(auth);
        String mail = correo(s.email());
        if (!mail.equals(u.getEmail()) && repo.existsByEmail(mail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
        String anterior = u.getEmail();
        if (!u.actualizarCorreo(mail))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo invalido");
        repo.flush();
        PerfilRespuesta r = PerfilRespuesta.desde(u);
        if (!anterior.equals(mail)) {
            notificaciones.enviar(anterior, "CAMBIO_CORREO", "Tu correo fue modificado",
                    "El correo de acceso de tu cuenta cambio a " + mail + ". Si no fuiste tu, contacta a la tienda.");
            notificaciones.enviar(mail, "CAMBIO_CORREO", "Correo actualizado",
                    "Este es ahora el correo de acceso de tu cuenta.");
        }
        return r;
    }

    public void cambiarContrasena(String auth, CambiarContrasenaSolicitud s) {
        Usuario u = usuario(auth);
        if (!claves.matches(s.actual(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contrasena actual incorrecta");
        if (!u.cumplePoliticaContrasena(s.nueva()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, POLITICA);
        u.actualizarHashContrasena(claves.encode(s.nueva()));
        repo.save(u);
        notificaciones.enviar(u.getEmail(), "CAMBIO_CONTRASENA", "Tu contrasena fue cambiada",
                "La contrasena de tu cuenta se modifico. Si no fuiste tu, recuperala de inmediato.");
    }

    public List<DireccionRespuesta> direcciones(String auth) {
        return usuario(auth).getLibreta().listarDirecciones().stream().map(DireccionRespuesta::desde).toList();
    }

    public DireccionRespuesta agregarDireccion(String auth, DireccionSolicitud s) {
        Usuario u = usuario(auth);
        Direccion d = new Direccion(s.calle(), s.numero(), s.comuna(), s.region());
        if (!u.getLibreta().agregarDireccion(d))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direccion invalida");
        repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
        return DireccionRespuesta.desde(d);
    }

    public DireccionRespuesta editarDireccion(String auth, Long id, DireccionSolicitud s) {
        Usuario u = usuario(auth);
        Direccion d = u.getLibreta().buscarDireccion(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direccion no encontrada"));
        if (!d.actualizarDatos(s.calle(), s.numero(), s.comuna(), s.region()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direccion invalida");
        repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
        return DireccionRespuesta.desde(d);
    }

    public void eliminarDireccion(String auth, Long id) {
        Usuario u = usuario(auth);
        if (!u.getLibreta().eliminarDireccion(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Direccion no encontrada");
        repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
    }

    public DireccionRespuesta predeterminada(String auth, Long id) {
        Usuario u = usuario(auth);
        if (!u.getLibreta().establecerPredeterminada(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Direccion no encontrada");
        repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
        return u.getLibreta().obtenerPredeterminada().map(DireccionRespuesta::desde).orElseThrow();
    }

    /**
     * HU-033: siempre responde igual (no revela si el correo existe). El codigo
     * viaja por notif-service (correo simulado).
     */
    public void solicitarRecuperacion(RecuperacionSolicitud s) {
        repo.findByEmail(correo(s.email())).ifPresent(u -> {
            String codigo = String.format("%06d", aleatorio.nextInt(1_000_000));
            u.registrarCodigoRecuperacion(claves.encode(codigo), LocalDateTime.now().plusMinutes(MINUTOS_CODIGO));
            repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
            notificaciones.enviar(u.getEmail(), "RECUPERACION_CONTRASENA", "Codigo para restablecer tu contrasena",
                    "Tu codigo es " + codigo + ". Vence en " + MINUTOS_CODIGO + " minutos y sirve una sola vez.");
        });
    }

    public void restablecerContrasena(RestablecerSolicitud s) {
        Usuario u = repo.findByEmail(correo(s.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo invalido o vencido"));
        if (!u.tieneCodigoRecuperacionVigente(LocalDateTime.now())
                || !claves.matches(s.codigo(), u.getCodigoRecuperacionHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo invalido o vencido");
        if (!u.cumplePoliticaContrasena(s.nueva()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, POLITICA);
        u.actualizarHashContrasena(claves.encode(s.nueva()));
        u.limpiarCodigoRecuperacion();
        repo.flush(); // usuario administrado: flush conserva las mismas instancias (Direccion con id)
        notificaciones.enviar(u.getEmail(), "CAMBIO_CONTRASENA", "Contrasena restablecida",
                "Tu contrasena fue restablecida correctamente.");
    }

    @Transactional(readOnly = true)
    public PaginaUsuarios listarClientes(int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginacion invalida");
        return PaginaUsuarios.desde(repo.findByRol(RolUsuario.CLIENTE,
                PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaRegistro")))
                .map(UsuarioBasico::desde));
    }

    @Transactional(readOnly = true)
    public List<UsuarioBasico> basicos(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        if (ids.size() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximo 500 ids");
        return repo.findByIdIn(ids).stream().map(UsuarioBasico::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioBasico> todosLosClientes() {
        return repo.findByRolOrderByIdAsc(RolUsuario.CLIENTE).stream().map(UsuarioBasico::desde).toList();
    }
}
