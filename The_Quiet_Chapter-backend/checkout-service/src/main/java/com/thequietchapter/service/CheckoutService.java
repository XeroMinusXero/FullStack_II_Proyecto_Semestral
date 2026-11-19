package com.thequietchapter.service;

import com.thequietchapter.dto.*;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.MedioPagoRepository;
import com.thequietchapter.repository.MetodoEnvioRepository;
import com.thequietchapter.repository.ProcesoCompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

/**
 * DC-03. Orquesta la compra completa con pasos compensables entre
 * microservicios
 */
@Service
public class CheckoutService {
    private final ProcesoCompraRepository procesos;
    private final MetodoEnvioRepository envios;
    private final MedioPagoRepository pagos;
    private final CarritoHttpClient carritoClient;
    private final OrquestacionHttpClient remoto;

    public CheckoutService(ProcesoCompraRepository procesos, MetodoEnvioRepository envios, MedioPagoRepository pagos,
            CarritoHttpClient carritoClient, OrquestacionHttpClient remoto) {
        this.procesos = procesos;
        this.envios = envios;
        this.pagos = pagos;
        this.carritoClient = carritoClient;
        this.remoto = remoto;
    }

    /**
     * p es una entidad ADMINISTRADA (cargada en esta transaccion): se usa flush() y
     * no saveAndFlush().
     * saveAndFlush hace merge() y Hibernate reemplaza los hijos nuevos (Pago,
     * DireccionEnvio, ResumenCompra)
     * por copias, dejando desconectadas las referencias que el codigo sigue usando.
     */
    private ProcesoCompra guardar(ProcesoCompra p) {
        procesos.flush();
        return p;
    }

    private ProcesoCompra propio(Long procesoId, Long clienteId) {
        return procesos.findByIdAndClienteId(procesoId, clienteId)
                .orElseThrow(() -> new NoSuchElementException("Checkout no encontrado para el cliente"));
    }

    /**
     * HU-022: solo clientes autenticados; reutiliza el proceso abierto del mismo
     * carrito.
     */
    @Transactional
    public CheckoutRespuesta iniciar(Long clienteId, Long carritoId) {
        carritoClient.obtenerCliente(clienteId, carritoId);
        var previo = procesos.findFirstByClienteIdAndCarritoIdAndEstadoInOrderByIdDesc(clienteId, carritoId,
                List.of(EstadoCheckout.EN_PREPARACION, EstadoCheckout.LISTO_PARA_PAGO));
        return CheckoutRespuesta.desde(previo.orElseGet(() -> procesos.save(new ProcesoCompra(clienteId, carritoId))));
    }

    @Transactional(readOnly = true)
    public CheckoutRespuesta consultar(Long clienteId, Long id) {
        return CheckoutRespuesta.desde(propio(id, clienteId));
    }

    @Transactional(readOnly = true)
    public List<CheckoutRespuesta> misProcesos(Long clienteId) {
        return procesos.findByClienteIdOrderByIdDesc(clienteId).stream().map(CheckoutRespuesta::desde).toList();
    }

    /**
     * HU-025: metodos aplicables a una region (o todos los activos si no se
     * indica).
     */
    @Transactional(readOnly = true)
    public List<MetodoEnvioRespuesta> metodosEnvio(String region) {
        return envios.findByActivoTrueOrderByNombreAsc().stream()
                .filter(m -> region == null || region.isBlank() || m.getRegionesHabilitadas().isEmpty()
                        || m.getRegionesHabilitadas().stream().anyMatch(r -> r.equalsIgnoreCase(region.strip())))
                .map(MetodoEnvioRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MedioPagoRespuesta> mediosPago() {
        return pagos.findByActivoTrueOrderByNombreAsc().stream().map(MedioPagoRespuesta::desde).toList();
    }

    @Transactional
    public CheckoutRespuesta elegirDireccion(Long clienteId, Long id, DireccionEnvioSolicitud s) {
        ProcesoCompra p = propio(id, clienteId);
        p.seleccionarDireccion(new DireccionEnvio(s.calle(), s.numero(), s.comuna(), s.region()));
        return CheckoutRespuesta.desde(guardar(p));
    }

    @Transactional
    public CheckoutRespuesta elegirEnvio(Long clienteId, Long id, Long envioId) {
        ProcesoCompra p = propio(id, clienteId);
        MetodoEnvio envio = envios.findById(envioId)
                .orElseThrow(() -> new NoSuchElementException("Metodo de envio inexistente"));
        p.seleccionarMetodoEnvio(envio);
        return CheckoutRespuesta.desde(guardar(p));
    }

    @Transactional
    public CheckoutRespuesta elegirPago(Long clienteId, Long id, Long medioId) {
        ProcesoCompra p = propio(id, clienteId);
        MedioPago medio = pagos.findById(medioId)
                .orElseThrow(() -> new NoSuchElementException("Medio de pago inexistente"));
        p.seleccionarMedioPago(medio);
        return CheckoutRespuesta.desde(guardar(p));
    }

    /**
     * HU-027: resumen con lineas, envio, descuento del cupon validado y total (IVA
     * incluido, informado aparte).
     */
    @Transactional
    public CheckoutRespuesta prepararResumen(Long clienteId, Long id) {
        ProcesoCompra p = propio(id, clienteId);
        var c = carritoClient.obtenerCliente(clienteId, p.getCarritoId());
        List<LineaCompra> lineas = c.items().stream()
                .map(i -> new LineaCompra(i.libroId(), i.titulo(), i.cantidad(), i.precioUnitario())).toList();
        p.prepararResumen(lineas, BigDecimal.ZERO, c.descuento());
        return CheckoutRespuesta.desde(guardar(p));
    }

    private static void validarSnapshot(ResumenCompra resumen, CarritoHttpClient.CarritoRemoto c) {
        if (resumen == null || c.items().size() != resumen.getLineas().size()
                || c.descuento().compareTo(resumen.getDescuento()) != 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tu carrito cambio: revisa nuevamente el resumen");
        for (LineaCompra l : resumen.getLineas()) {
            boolean coincide = c.items().stream().anyMatch(i -> i.libroId().equals(l.getLibroId())
                    && i.cantidad() == l.getCantidad() && i.precioUnitario().compareTo(l.getPrecioUnitario()) == 0);
            if (!coincide)
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Un precio o cantidad cambio: revisa nuevamente el resumen");
        }
    }

    /**
     * HU-028 / HU-029. Pago SIMULADO: 1) registra el intento, 2) descuenta stock
     * (catalog), 3) crea el pedido (order),
     * 4) aprueba el pago y finaliza, 5) vacia el carrito y registra el canje del
     * cupon.
     * Si 2 o 3 fallan: el pago queda RECHAZADO con el motivo, se compensa el stock
     * y el carrito queda intacto.
     */
    @Transactional(noRollbackFor = OrquestacionHttpClient.PasoFallido.class)
    public CheckoutRespuesta pagar(Long clienteId, Long id, boolean simularRechazo) {
        ProcesoCompra p = propio(id, clienteId);
        if (!p.puedePagar())
            throw new IllegalStateException("Completa direccion, envio, medio de pago y resumen antes de pagar");
        var c = carritoClient.obtenerCliente(clienteId, p.getCarritoId());
        validarSnapshot(p.getResumen(), c);
        if (!p.getMetodoEnvio().esAplicable(p.getDireccionEnvio())
                || p.getMetodoEnvio().calcularCosto(p.getDireccionEnvio())
                        .compareTo(p.getResumen().getCostoEnvio()) != 0
                || !p.getMedioPago().estaDisponible())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Las opciones de envio o pago cambiaron: revisa el resumen");

        Pago pago = new Pago(p.getResumen().calcularTotal());
        p.registrarIntentoPago(pago);
        guardar(p);
        if (simularRechazo) {
            pago.rechazar("Pago rechazado por el emisor (simulacion)");
            return rechazado(p, pago);
        }

        String referencia = "SIM-" + p.getId() + "-" + pago.getId();
        List<OrquestacionHttpClient.Linea> lineas = p.getResumen().getLineas().stream()
                .map(l -> new OrquestacionHttpClient.Linea(l.getLibroId(), l.getCantidad())).toList();
        try {
            remoto.descontarStock(referencia, lineas);
        } catch (OrquestacionHttpClient.PasoFallido e) {
            pago.rechazar(e.getMessage());
            return rechazado(p, pago);
        }
        OrquestacionHttpClient.PedidoCreado pedido;
        try {
            pedido = remoto.crearPedido(solicitudPedido(p, referencia));
        } catch (OrquestacionHttpClient.PasoFallido e) {
            remoto.reponerStock(referencia, lineas);
            pago.rechazar(e.getMessage());
            return rechazado(p, pago);
        }
        pago.aprobar(referencia);
        p.finalizar(pedido.numeroPedido());
        guardar(p);
        carritoClient.vaciar(clienteId, p.getCarritoId());
        remoto.registrarCanje(c.cuponAplicadoId(), clienteId, pedido.id(), p.getResumen().getDescuento());
        remoto.registrarAplicaciones(pedido.id(), lineas);
        remoto.notificarPago(clienteId, "Pago aprobado - pedido " + pedido.numeroPedido(),
                "Tu pago por $" + pago.getMonto().toPlainString() + " fue aprobado. Numero de pedido: "
                        + pedido.numeroPedido() + ".");
        return CheckoutRespuesta.desde(p);
    }

    private CheckoutRespuesta rechazado(ProcesoCompra p, Pago pago) {
        guardar(p);
        remoto.notificarPago(p.getClienteId(), "Pago rechazado",
                "Tu pago no pudo completarse: " + pago.getMotivoRechazo()
                        + ". Tu carrito se mantiene intacto: revisa los datos e intenta nuevamente.");
        return CheckoutRespuesta.desde(p);
    }

    private static Map<String, Object> solicitudPedido(ProcesoCompra p, String referencia) {
        var r = p.getResumen();
        var d = p.getDireccionEnvio();
        var m = p.getMetodoEnvio();
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("clienteId", p.getClienteId());
        s.put("detalles",
                r.getLineas().stream().map(l -> Map.of("libroId", l.getLibroId(), "tituloLibro", l.getTitulo(),
                        "cantidad", l.getCantidad(), "precioUnitario", l.getPrecioUnitario())).toList());
        s.put("entrega",
                Map.of("calle", d.getCalle(), "numero", d.getNumero(), "comuna", d.getComuna(), "region", d.getRegion(),
                        "metodoEnvio", m.getNombre(), "plazoEstimadoDias", m.getPlazoEstimadoDias()));
        s.put("importes", Map.of("subtotal", r.calcularSubtotal(), "costoEnvio", r.getCostoEnvio(),
                "impuestos", r.getImpuestos(), "descuento", r.getDescuento()));
        s.put("medioPago", p.getMedioPago().getNombre());
        s.put("referenciaPago", referencia);
        return s;
    }

    @Transactional
    public CheckoutRespuesta cancelar(Long clienteId, Long id) {
        ProcesoCompra p = propio(id, clienteId);
        p.cancelar();
        return CheckoutRespuesta.desde(guardar(p));
    }

    // ------------------------------------------------ administracion de opciones
    // (reemplaza el SQL manual)
    @Transactional(readOnly = true)
    public List<MetodoEnvioRespuesta> todosLosEnvios() {
        return envios.findAll().stream().map(MetodoEnvioRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MedioPagoRespuesta> todosLosMedios() {
        return pagos.findAll().stream().map(MedioPagoRespuesta::desde).toList();
    }

    @Transactional
    public MetodoEnvioRespuesta crearEnvio(MetodoEnvioSolicitud s) {
        return MetodoEnvioRespuesta
                .desde(envios.saveAndFlush(new MetodoEnvio(s.nombre(), s.costoBase(), s.plazoEstimadoDias(),
                        s.activo(), s.regionesHabilitadas())));
    }

    @Transactional
    public MetodoEnvioRespuesta editarEnvio(Long id, MetodoEnvioSolicitud s) {
        MetodoEnvio m = envios.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Metodo de envio inexistente"));
        m.actualizar(s.nombre(), s.costoBase(), s.plazoEstimadoDias(), s.activo(), s.regionesHabilitadas());
        return MetodoEnvioRespuesta.desde(envios.saveAndFlush(m));
    }

    @Transactional
    public MedioPagoRespuesta crearMedio(MedioPagoSolicitud s) {
        return MedioPagoRespuesta.desde(pagos.saveAndFlush(new MedioPago(s.nombre(), s.activo())));
    }

    @Transactional
    public MedioPagoRespuesta editarMedio(Long id, MedioPagoSolicitud s) {
        MedioPago m = pagos.findById(id).orElseThrow(() -> new NoSuchElementException("Medio de pago inexistente"));
        m.actualizar(s.nombre(), s.activo());
        return MedioPagoRespuesta.desde(pagos.saveAndFlush(m));
    }
}
