package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkout_procesos", indexes = { @Index(name = "ix_checkout_cliente", columnList = "cliente_id") })
public class ProcesoCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "carrito_id", nullable = false)
    private Long carritoId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCheckout estado = EstadoCheckout.EN_PREPARACION;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", unique = true)
    private DireccionEnvio direccionEnvio;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_envio_id")
    private MetodoEnvio metodoEnvio;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medio_pago_id")
    private MedioPago medioPago;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resumen_id", unique = true)
    private ResumenCompra resumen;
    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pago> intentosPago = new ArrayList<>();
    @Column(length = 100, unique = true)
    private String numeroPedidoConfirmado;

    protected ProcesoCompra() {
    }

    public ProcesoCompra(Long clienteId, Long carritoId) {
        if (clienteId == null || clienteId <= 0 || carritoId == null || carritoId <= 0)
            throw new IllegalArgumentException("Checkout exige cliente autenticado y carrito válido");
        this.clienteId = clienteId;
        this.carritoId = carritoId;
        this.estado = EstadoCheckout.EN_PREPARACION;
    }

    private void editable() {
        if (estado == EstadoCheckout.CANCELADO || estado == EstadoCheckout.FINALIZADO)
            throw new IllegalStateException("El proceso de compra está cerrado");
        if (intentosPago.stream().anyMatch(p -> p.getEstado() == EstadoPago.PENDIENTE || p.estaAprobado()))
            throw new IllegalStateException("No se puede modificar un checkout con pago en curso o aprobado");
    }

    private void invalidarResumen() {
        resumen = null;
        estado = EstadoCheckout.EN_PREPARACION;
    }

    public void seleccionarDireccion(DireccionEnvio direccion) {
        editable();
        if (direccion == null || !direccion.esValida())
            throw new IllegalArgumentException("Dirección inválida");
        direccionEnvio = direccion;
        metodoEnvio = null;
        invalidarResumen();
    }

    public void seleccionarMetodoEnvio(MetodoEnvio metodo) {
        editable();
        if (direccionEnvio == null || metodo == null || !metodo.esAplicable(direccionEnvio))
            throw new IllegalArgumentException("Método de envío incompatible con dirección");
        metodoEnvio = metodo;
        invalidarResumen();
    }

    public void seleccionarMedioPago(MedioPago medio) {
        editable();
        if (medio == null || !medio.estaDisponible())
            throw new IllegalArgumentException("Medio de pago no disponible");
        medioPago = medio;
        invalidarResumen();
    }

    public ResumenCompra prepararResumen(List<LineaCompra> lineas, BigDecimal impuestos, BigDecimal descuento) {
        editable();
        if (direccionEnvio == null || metodoEnvio == null || !metodoEnvio.esAplicable(direccionEnvio)
                || medioPago == null || !medioPago.estaDisponible())
            throw new IllegalStateException("Seleccione dirección, envío y medio de pago antes de preparar el resumen");
        this.resumen = new ResumenCompra(lineas, metodoEnvio.calcularCosto(direccionEnvio), impuestos, descuento);
        estado = EstadoCheckout.LISTO_PARA_PAGO;
        return resumen;
    }

    public boolean puedePagar() {
        return estado == EstadoCheckout.LISTO_PARA_PAGO && resumen != null && resumen.esValido() &&
                intentosPago.stream().noneMatch(p -> p.getEstado() == EstadoPago.PENDIENTE || p.estaAprobado());
    }

    public void registrarIntentoPago(Pago pago) {
        if (!puedePagar() || pago == null || pago.getMonto().compareTo(resumen.calcularTotal()) != 0)
            throw new IllegalStateException("Resumen no listo, monto incorrecto o pago pendiente");
        pago.vincularProceso(this);
        intentosPago.add(pago);
    }

    /**
     * Requiere aprobación comprobada y pedido generado por Order Service. Sin
     * endpoint público hasta integrar ambos.
     */
    public void finalizar(String numeroPedido) {
        if (estado != EstadoCheckout.LISTO_PARA_PAGO || numeroPedido == null || numeroPedido.isBlank() ||
                intentosPago.stream().noneMatch(Pago::estaAprobado))
            throw new IllegalStateException("No se puede finalizar sin pago aprobado y pedido confirmado");
        numeroPedidoConfirmado = numeroPedido;
        estado = EstadoCheckout.FINALIZADO;
    }

    public void cancelar() {
        if (estado == EstadoCheckout.FINALIZADO || estado == EstadoCheckout.CANCELADO)
            throw new IllegalStateException("No se puede cancelar");
        if (intentosPago.stream().anyMatch(Pago::estaAprobado))
            throw new IllegalStateException("Pago aprobado: requiere devolución coordinada");
        intentosPago.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).forEach(Pago::cancelar);
        estado = EstadoCheckout.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getCarritoId() {
        return carritoId;
    }

    public EstadoCheckout getEstado() {
        return estado;
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public MetodoEnvio getMetodoEnvio() {
        return metodoEnvio;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public ResumenCompra getResumen() {
        return resumen;
    }

    public List<Pago> getIntentosPago() {
        return List.copyOf(intentosPago);
    }

    public String getNumeroPedidoConfirmado() {
        return numeroPedidoConfirmado;
    }
}
