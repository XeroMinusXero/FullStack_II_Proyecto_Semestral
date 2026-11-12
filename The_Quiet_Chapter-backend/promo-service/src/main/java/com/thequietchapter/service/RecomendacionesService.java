package com.thequietchapter.service;

import com.thequietchapter.client.ServiciosHttpClient;
import com.thequietchapter.client.ServiciosHttpClient.LibroInterno;
import com.thequietchapter.dto.Dtos.LibroSugerido;
import com.thequietchapter.model.LibroRecomendable;
import com.thequietchapter.model.PerfilPreferencias;
import com.thequietchapter.model.RecomendadorLibros;
import com.thequietchapter.repository.PerfilPreferenciasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HU-114..120: recomendaciones a partir del catalogo, la popularidad (ventas) y
 * el perfil del cliente.
 */
@Service
public class RecomendacionesService {
    private final PerfilPreferenciasRepository perfiles;
    private final ServiciosHttpClient servicios;

    public RecomendacionesService(PerfilPreferenciasRepository perfiles, ServiciosHttpClient servicios) {
        this.perfiles = perfiles;
        this.servicios = servicios;
    }

    private record Contexto(Map<Long, LibroInterno> libros, List<LibroRecomendable> candidatos) {
        List<LibroSugerido> sugeridos(List<Long> ids) {
            return ids.stream().map(libros::get).filter(Objects::nonNull)
                    .map(l -> new LibroSugerido(l.id(), l.titulo(), l.autores(), l.precio(), l.portadaUrl(),
                            l.disponible(), l.categoriaId()))
                    .toList();
        }
    }

    private Contexto contexto() {
        List<LibroInterno> libros = servicios.libros(List.of());
        Map<Long, Integer> ventas = servicios.unidadesVendidas();
        LocalDateTime hace30 = LocalDateTime.now().minusDays(30);
        List<LibroRecomendable> c = libros.stream().filter(LibroInterno::activo).map(l -> new LibroRecomendable(l.id(),
                new HashSet<>(l.autorIds()), l.categoriaId(), new HashSet<>(l.generoIds()), l.disponible(),
                l.fechaRegistro() != null && l.fechaRegistro().isAfter(hace30), ventas.getOrDefault(l.id(), 0)))
                .toList();
        return new Contexto(libros.stream().collect(Collectors.toMap(LibroInterno::id, Function.identity())), c);
    }

    private RecomendadorLibros recomendador(Long clienteId) {
        RecomendadorLibros r = new RecomendadorLibros();
        if (clienteId != null)
            perfiles.findByClienteId(clienteId).ifPresent(r::agregarPerfil);
        return r;
    }

    private static int limite(int l) {
        return Math.max(1, Math.min(l, 24));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> inicio(Long clienteId, int limite) {
        Contexto c = contexto();
        if (clienteId == null)
            return c.sugeridos(new RecomendadorLibros().recomendarGenerales(c.candidatos(), limite(limite)));
        return c.sugeridos(recomendador(clienteId).recomendarInicio(clienteId, servicios.librosComprados(clienteId),
                c.candidatos(), limite(limite)));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> perfil(Long clienteId, int limite) {
        Contexto c = contexto();
        return c.sugeridos(recomendador(clienteId).recomendarPerfil(clienteId, servicios.librosComprados(clienteId),
                c.candidatos(), limite(limite)));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> segunCompras(Long clienteId, int limite) {
        Contexto c = contexto();
        return c.sugeridos(new RecomendadorLibros().recomendarSegunCompras(servicios.librosComprados(clienteId),
                c.candidatos(), limite(limite)));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> porCategoria(Long categoriaId, int limite) {
        Contexto c = contexto();
        return c.sugeridos(
                new RecomendadorLibros().recomendarPorCategoria(categoriaId, c.candidatos(), limite(limite)));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> relacionados(Long libroId, int limite) {
        Contexto c = contexto();
        return c.sugeridos(new RecomendadorLibros().recomendarRelacionados(libroId, c.candidatos(), limite(limite)));
    }

    @Transactional(readOnly = true)
    public List<LibroSugerido> complementarios(List<Long> carrito, int limite) {
        Contexto c = contexto();
        return c.sugeridos(new RecomendadorLibros().recomendarComplementarios(
                new HashSet<>(carrito == null ? List.of() : carrito), c.candidatos(), limite(limite)));
    }

    /**
     * Registra la visita del cliente al detalle de un libro (alimenta HU-114/115).
     */
    @Transactional
    public void registrarVisita(Long clienteId, Long libroId) {
        PerfilPreferencias p = perfiles.findByClienteId(clienteId)
                .orElseGet(() -> perfiles.save(new PerfilPreferencias(clienteId)));
        p.registrarVisita(libroId);
        servicios.libros(List.of(libroId)).stream().findFirst()
                .ifPresent(l -> p.registrarInteresCategoria(l.categoriaId()));
        perfiles.flush();
    }
}
