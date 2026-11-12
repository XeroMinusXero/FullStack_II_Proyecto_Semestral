package com.thequietchapter.service;

import com.thequietchapter.client.ServiciosHttpClient;
import com.thequietchapter.dto.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.PedidoRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {
    private final PedidoRepository repository;
    private final ServiciosHttpClient servicios;

    public PedidoService(PedidoRepository repository, ServiciosHttpClient servicios) {
        this.repository = repository;
        this.servicios = servicios;
    }

    private static Pageable pagina(int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100)
            throw new IllegalArgumentException("Página >= 0 y tamaño entre 1 y 100");
        return PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("id")));
    }

    private Pedido propio(Long id, Long clienteId) {
        Pedido p = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
        if (!p.getClienteId().equals(clienteId))
            throw new NoSuchElementException("Pedido no encontrado");
        return p;
    }

    private Pedido bloquear(Long id) {
        return repository.bloquear(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
    }

    /**
     * Solo el adaptador interno autorizado debe confirmar un pago; mientras tanto,
     * dev local explícito.
     */
    @Transactional
    public PedidoRespuesta crear(CrearPedidoSolicitud s) {
        var existente = repository.findByReferenciaPago(s.referenciaPago().trim());
        if (existente.isPresent()) {
            Pedido p = existente.get();
            if (!p.getClienteId().equals(s.clienteId()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Referencia de pago ya utilizada");
            // No aceptar una referencia repetida con líneas o importes distintos.
            if (!coincideSolicitud(p, s))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Referencia de pago usada con otro contenido");
            return PedidoRespuesta.desde(p);
        }
        List<DetallePedido> detalles = s.detalles().stream()
                .map(d -> new DetallePedido(d.libroId(), d.tituloLibro(), d.cantidad(), d.precioUnitario())).toList();
        var e = s.entrega();
        var i = s.importes();
        var entrega = new DatosEntregaPedido(e.calle(), e.numero(), e.comuna(), e.region(), e.metodoEnvio(),
                e.plazoEstimadoDias());
        var importes = new ImportesPedido(i.subtotal(), i.costoEnvio(), i.impuestos(), i.descuento());
        // RegistroPedidos sigue siendo la clase de negocio del DC-04; JPA conserva la
        // propiedad de los datos.
        Pedido pedido = new RegistroPedidos().crearPedido(s.clienteId(), detalles, entrega, importes, s.medioPago(),
                s.referenciaPago());
        repository.saveAndFlush(pedido);
        pedido.completarNumeroPersistente();
        repository.flush();
        servicios.notificar(pedido.getClienteId(), "CONFIRMACION_COMPRA",
                "Confirmacion de tu pedido " + pedido.getNumeroPedido(),
                "Recibimos tu pedido " + pedido.getNumeroPedido() + " por un total de $"
                        + pedido.getImportes().calcularTotal().toPlainString()
                        + ". Entrega en: " + pedido.getEntrega().formatearDireccion() + ".");
        servicios.notificar(pedido.getClienteId(), "COMPROBANTE_COMPRA",
                "Comprobante de compra " + pedido.getNumeroPedido(), comprobante(pedido));
        return PedidoRespuesta.desde(pedido);
    }

    private static boolean coincideSolicitud(Pedido p, CrearPedidoSolicitud s) {
        if (p.getDetalles().size() != s.detalles().size())
            return false;
        if (!p.getMedioPago().equals(s.medioPago().trim()))
            return false;
        var importes = p.getImportes();
        var i = s.importes();
        if (importes.getSubtotal().compareTo(i.subtotal()) != 0
                || importes.getCostoEnvio().compareTo(i.costoEnvio()) != 0 ||
                importes.getImpuestos().compareTo(i.impuestos()) != 0
                || importes.getDescuento().compareTo(i.descuento()) != 0)
            return false;
        var e = p.getEntrega();
        var de = s.entrega();
        if (!e.getCalle().equals(de.calle().trim()) || !e.getNumero().equals(de.numero().trim()) ||
                !e.getComuna().equals(de.comuna().trim()) || !e.getRegion().equals(de.region().trim()) ||
                !e.getMetodoEnvio().equals(de.metodoEnvio().trim())
                || e.getPlazoEstimadoDias() != de.plazoEstimadoDias())
            return false;
        for (int n = 0; n < s.detalles().size(); n++) {
            var actual = p.getDetalles().get(n);
            var entrada = s.detalles().get(n);
            if (!actual.getLibroId().equals(entrada.libroId())
                    || !actual.getTituloLibro().equals(entrada.tituloLibro().trim()) ||
                    actual.getCantidad() != entrada.cantidad()
                    || actual.getPrecioUnitario().compareTo(entrada.precioUnitario()) != 0)
                return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public PedidoRespuesta consultarPropio(Long clienteId, Long id) {
        return PedidoRespuesta.desde(propio(id, clienteId));
    }

    @Transactional(readOnly = true)
    public PedidoRespuesta buscarPorNumero(Long clienteId, String numero) {
        Pedido p = repository.findByNumeroPedido(numero)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
        if (!p.getClienteId().equals(clienteId))
            throw new NoSuchElementException("Pedido no encontrado");
        return PedidoRespuesta.desde(p);
    }

    @Transactional(readOnly = true)
    public PaginaPedidosRespuesta misPedidos(Long clienteId, int n, int size) {
        return PaginaPedidosRespuesta.desde(repository.findByClienteId(clienteId, pagina(n, size)));
    }

    @Transactional(readOnly = true)
    public PaginaPedidosRespuesta filtrar(Long clienteId, CriteriosPedido criterios, int n, int size) {
        Specification<Pedido> filtros = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (clienteId != null)
                condiciones.add(cb.equal(root.get("clienteId"), clienteId));
            if (criterios.getEstado() != null)
                condiciones.add(cb.equal(root.get("estado"), criterios.getEstado()));
            if (criterios.getFechaDesde() != null)
                condiciones.add(
                        cb.greaterThanOrEqualTo(root.get("fechaCreacion"), criterios.getFechaDesde().atStartOfDay()));
            if (criterios.getFechaHasta() != null)
                condiciones.add(
                        cb.lessThan(root.get("fechaCreacion"), criterios.getFechaHasta().plusDays(1).atStartOfDay()));
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return PaginaPedidosRespuesta.desde(repository.findAll(filtros, pagina(n, size)));
    }

    @Transactional
    public PedidoRespuesta cambiarEstado(Long id, EstadoPedido estado) {
        if (estado == EstadoPedido.CANCELADO)
            return cancelarAdmin(id);
        Pedido p = bloquear(id);
        EstadoPedido anterior = p.getEstado();
        if (!p.avanzarEstado(estado))
            throw new IllegalStateException("Transicion de estado no permitida desde " + p.getEstado());
        repository.saveAndFlush(p);
        String tipo = switch (estado) {
            case ENVIADO -> "DESPACHO";
            case ENTREGADO -> "ENTREGA";
            default -> "CAMBIO_ESTADO_PEDIDO";
        };
        String extra = estado == EstadoPedido.ENTREGADO ? " Te invitamos a calificar los libros que compraste."
                : (estado == EstadoPedido.ENVIADO && p.getEntrega().tieneSeguimiento()
                        ? " Seguimiento: " + p.getEntrega().getNumeroSeguimiento() + "."
                        : "");
        servicios.notificar(p.getClienteId(), tipo,
                "Tu pedido " + p.getNumeroPedido() + " ahora esta " + estado.name().replace('_', ' '),
                "Tu pedido " + p.getNumeroPedido() + " cambio de " + anterior.name().replace('_', ' ') + " a "
                        + estado.name().replace('_', ' ') + "." + extra);
        return conCliente(PedidoRespuesta.desde(p));
    }

    @Transactional
    public PedidoRespuesta seguimiento(Long id, String codigo) {
        Pedido p = bloquear(id);
        p.registrarSeguimiento(codigo);
        repository.saveAndFlush(p);
        servicios.notificar(p.getClienteId(), "DESPACHO", "Seguimiento de tu pedido " + p.getNumeroPedido(),
                "Tu pedido fue despachado. Numero de seguimiento: " + codigo.trim());
        return conCliente(PedidoRespuesta.desde(p));
    }

    /**
     * Cliente: solo su pedido y antes del envio. Repone stock en catalogo (HU
     * cancelacion).
     */
    @Transactional
    public PedidoRespuesta cancelar(Long clienteId, Long id) {
        Pedido p = bloquear(id);
        if (!p.getClienteId().equals(clienteId))
            throw new NoSuchElementException("Pedido no encontrado");
        return PedidoRespuesta.desde(cancelarYReponer(p));
    }

    @Transactional
    public PedidoRespuesta cancelarAdmin(Long id) {
        return conCliente(PedidoRespuesta.desde(cancelarYReponer(bloquear(id))));
    }

    private Pedido cancelarYReponer(Pedido p) {
        if (!p.cancelar())
            throw new IllegalStateException("El pedido ya no puede cancelarse (estado " + p.getEstado() + ")");
        repository.saveAndFlush(p);
        // Si catalogo falla se lanza excepcion y la transaccion revierte la
        // cancelacion: nunca queda cancelado sin reponer.
        servicios.reponerStock("CANCELACION " + p.getNumeroPedido(),
                p.getDetalles().stream().map(d -> new ServiciosHttpClient.Linea(d.getLibroId(), d.getCantidad()))
                        .toList());
        servicios.notificar(p.getClienteId(), "CANCELACION", "Pedido " + p.getNumeroPedido() + " cancelado",
                "Tu pedido " + p.getNumeroPedido()
                        + " fue cancelado. Si ya pagaste, el reembolso se gestiona con el mismo medio de pago.");
        return p;
    }

    // ---------------------------------------------- administracion
    @Transactional(readOnly = true)
    public PedidoRespuesta consultarAdmin(Long id) {
        return conCliente(PedidoRespuesta
                .desde(repository.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"))));
    }

    @Transactional(readOnly = true)
    public PedidoRespuesta buscarNumeroAdmin(String numero) {
        return conCliente(PedidoRespuesta.desde(repository.findByNumeroPedido(numero.trim().toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("No se encontro el pedido " + numero))));
    }

    @Transactional(readOnly = true)
    public PaginaPedidosRespuesta listarAdmin(CriteriosPedido criterios, int n, int size) {
        if (!criterios.esRangoFechasValido())
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        PaginaPedidosRespuesta pagina = filtrar(null, criterios, n, size);
        var nombres = servicios.usuarios(pagina.pedidos().stream().map(PedidoRespuesta::clienteId).toList());
        return pagina.conPedidos(pagina.pedidos().stream().map(p -> {
            var u = nombres.get(p.clienteId());
            return u == null ? p : p.conCliente(u.nombreCompleto(), u.email());
        }).toList());
    }

    private PedidoRespuesta conCliente(PedidoRespuesta p) {
        var u = servicios.usuarios(List.of(p.clienteId())).get(p.clienteId());
        return u == null ? p : p.conCliente(u.nombreCompleto(), u.email());
    }

    // ---------------------------------------------- interno
    @Transactional(readOnly = true)
    public boolean libroTienePedidos(Long libroId) {
        return repository.existeLibro(libroId);
    }

    @Transactional(readOnly = true)
    public List<Long> librosComprados(Long clienteId) {
        return repository.librosComprados(clienteId, EstadoPedido.CANCELADO);
    }

    @Transactional(readOnly = true)
    public List<PedidoRespuesta> entre(java.time.LocalDate desde, java.time.LocalDate hasta) {
        var d = (desde == null ? java.time.LocalDate.of(2000, 1, 1) : desde).atStartOfDay();
        var h = (hasta == null ? java.time.LocalDate.now() : hasta).plusDays(1).atStartOfDay();
        return repository.findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThanOrderByFechaCreacionAsc(d, h)
                .stream().map(PedidoRespuesta::desde).toList();
    }

    private static String comprobante(Pedido p) {
        StringBuilder sb = new StringBuilder(
                "Comprobante " + p.getNumeroPedido() + " - " + p.getFechaCreacion().toLocalDate() + "\n");
        p.getDetalles().forEach(d -> sb.append(d.getCantidad()).append(" x ").append(d.getTituloLibro()).append(" = $")
                .append(d.calcularSubtotal().toPlainString()).append("\n"));
        var i = p.getImportes();
        sb.append("Subtotal $").append(i.getSubtotal().toPlainString()).append(" | Envio $")
                .append(i.getCostoEnvio().toPlainString())
                .append(" | Descuento $").append(i.getDescuento().toPlainString()).append(" | Total $")
                .append(i.calcularTotal().toPlainString())
                .append(" (IVA incluido $").append(i.calcularIvaIncluido().toPlainString()).append(")");
        return sb.toString();
    }
}
