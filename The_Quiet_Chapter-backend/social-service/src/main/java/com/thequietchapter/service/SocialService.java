package com.thequietchapter.service;

import com.thequietchapter.client.ServiciosHttpClient;
import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.CalificacionRepository;
import com.thequietchapter.repository.ListaFavoritosRepository;
import com.thequietchapter.repository.ResenaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Casos de uso DC-06 (EP-06 Fidelizacion y Experiencia Social). */
@Service
public class SocialService {
    private final ListaFavoritosRepository listas;
    private final ResenaRepository resenas;
    private final CalificacionRepository calificaciones;
    private final ServiciosHttpClient servicios;

    public SocialService(ListaFavoritosRepository listas, ResenaRepository resenas,
            CalificacionRepository calificaciones,
            ServiciosHttpClient servicios) {
        this.listas = listas;
        this.resenas = resenas;
        this.calificaciones = calificaciones;
        this.servicios = servicios;
    }

    // ------------------------------------------------------------ favoritos
    // (HU-075..079)
    private ListaFavoritos lista(Long clienteId) {
        return listas.findByClienteId(clienteId).orElseGet(() -> listas.saveAndFlush(new ListaFavoritos(clienteId)));
    }

    @Transactional
    public List<FavoritoRespuesta> favoritos(Long clienteId) {
        return lista(clienteId).getFavoritos().stream()
                .map(f -> new FavoritoRespuesta(f.getLibroId(), f.getFechaAgregado())).toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> idsFavoritos(Long clienteId) {
        return listas.findByClienteId(clienteId).map(ListaFavoritos::listarLibros).orElse(Set.of());
    }

    @Transactional
    public List<FavoritoRespuesta> agregarFavorito(Long clienteId, Long libroId) {
        ListaFavoritos l = lista(clienteId);
        if (!l.agregarLibro(libroId))
            throw new IllegalStateException("El libro ya esta en tus favoritos");
        listas.flush();
        return favoritos(clienteId);
    }

    @Transactional
    public List<FavoritoRespuesta> quitarFavorito(Long clienteId, Long libroId) {
        ListaFavoritos l = lista(clienteId);
        if (!l.quitarLibro(libroId))
            throw new NoSuchElementException("El libro no esta en tus favoritos");
        listas.flush();
        return favoritos(clienteId);
    }

    // ------------------------------------------------------------ opiniones
    // (HU-080..089)
    private RegistroOpiniones registro(Long libroId) {
        return new RegistroOpiniones(resenas.findByLibroId(libroId), calificaciones.findByLibroId(libroId));
    }

    @Transactional(readOnly = true)
    public OpinionesLibro opiniones(Long libroId, OrdenResenas orden, Integer puntuacion, Long clienteId) {
        RegistroOpiniones r = registro(libroId);
        List<Resena> lista = puntuacion == null ? r.ordenarResenas(libroId, orden)
                : r.ordenarResenas(libroId, orden).stream()
                        .filter(x -> r.filtrarPorCalificacion(libroId, puntuacion).contains(x)).toList();
        Map<Long, String> nombres = servicios.nombres(lista.stream().map(Resena::getClienteId).toList());
        boolean puedoOpinar = false;
        if (clienteId != null) {
            try {
                puedoOpinar = servicios.librosComprados(clienteId).contains(libroId);
            } catch (ResponseStatusException e) {
                puedoOpinar = false;
            }
        }
        return new OpinionesLibro(libroId, r.calcularPromedio(libroId), r.contarCalificaciones(libroId),
                clienteId == null ? null : r.puntuacionDe(clienteId, libroId).orElse(null), puedoOpinar,
                lista.stream().map(x -> respuesta(x, nombres, r, clienteId, false)).toList());
    }

    private ResenaRespuesta respuesta(Resena x, Map<Long, String> nombres, RegistroOpiniones r, Long yo,
            boolean admin) {
        return new ResenaRespuesta(x.getId(), x.getLibroId(), x.getClienteId(),
                nombres.getOrDefault(x.getClienteId(), "Cliente"),
                x.getContenido(), x.getFechaPublicacion(), x.getFechaActualizacion(), x.getFechaActualizacion() != null,
                x.getEstado().name(), x.contarVotosUtiles(),
                r == null ? null : r.puntuacionDe(x.getClienteId(), x.getLibroId()).orElse(null),
                x.marcadaUtilPor(yo), x.getClienteId().equals(yo),
                admin ? x.getReportes().stream().map(ReporteRespuesta::desde).toList() : List.of());
    }

    /**
     * HU-080 (+ HU-083 si viene puntuacion): solo con compra verificada y una
     * resena vigente por libro.
     */
    @Transactional
    public ResenaRespuesta publicar(Long clienteId, Long libroId, String contenido, Integer puntuacion) {
        boolean compro = servicios.librosComprados(clienteId).contains(libroId);
        if (!compro)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes resenar libros que hayas comprado");
        RegistroOpiniones r = registro(libroId);
        Resena nueva = new Resena(clienteId, libroId, contenido);
        if (!r.publicarResena(nueva, true))
            throw new IllegalStateException("Ya publicaste una resena de este libro: puedes editarla");
        resenas.saveAndFlush(nueva);
        if (puntuacion != null)
            guardarCalificacion(clienteId, libroId, puntuacion, r);
        return respuesta(nueva, servicios.nombres(List.of(clienteId)), registro(libroId), clienteId, false);
    }

    /** HU-083: crea o actualiza la calificacion. */
    @Transactional
    public OpinionesLibro calificar(Long clienteId, Long libroId, int puntuacion) {
        if (!servicios.librosComprados(clienteId).contains(libroId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes calificar libros que hayas comprado");
        guardarCalificacion(clienteId, libroId, puntuacion, registro(libroId));
        return opiniones(libroId, OrdenResenas.MAS_RECIENTES, null, clienteId);
    }

    private void guardarCalificacion(Long clienteId, Long libroId, int puntuacion, RegistroOpiniones r) {
        var existente = calificaciones.findByClienteIdAndLibroId(clienteId, libroId);
        if (existente.isPresent()) {
            if (!existente.get().actualizarPuntuacion(puntuacion))
                throw new IllegalArgumentException("La calificacion debe estar entre 1 y 5");
            calificaciones.flush();
        } else {
            Calificacion c = new Calificacion(clienteId, libroId, puntuacion);
            if (!r.registrarCalificacion(c, true))
                throw new IllegalArgumentException("Calificacion invalida");
            calificaciones.saveAndFlush(c);
        }
    }

    private Resena obtener(Long id) {
        return resenas.findById(id).orElseThrow(() -> new NoSuchElementException("Resena no encontrada"));
    }

    @Transactional
    public ResenaRespuesta editar(Long clienteId, Long id, String contenido) {
        Resena r = obtener(id);
        if (!r.editarTexto(clienteId, contenido))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede editar su resena");
        resenas.flush();
        return respuesta(r, servicios.nombres(List.of(clienteId)), registro(r.getLibroId()), clienteId, false);
    }

    @Transactional
    public void eliminarPropia(Long clienteId, Long id) {
        Resena r = obtener(id);
        if (!r.eliminarPorAutor(clienteId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede eliminar su resena");
        resenas.flush();
    }

    @Transactional
    public ResenaRespuesta marcarUtil(Long clienteId, Long id) {
        Resena r = obtener(id);
        if (r.getClienteId().equals(clienteId))
            throw new IllegalStateException("No puedes marcar como util tu propia resena");
        if (!r.marcarUtil(clienteId))
            throw new IllegalStateException("Ya marcaste esta resena como util");
        resenas.flush();
        return respuesta(r, servicios.nombres(List.of(r.getClienteId())), registro(r.getLibroId()), clienteId, false);
    }

    @Transactional
    public ResenaRespuesta quitarUtil(Long clienteId, Long id) {
        Resena r = obtener(id);
        if (!r.retirarMarcaUtil(clienteId))
            throw new IllegalStateException("No habias marcado esta resena como util");
        resenas.flush();
        return respuesta(r, servicios.nombres(List.of(r.getClienteId())), registro(r.getLibroId()), clienteId, false);
    }

    @Transactional
    public void reportar(Long clienteId, Long id, String motivo) {
        Resena r = obtener(id);
        if (!r.reportar(clienteId, motivo))
            throw new IllegalStateException("Ya reportaste esta resena o no se puede reportar");
        resenas.flush();
    }

    @Transactional(readOnly = true)
    public List<ResenaRespuesta> misResenas(Long clienteId) {
        var lista = resenas.findByClienteIdOrderByFechaPublicacionDesc(clienteId);
        var nombres = servicios.nombres(List.of(clienteId));
        return lista.stream().map(x -> respuesta(x, nombres, registro(x.getLibroId()), clienteId, false)).toList();
    }

    /** Promedio y cantidad de varias obras (tarjetas del catalogo). */
    @Transactional(readOnly = true)
    public List<Promedio> promedios(List<Long> libroIds) {
        if (libroIds == null || libroIds.isEmpty())
            return List.of();
        return calificaciones.promedios(new HashSet<>(libroIds)).stream()
                .map(f -> new Promedio((Long) f[0],
                        BigDecimal.valueOf(((Number) f[1]).doubleValue()).setScale(1, RoundingMode.HALF_UP),
                        ((Number) f[2]).longValue()))
                .toList();
    }

    // ------------------------------------------------------------ moderacion
    // (HU-090..093)
    @Transactional(readOnly = true)
    public List<ResenaRespuesta> reportadas() {
        var lista = resenas.findByEstadoOrderByFechaPublicacionAsc(EstadoResena.REPORTADA);
        var nombres = servicios.nombres(lista.stream().map(Resena::getClienteId).toList());
        return lista.stream().map(x -> respuesta(x, nombres, registro(x.getLibroId()), null, true)).toList();
    }

    @Transactional
    public ResenaRespuesta moderar(Long administradorId, Long id, EstadoReporte resultado) {
        Resena r = obtener(id);
        if (!r.resolverReporte(administradorId, resultado))
            throw new IllegalStateException("La resena no tiene reportes pendientes");
        resenas.flush();
        return respuesta(r, servicios.nombres(List.of(r.getClienteId())), registro(r.getLibroId()), null, true);
    }

    @Transactional(readOnly = true)
    public PaginaResenas todas(EstadoResena estado, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100)
            throw new IllegalArgumentException("Paginacion invalida");
        var pageable = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Resena> p = estado == null ? resenas.findAll(pageable) : resenas.findByEstado(estado, pageable);
        var nombres = servicios.nombres(p.getContent().stream().map(Resena::getClienteId).toList());
        return new PaginaResenas(
                p.getContent().stream().map(x -> respuesta(x, nombres, registro(x.getLibroId()), null, true)).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
