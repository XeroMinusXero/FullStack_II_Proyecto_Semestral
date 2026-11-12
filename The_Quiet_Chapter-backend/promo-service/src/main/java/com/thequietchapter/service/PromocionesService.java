package com.thequietchapter.service;

import com.thequietchapter.client.ServiciosHttpClient;
import com.thequietchapter.client.ServiciosHttpClient.LibroInterno;
import com.thequietchapter.dto.Dtos.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.CuponRepository;
import com.thequietchapter.repository.PromocionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/** Casos de uso de cupones (HU-094..102) y promociones (HU-103..113). */
@Service
public class PromocionesService {
    private final CuponRepository cupones;
    private final PromocionRepository promociones;
    private final ServiciosHttpClient servicios;

    public PromocionesService(CuponRepository cupones, PromocionRepository promociones, ServiciosHttpClient servicios) {
        this.cupones = cupones;
        this.promociones = promociones;
        this.servicios = servicios;
    }

    private static PageRequest pagina(int n, int t, String orden) {
        if (n < 0 || t < 1 || t > 100)
            throw new IllegalArgumentException("Paginacion invalida");
        return PageRequest.of(n, t, Sort.by(Sort.Direction.DESC, orden));
    }

    private static <A, B> Pagina<B> pagina(Page<A> p, java.util.function.Function<A, B> f) {
        return new Pagina<>(p.getContent().stream().map(f).toList(), p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages());
    }

    // ------------------------------------------------------------ cupones
    @Transactional(readOnly = true)
    public Pagina<CuponRespuesta> listarCupones(int n, int t) {
        return pagina(cupones.findAll(pagina(n, t, "fechaInicio")), CuponRespuesta::desde);
    }

    private Cupon cupon(Long id) {
        return cupones.findById(id).orElseThrow(() -> new NoSuchElementException("Cupon no encontrado"));
    }

    @Transactional
    public CuponRespuesta crearCupon(CuponSolicitud s) {
        if (!new RegistroCupones(cupones.findAll()).codigoDisponible(s.codigo(), null))
            throw new IllegalStateException("Ya existe un cupon con ese codigo");
        Cupon c = new Cupon(s.codigo(), s.tipoDescuento(), s.valor(), s.montoMinimo(), s.fechaInicio(), s.fechaFin());
        return CuponRespuesta.desde(cupones.saveAndFlush(c));
    }

    @Transactional
    public CuponRespuesta editarCupon(Long id, CuponSolicitud s) {
        Cupon c = cupon(id);
        if (cupones.existsByCodigoAndIdNot(Cupon.normalizar(s.codigo()), id))
            throw new IllegalStateException("Ya existe un cupon con ese codigo");
        c.cambiarCodigo(s.codigo());
        c.editarCondiciones(s.tipoDescuento(), s.valor(), s.montoMinimo(), s.fechaInicio(), s.fechaFin());
        cupones.flush();
        return CuponRespuesta.desde(c);
    }

    @Transactional
    public CuponRespuesta estadoCupon(Long id, boolean activo) {
        Cupon c = cupon(id);
        c.cambiarEstado(activo);
        cupones.flush();
        return CuponRespuesta.desde(c);
    }

    @Transactional
    public void eliminarCupon(Long id) {
        Cupon c = cupon(id);
        if (!new RegistroCupones(List.of(c)).puedeEliminar(c))
            throw new IllegalStateException("El cupon tiene canjes registrados: desactivalo en lugar de eliminarlo");
        cupones.delete(c);
    }

    @Transactional(readOnly = true)
    public List<CanjeRespuesta> canjes(Long id) {
        return cupon(id).listarCanjes().stream().map(CanjeRespuesta::desde).toList();
    }

    /**
     * Interno (cart): valida y calcula el descuento; si no es canjeable, informa el
     * motivo (HU-100).
     */
    @Transactional(readOnly = true)
    public CuponValidado validar(String codigo, BigDecimal subtotal) {
        Cupon c = cupones.findByCodigo(Cupon.normalizar(codigo))
                .orElseThrow(() -> new NoSuchElementException("El cupon no existe"));
        c.motivoRechazo(subtotal, LocalDateTime.now()).ifPresent(m -> {
            throw new IllegalStateException(m);
        });
        return new CuponValidado(c.getId(), c.getCodigo(), c.calcularDescuento(subtotal));
    }

    /**
     * Interno (checkout): registra el canje tras el pago aprobado. Idempotente por
     * pedido.
     */
    @Transactional
    public CanjeRespuesta registrarCanje(Long cuponId, CanjeSolicitud s) {
        Cupon c = cupon(cuponId);
        CanjeCupon canje = c.registrarCanje(s.clienteId(), s.pedidoId(), s.descuento());
        cupones.flush();
        return CanjeRespuesta.desde(canje);
    }

    // ------------------------------------------------------------ promociones
    @Transactional(readOnly = true)
    public Pagina<PromocionRespuesta> listarPromociones(int n, int t) {
        return pagina(promociones.findAll(pagina(n, t, "fechaInicio")), PromocionRespuesta::desde);
    }

    private Promocion promocion(Long id) {
        return promociones.findById(id).orElseThrow(() -> new NoSuchElementException("Promocion no encontrada"));
    }

    private static List<AlcancePromocion> alcances(PromocionSolicitud s) {
        return s.alcances().stream().map(a -> new AlcancePromocion(a.tipo(), a.referenciaId())).toList();
    }

    @Transactional
    public PromocionRespuesta crearPromocion(PromocionSolicitud s) {
        Promocion p = new Promocion(s.nombre(), s.tipoDescuento(), s.valor(), s.fechaInicio(), s.fechaFin());
        p.reemplazarAlcances(alcances(s));
        p.cambiarEstado(s.activo() == null || s.activo());
        return PromocionRespuesta.desde(promociones.saveAndFlush(p));
    }

    @Transactional
    public PromocionRespuesta editarPromocion(Long id, PromocionSolicitud s) {
        Promocion p = promocion(id);
        p.renombrar(s.nombre());
        p.editarDescuento(s.tipoDescuento(), s.valor());
        p.definirVigencia(s.fechaInicio(), s.fechaFin());
        p.reemplazarAlcances(alcances(s));
        if (s.activo() != null)
            p.cambiarEstado(s.activo());
        promociones.flush();
        return PromocionRespuesta.desde(p);
    }

    @Transactional
    public PromocionRespuesta estadoPromocion(Long id, boolean activo) {
        Promocion p = promocion(id);
        p.cambiarEstado(activo);
        promociones.flush();
        return PromocionRespuesta.desde(p);
    }

    @Transactional
    public void eliminarPromocion(Long id) {
        Promocion p = promocion(id);
        if (p.tieneAplicaciones())
            throw new IllegalStateException("La promocion ya se aplico en pedidos: desactivala en lugar de eliminarla");
        promociones.delete(p);
    }

    // ------------------------------------------------------------ precios con
    // promocion
    private RegistroPromociones vigentes() {
        return new RegistroPromociones(promociones.findByActivoTrue());
    }

    private Optional<PrecioPromo> precio(RegistroPromociones r, LibroInterno l, BigDecimal precioLista,
            LocalDateTime ahora) {
        return r.mejorPara(l.id(), l.categoriaId(), l.generoIds(), l.editorialId(), precioLista, ahora).map(p -> {
            BigDecimal d = p.calcularDescuento(precioLista);
            BigDecimal fin = precioLista.subtract(d).max(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);
            int pct = d.multiply(BigDecimal.valueOf(100)).divide(precioLista, 0, RoundingMode.HALF_UP).intValue();
            return new PrecioPromo(l.id(), precioLista, fin, d, pct, p.getNombre(), p.getFechaFin());
        }).filter(x -> x.descuento().signum() > 0);
    }

    /**
     * HU-111/113: precio con descuento para tarjetas y detalle (solo libros con
     * promocion vigente).
     */
    @Transactional(readOnly = true)
    public List<PrecioPromo> precios(List<Long> libroIds) {
        if (libroIds == null || libroIds.isEmpty())
            return List.of();
        var r = vigentes();
        var ahora = LocalDateTime.now();
        return servicios.libros(libroIds).stream().map(l -> precio(r, l, l.precio(), ahora)).flatMap(Optional::stream)
                .toList();
    }

    /** Interno (cart): precio final de lista -> con promocion. */
    @Transactional(readOnly = true)
    public List<PrecioPromo> preciosInternos(List<PrecioLista> lista) {
        if (lista == null || lista.isEmpty())
            return List.of();
        var r = vigentes();
        if (r.listarVigentes(LocalDateTime.now()).isEmpty())
            return List.of();
        Map<Long, BigDecimal> precios = new HashMap<>();
        lista.forEach(x -> precios.put(x.libroId(), x.precio()));
        var ahora = LocalDateTime.now();
        return servicios.libros(precios.keySet()).stream().map(l -> precio(r, l, precios.get(l.id()), ahora))
                .flatMap(Optional::stream).toList();
    }

    /** HU-112: seccion de ofertas temporales. */
    @Transactional(readOnly = true)
    public List<Oferta> ofertas() {
        var r = vigentes();
        var ahora = LocalDateTime.now();
        if (r.listarVigentes(ahora).isEmpty())
            return List.of();
        List<Oferta> res = new ArrayList<>();
        for (LibroInterno l : servicios.libros(List.of())) {
            if (!l.activo())
                continue;
            precio(r, l, l.precio(), ahora)
                    .ifPresent(p -> res.add(new Oferta(l.id(), l.titulo(), l.autores(), l.portadaUrl(), l.disponible(),
                            p.precioOriginal(), p.precioFinal(), p.porcentaje(), p.promocion(), p.vigenteHasta())));
        }
        res.sort(Comparator.comparing(Oferta::porcentaje).reversed());
        return res;
    }

    /**
     * Interno (checkout): registra AplicacionPromocion por linea vendida con
     * promocion vigente (regla de eliminacion HU-107).
     */
    @Transactional
    public void registrarAplicaciones(AplicacionesSolicitud s) {
        var r = vigentes();
        var ahora = LocalDateTime.now();
        Map<Long, Integer> cantidades = new HashMap<>();
        s.lineas().forEach(x -> cantidades.merge(x.libroId(), x.cantidad(), Integer::sum));
        for (LibroInterno l : servicios.libros(cantidades.keySet())) {
            r.mejorPara(l.id(), l.categoriaId(), l.generoIds(), l.editorialId(), l.precio(), ahora)
                    .ifPresent(p -> p.registrarAplicacion(s.pedidoId(), l.id(),
                            p.calcularDescuento(l.precio()).multiply(BigDecimal.valueOf(cantidades.get(l.id())))));
        }
        promociones.flush();
    }
}
