package com.thequietchapter.service;

import com.thequietchapter.dto.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.LibroEspecificaciones;
import com.thequietchapter.repository.LibroRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * Casos de uso de DC-01. Las reglas siguen en Libro, Inventario, FichaLibro y
 * CriteriosCatalogo;
 * las consultas se ejecutan en MySQL (paginadas) en vez de cargar todo el
 * catalogo en memoria.
 */
@Service
public class CatalogoService {
    public static final int MIN_CARACTERES_SUGERENCIA = 2;
    private final LibroRepository repository;
    private final EntityManager entityManager;
    private final com.thequietchapter.client.NotificacionesHttpClient notificaciones;

    public CatalogoService(LibroRepository repository, EntityManager entityManager,
            com.thequietchapter.client.NotificacionesHttpClient notificaciones) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.notificaciones = notificaciones;
    }

    // ---------------------------------------------------------------- consultas
    // publicas y admin
    @Transactional(readOnly = true)
    public long contarLibros() {
        return repository.count();
    }

    /**
     * HU-001..HU-010: texto + filtros + orden + paginacion en una sola consulta.
     */
    @Transactional(readOnly = true)
    public Page<LibroResumen> buscar(String texto, CriteriosCatalogo criterios, OrdenCatalogo orden, int pagina,
            int tamanio) {
        if (!criterios.esRangoPrecioValido())
            throw new IllegalArgumentException("El precio minimo no puede ser superior al maximo");
        if (pagina < 0 || tamanio < 1 || tamanio > 100)
            throw new IllegalArgumentException("Paginacion invalida");
        Pageable pageable = PageRequest.of(pagina, tamanio, orden(orden));
        return repository.findAll(LibroEspecificaciones.segun(texto, criterios), pageable).map(LibroResumen::desde);
    }

    /**
     * HU-004: hasta {limite} sugerencias desde {MIN_CARACTERES_SUGERENCIA}
     * caracteres.
     */
    @Transactional(readOnly = true)
    public List<LibroResumen> sugerir(String texto, int limite) {
        if (texto == null || texto.strip().length() < MIN_CARACTERES_SUGERENCIA)
            return List.of();
        var criterios = new CriteriosCatalogo(null, null, null, null, true, false, null, null);
        return buscar(texto, criterios, OrdenCatalogo.TITULO_ASC, 0, Math.max(1, Math.min(limite, 10))).getContent();
    }

    private static Sort orden(OrdenCatalogo orden) {
        if (orden == null)
            return Sort.by("titulo").ascending().and(Sort.by("id"));
        return switch (orden) {
            case PRECIO_ASC -> Sort.by("precio").ascending().and(Sort.by("id"));
            case PRECIO_DESC -> Sort.by("precio").descending().and(Sort.by("id"));
            case TITULO_ASC -> Sort.by("titulo").ascending().and(Sort.by("id"));
            case NOVEDADES -> Sort.by("fechaRegistro").descending().and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }

    @Transactional(readOnly = true)
    public LibroDetalle detalle(Long id, boolean incluirInactivos) {
        return LibroDetalle.desde(obligatorio(id, incluirInactivos));
    }

    /**
     * Para cart-service: un solo request con todos los libros del carrito (incluye
     * inactivos).
     */
    @Transactional(readOnly = true)
    public List<CondicionLibroRespuesta> condiciones(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        if (ids.size() > 200)
            throw new IllegalArgumentException("Maximo 200 libros por consulta");
        return repository.findByIdIn(new LinkedHashSet<>(ids)).stream().map(CondicionLibroRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public AutorRespuesta autor(Long id) {
        Autor a = entityManager.find(Autor.class, id);
        if (a == null)
            throw new NoSuchElementException("Autor no encontrado");
        return AutorRespuesta.desde(a);
    }

    @Transactional
    public AutorRespuesta actualizarBiografia(Long id, String biografia) {
        Autor a = entityManager.find(Autor.class, id);
        if (a == null)
            throw new NoSuchElementException("Autor no encontrado");
        a.actualizarBiografia(biografia);
        return AutorRespuesta.desde(a);
    }

    @Transactional(readOnly = true)
    public List<ClasificacionRespuesta> listarCategorias() {
        return clasificaciones("Categoria");
    }

    @Transactional(readOnly = true)
    public List<ClasificacionRespuesta> listarGeneros() {
        return clasificaciones("Genero");
    }

    @Transactional(readOnly = true)
    public List<ClasificacionRespuesta> listarAutores() {
        return clasificaciones("Autor");
    }

    @Transactional(readOnly = true)
    public List<ClasificacionRespuesta> listarEditoriales() {
        return clasificaciones("Editorial");
    }

    private List<ClasificacionRespuesta> clasificaciones(String entidad) {
        return entityManager
                .createQuery("select new com.thequietchapter.dto.ClasificacionRespuesta(e.id, e.nombre) from "
                        + entidad + " e order by e.nombre", ClasificacionRespuesta.class)
                .getResultList();
    }

    // ---------------------------------------------------------------- mantenedor
    // (ADMINISTRADOR)
    @Transactional
    public LibroDetalle registrar(LibroSolicitud s) {
        if (s.precio() == null)
            throw new IllegalArgumentException("Precio obligatorio");
        if (repository.existsByIsbnIgnoreCase(s.isbn().strip()))
            throw new IllegalStateException("ISBN ya registrado");
        var libro = new Libro(s.isbn(), s.titulo(), s.precio(),
                new Inventario(s.stockInicial() == null ? 0 : s.stockInicial()));
        aplicarFichaYClasificaciones(libro, s);
        return LibroDetalle.desde(repository.saveAndFlush(libro));
    }

    @Transactional
    public LibroDetalle actualizar(Long id, LibroSolicitud s) {
        Libro libro = obligatorio(id, true);
        if (repository.existsByIsbnIgnoreCaseAndIdNot(s.isbn().strip(), id))
            throw new IllegalStateException("ISBN ya registrado en otro libro");
        libro.actualizarIdentificacion(s.titulo(), s.isbn());
        if (s.precio() != null)
            libro.actualizarPrecio(s.precio());
        aplicarFichaYClasificaciones(libro, s);
        repository.flush();
        return LibroDetalle.desde(libro);
    }

    private void aplicarFichaYClasificaciones(Libro libro, LibroSolicitud s) {
        libro.getFicha().actualizarInformacion(new FichaLibro(s.sinopsis(), s.formato(),
                s.paginas() == null ? 0 : s.paginas(), s.idioma(),
                s.anioPublicacion() == null ? 0 : s.anioPublicacion(),
                s.encuadernacion(), s.portadaUrl(), s.extractoUrl()));
        libro.asignarCategoria(reusar(Categoria.class, s.categoria(), () -> new Categoria(s.categoria())));
        libro.asignarEditorial(reusar(Editorial.class, s.editorial(), () -> new Editorial(s.editorial())));
        libro.reemplazarAutores(s.autores().stream().map(String::strip).distinct()
                .map(a -> reusar(Autor.class, a, () -> new Autor(a, ""))).toList());
        libro.reemplazarGeneros(s.generos() == null ? List.of()
                : s.generos().stream().map(String::strip).distinct()
                        .map(g -> reusar(Genero.class, g, () -> new Genero(g))).toList());
    }

    @Transactional
    public LibroDetalle actualizarPrecio(Long id, BigDecimal nuevoPrecio) {
        Libro libro = obligatorio(id, true);
        libro.actualizarPrecio(nuevoPrecio);
        repository.flush();
        return LibroDetalle.desde(libro);
    }

    @Transactional
    public LibroDetalle cambiarEstado(Long id, boolean activo) {
        Libro libro = obligatorio(id, true);
        libro.cambiarEstado(activo);
        repository.flush();
        return LibroDetalle.desde(libro);
    }

    /**
     * Regla DC-01 Catalogo.eliminarLibro: con pedidos asociados se rechaza y se
     * sugiere desactivar.
     */
    @Transactional
    public void eliminar(Long id, boolean tienePedidos) {
        Libro libro = obligatorio(id, true);
        if (tienePedidos)
            throw new IllegalStateException("El libro tiene pedidos asociados: desactivalo en lugar de eliminarlo");
        repository.delete(libro);
        repository.flush();
    }

    @Transactional
    public MovimientoStockRespuesta actualizarStock(Long id, int nuevoStock, Long responsableId) {
        Libro libro = bloquearYObtener(id);
        int antes = libro.getInventario().consultarStock();
        var m = libro.getInventario().actualizarStock(nuevoStock, responsableId);
        repository.flush(); // entidad administrada: flush persiste el movimiento nuevo (misma instancia,
                            // con id)
        if (antes == 0 && libro.getInventario().consultarStock() > 0)
            notificaciones.libroDisponible(libro.getId(), libro.getTitulo());
        return MovimientoStockRespuesta.desde(m);
    }

    @Transactional
    public MovimientoStockRespuesta ajustarStock(Long id, int nuevoStock, String motivo, Long responsableId) {
        Libro libro = bloquearYObtener(id);
        int antes = libro.getInventario().consultarStock();
        var m = libro.getInventario().registrarAjuste(nuevoStock, motivo, responsableId);
        repository.flush(); // entidad administrada: flush persiste el movimiento nuevo (misma instancia,
                            // con id)
        if (antes == 0 && libro.getInventario().consultarStock() > 0)
            notificaciones.libroDisponible(libro.getId(), libro.getTitulo());
        return MovimientoStockRespuesta.desde(m);
    }

    @Transactional
    public MovimientoStockRespuesta registrarIngreso(Long id, int cantidad, Long responsableId) {
        Libro libro = bloquearYObtener(id);
        int antes = libro.getInventario().consultarStock();
        var m = libro.getInventario().registrarIngreso(cantidad, responsableId);
        repository.flush(); // entidad administrada: flush persiste el movimiento nuevo (misma instancia,
                            // con id)
        if (antes == 0 && libro.getInventario().consultarStock() > 0)
            notificaciones.libroDisponible(libro.getId(), libro.getTitulo());
        return MovimientoStockRespuesta.desde(m);
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockRespuesta> consultarMovimientos(Long id) {
        return obligatorio(id, true).getInventario().consultarHistorial().stream()
                .sorted(Comparator.comparing(MovimientoInventario::getFecha).reversed())
                .map(MovimientoStockRespuesta::desde).toList();
    }

    // ---------------------------------------------------------------- interno
    // (checkout / order / report / promo)
    /**
     * Descuenta TODAS las lineas o ninguna. Idempotente: la misma referencia no
     * descuenta dos veces.
     */
    @Transactional
    public void descontarLote(LineasStockSolicitud s) {
        String motivo = "Venta " + s.referencia().strip();
        if (existeMovimiento(TipoMovimiento.VENTA, motivo))
            return;
        for (var linea : agrupar(s)) {
            Libro libro = bloquearYObtener(linea.libroId());
            if (!libro.isActivo())
                throw new IllegalStateException("El libro '" + libro.getTitulo() + "' ya no esta disponible");
            if (libro.getInventario().consultarStock() < linea.cantidad())
                throw new IllegalStateException("Stock insuficiente para '" + libro.getTitulo() + "'");
            libro.getInventario().descontarStock(linea.cantidad(), motivo);
        }
        repository.flush();
    }

    /**
     * Compensacion o cancelacion: repone TODAS las lineas. Idempotente por
     * referencia.
     */
    @Transactional
    public void reponerLote(LineasStockSolicitud s) {
        String motivo = "Reposicion " + s.referencia().strip();
        if (existeMovimiento(TipoMovimiento.REPOSICION_CANCELACION, motivo))
            return;
        for (var linea : agrupar(s)) {
            Libro libro = bloquearYObtener(linea.libroId());
            libro.getInventario().reponerStock(linea.cantidad(), motivo);
        }
        repository.flush();
    }

    /**
     * Suma lineas repetidas y ordena por id para bloquear filas siempre en el mismo
     * orden (evita deadlocks).
     */
    private static List<LineasStockSolicitud.Linea> agrupar(LineasStockSolicitud s) {
        Map<Long, Integer> total = new TreeMap<>();
        s.lineas().forEach(l -> total.merge(l.libroId(), l.cantidad(), Integer::sum));
        return total.entrySet().stream().map(e -> new LineasStockSolicitud.Linea(e.getKey(), e.getValue())).toList();
    }

    private boolean existeMovimiento(TipoMovimiento tipo, String motivo) {
        return entityManager
                .createQuery("select count(m) from MovimientoInventario m where m.tipo = :t and m.motivo = :m",
                        Long.class)
                .setParameter("t", tipo).setParameter("m", motivo).getSingleResult() > 0;
    }

    @Transactional(readOnly = true)
    public List<LibroInterno> todosInterno(List<Long> ids) {
        var libros = (ids == null || ids.isEmpty()) ? repository.findAll(Sort.by("id"))
                : repository.findByIdIn(new LinkedHashSet<>(ids));
        return libros.stream().map(LibroInterno::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoConsolidado> movimientosEntre(LocalDate desde, LocalDate hasta) {
        LocalDateTime d = desde == null ? LocalDate.of(2000, 1, 1).atStartOfDay() : desde.atStartOfDay();
        LocalDateTime h = hasta == null ? LocalDateTime.now().plusDays(1) : hasta.plusDays(1).atStartOfDay();
        List<Object[]> filas = entityManager.createQuery(
                "select m.id, l.id, l.titulo, m.fecha, m.tipo, m.cantidad, m.responsableId " +
                        "from Libro l join l.inventario i join i.movimientos m where m.fecha >= :d and m.fecha < :h order by m.fecha desc",
                Object[].class).setParameter("d", d).setParameter("h", h).getResultList();
        return filas.stream().map(f -> new MovimientoConsolidado((Long) f[0], (Long) f[1], (String) f[2],
                (LocalDateTime) f[3], ((TipoMovimiento) f[4]).name(), (Integer) f[5], (Long) f[6])).toList();
    }

    // ---------------------------------------------------------------- utilidades
    private Libro obligatorio(Long id, boolean incluirInactivos) {
        Libro libro = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Libro no encontrado"));
        if (!incluirInactivos && !libro.isActivo())
            throw new NoSuchElementException("Libro no encontrado");
        return libro;
    }

    private Libro bloquearYObtener(Long id) {
        if (repository.bloquearInventario(id).isEmpty())
            throw new NoSuchElementException("Libro no encontrado: " + id);
        return obligatorio(id, true);
    }

    private <T> T reusar(Class<T> tipo, String nombre, Supplier<T> proveedor) {
        return entityManager.createQuery("select e from " + tipo.getSimpleName()
                + " e where lower(e.nombre) = lower(:nombre)", tipo)
                .setParameter("nombre", nombre.strip())
                .getResultStream().findFirst().orElseGet(() -> {
                    T entidad = proveedor.get();
                    entityManager.persist(entidad);
                    return entidad;
                });
    }
}
