package com.thequietchapter.service;

import com.thequietchapter.client.ServiciosHttpClient;
import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;

/** Casos de uso DC-08 (EP-08 Notificaciones y Comunicacion). */
@Service
public class NotificacionesService {
    private final NotificacionRepository notificaciones;
    private final SolicitudReabastecimientoRepository avisos;
    private final SuscripcionRepository suscripciones;
    private final BoletinRepository boletines;
    private final ServiciosHttpClient servicios;
    private final String frontend;
    private final CentroNotificaciones centro = new CentroNotificaciones();
    private final SecureRandom aleatorio = new SecureRandom();

    public NotificacionesService(NotificacionRepository notificaciones, SolicitudReabastecimientoRepository avisos,
            SuscripcionRepository suscripciones, BoletinRepository boletines, ServiciosHttpClient servicios,
            @Value("${tqc.frontend.url}") String frontend) {
        this.notificaciones = notificaciones;
        this.avisos = avisos;
        this.suscripciones = suscripciones;
        this.boletines = boletines;
        this.servicios = servicios;
        this.frontend = frontend.replaceAll("/+$", "");
    }

    private static PageRequest pagina(int n, int t, String orden) {
        if (n < 0 || t < 1 || t > 100)
            throw new IllegalArgumentException("Paginacion invalida");
        return PageRequest.of(n, t, Sort.by(Sort.Direction.DESC, orden));
    }

    private static <A, B> Pagina<B> pagina(Page<A> p, Function<A, B> f) {
        return new Pagina<>(p.getContent().stream().map(f).toList(), p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages());
    }

    private String token() {
        byte[] b = new byte[24];
        aleatorio.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private Notificacion despachar(Notificacion n) {
        centro.registrarNotificacion(n);
        return notificaciones.save(n);
    }

    // ------------------------------------------------------------ envio (interno)
    @Transactional
    public NotificacionRespuesta enviar(EnviarSolicitud s) {
        TipoNotificacion tipo;
        try {
            tipo = TipoNotificacion.valueOf(s.tipo().strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de notificacion desconocido: " + s.tipo());
        }
        String destino = s.destinatario() != null && !s.destinatario().isBlank() ? s.destinatario()
                : (s.clienteId() != null ? servicios.correo(s.clienteId()) : null);
        if (destino == null)
            throw new IllegalArgumentException("Indica destinatario o clienteId");
        Notificacion n = new Notificacion(destino, tipo, s.asunto(), s.mensaje());
        if (s.enlaceAccion() != null)
            n.asignarEnlaceAccion(s.enlaceAccion());
        return NotificacionRespuesta.desde(despachar(n));
    }

    @Transactional(readOnly = true)
    public Pagina<NotificacionRespuesta> mias(Long clienteId, int n, int t) {
        return pagina(notificaciones.findByDestinatario(servicios.correo(clienteId).toLowerCase(),
                pagina(n, t, "fechaCreacion")), NotificacionRespuesta::desde);
    }

    /**
     * Bandeja administrativa del correo simulado (incluye codigos de recuperacion
     * para la demo).
     */
    @Transactional(readOnly = true)
    public Pagina<NotificacionRespuesta> todas(TipoNotificacion tipo, String destinatario, int n, int t) {
        Specification<Notificacion> spec = (root, q, cb) -> {
            var p = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (tipo != null)
                p.add(cb.equal(root.get("tipo"), tipo));
            if (destinatario != null && !destinatario.isBlank())
                p.add(cb.like(cb.lower(root.<String>get("destinatario")),
                        "%" + destinatario.strip().toLowerCase() + "%"));
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return pagina(notificaciones.findAll(spec, pagina(n, t, "fechaCreacion")), NotificacionRespuesta::desde);
    }

    // ------------------------------------------------------------ reabastecimiento
    // (HU-131)
    @Transactional
    public AvisoRespuesta solicitarAviso(Long clienteId, Long libroId) {
        var condicion = servicios.condicion(libroId);
        var registro = new RegistroAvisosStock(avisos.findByClienteIdAndActivaTrue(clienteId));
        var nueva = registro.solicitarAviso(clienteId, libroId, condicion.activo() ? condicion.stock() : 0)
                .orElseThrow(() -> new IllegalStateException("Ya pediste el aviso de este libro"));
        return AvisoRespuesta.desde(avisos.save(nueva));
    }

    @Transactional(readOnly = true)
    public List<AvisoRespuesta> misAvisos(Long clienteId) {
        return avisos.findByClienteIdAndActivaTrue(clienteId).stream().map(AvisoRespuesta::desde).toList();
    }

    @Transactional
    public void cancelarAviso(Long clienteId, Long libroId) {
        var activos = avisos.findByClienteIdAndActivaTrue(clienteId).stream()
                .filter(a -> a.getLibroId().equals(libroId)).toList();
        if (activos.isEmpty())
            throw new NoSuchElementException("No tienes un aviso activo para este libro");
        activos.forEach(SolicitudReabastecimiento::marcarAtendida);
    }

    /**
     * Interno (catalog): el libro volvio a tener stock -> correo a cada solicitante
     * y se cierran las solicitudes.
     */
    @Transactional
    public int libroDisponible(Long libroId, String titulo) {
        var pendientes = new RegistroAvisosStock(avisos.findByLibroIdAndActivaTrue(libroId)).listarPorLibro(libroId);
        if (pendientes.isEmpty())
            return 0;
        var correos = servicios.usuarios(pendientes.stream().map(SolicitudReabastecimiento::getClienteId).toList());
        int enviados = 0;
        for (var s : pendientes) {
            var u = correos.get(s.getClienteId());
            if (u != null) {
                Notificacion n = new Notificacion(u.email(), TipoNotificacion.REABASTECIMIENTO,
                        "Volvio a estar disponible: " + titulo,
                        "Buenas noticias: \"" + titulo + "\" ya tiene stock en The Quiet Chapter.");
                n.asignarEnlaceAccion(frontend + "/libros/" + libroId);
                despachar(n);
                enviados++;
            }
            s.marcarAtendida();
        }
        return enviados;
    }

    // ------------------------------------------------------------ boletin
    // (HU-132..139)
    @Transactional
    public Mensaje suscribir(String email) {
        String correo = email.strip().toLowerCase();
        var existente = suscripciones.findByEmail(correo);
        var s = new RegistroSuscriptores().registrarSuscripcion(existente, correo, token(), token())
                .orElseThrow(() -> new IllegalStateException("Este correo ya esta suscrito al boletin"));
        suscripciones.save(s);
        Notificacion n = new Notificacion(correo, TipoNotificacion.CONFIRMACION_SUSCRIPCION,
                "Confirma tu suscripcion al boletin",
                "Para confirmar tu suscripcion abre: " + frontend + "/boletin/confirmar?token="
                        + s.getTokenConfirmacion());
        n.asignarEnlaceAccion(frontend + "/boletin/confirmar?token=" + s.getTokenConfirmacion());
        despachar(n);
        return new Mensaje("Te enviamos un correo para confirmar la suscripcion");
    }

    @Transactional
    public Mensaje confirmar(String token) {
        var s = suscripciones.findByTokenConfirmacion(token)
                .orElseThrow(() -> new NoSuchElementException("Enlace de confirmacion invalido"));
        if (s.estaActiva())
            return new Mensaje("Tu suscripcion ya estaba confirmada");
        if (!s.confirmar(token))
            throw new IllegalStateException("Esta suscripcion fue cancelada: suscribete nuevamente");
        return new Mensaje("Suscripcion confirmada. Gracias por unirte al boletin");
    }

    @Transactional
    public Mensaje cancelar(String token) {
        var s = suscripciones.findByTokenCancelacion(token)
                .orElseThrow(() -> new NoSuchElementException("Enlace de cancelacion invalido"));
        if (!s.cancelar(token))
            return new Mensaje("Tu suscripcion ya estaba cancelada");
        return new Mensaje("Cancelaste tu suscripcion. No recibiras mas boletines");
    }

    @Transactional(readOnly = true)
    public Pagina<SuscriptorRespuesta> suscriptores(EstadoSuscripcion estado, int n, int t) {
        var pr = pagina(n, t, "fechaSuscripcion");
        return pagina(estado == null ? suscripciones.findAll(pr) : suscripciones.findByEstado(estado, pr),
                SuscriptorRespuesta::desde);
    }

    @Transactional
    public BoletinRespuesta crearBoletin(BoletinSolicitud s) {
        return BoletinRespuesta.desde(boletines.save(new Boletin(s.asunto(), s.contenido())));
    }

    private Boletin boletin(Long id) {
        return boletines.findById(id).orElseThrow(() -> new NoSuchElementException("Boletin no encontrado"));
    }

    @Transactional
    public BoletinRespuesta editarBoletin(Long id, BoletinSolicitud s) {
        Boletin b = boletin(id);
        if (!b.editar(s.asunto(), s.contenido()))
            throw new IllegalStateException("Un boletin enviado no se puede editar");
        return BoletinRespuesta.desde(b);
    }

    @Transactional
    public BoletinRespuesta enviarBoletin(Long id) {
        Boletin b = boletin(id);
        var correos = new RegistroBoletines().prepararEnvio(b, suscripciones.findByEstado(EstadoSuscripcion.CONFIRMADA),
                s -> frontend + "/boletin/cancelar?token=" + s.getTokenCancelacion());
        correos.forEach(this::despachar);
        b.marcarEnviado(correos.size());
        return BoletinRespuesta.desde(b);
    }

    @Transactional(readOnly = true)
    public Pagina<BoletinRespuesta> listarBoletines(EstadoBoletin estado, int n, int t) {
        var pr = pagina(n, t, "fechaCreacion");
        return pagina(estado == null ? boletines.findAll(pr) : boletines.findByEstado(estado, pr),
                BoletinRespuesta::desde);
    }
}
