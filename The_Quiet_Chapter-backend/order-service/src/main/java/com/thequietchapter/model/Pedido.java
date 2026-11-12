package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_pedidos_cliente_fecha", columnList = "cliente_id,fecha_creacion"),
        @Index(name = "idx_pedidos_estado", columnList = "estado") })
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // El número definitivo se asigna al obtener el ID autogenerado (en la misma
    // transacción).
    @Column(name = "numero_pedido", unique = true, length = 40)
    private String numeroPedido;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();
    @Embedded
    private DatosEntregaPedido entrega;
    @Embedded
    private ImportesPedido importes;
    @Column(name = "medio_pago", nullable = false, length = 120)
    private String medioPago;
    @Column(name = "referencia_pago", unique = true, nullable = false, length = 150)
    private String referenciaPago;
    @Version
    private long version;

    protected Pedido() {
    }

    public Pedido(String numeroPedido, Long clienteId, List<DetallePedido> detalles, DatosEntregaPedido entrega,
            ImportesPedido importes, String medioPago, String referenciaPago) {
        if (clienteId == null || clienteId <= 0)
            throw new IllegalArgumentException("Cliente inválido");
        if (detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("Pedido sin líneas");
        if (!Objects.requireNonNull(importes, "Importes obligatorios").esConsistente(detalles))
            throw new IllegalArgumentException("El subtotal no coincide con las líneas");
        this.clienteId = clienteId;
        this.entrega = Objects.requireNonNull(entrega, "Entrega obligatoria");
        this.importes = importes;
        this.medioPago = obligatorio(medioPago, "Medio de pago", 120);
        this.referenciaPago = obligatorio(referenciaPago, "Referencia de pago", 150);
        this.numeroPedido = numeroPedido;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPedido.PAGADO;
        for (DetallePedido d : detalles) {
            this.detalles.add(Objects.requireNonNull(d));
            d.vincularPedido(this);
        }
    }

    private static String obligatorio(String s, String campo, int max) {
        if (s == null || s.isBlank() || s.trim().length() > max)
            throw new IllegalArgumentException(campo + " inválido");
        return s.trim();
    }

    /**
     * Numeración segura: usa ID persistente y no un contador local con riesgo de
     * duplicados.
     */
    public void completarNumeroPersistente() {
        if (id == null)
            throw new IllegalStateException("Persistir antes de numerar el pedido");
        if (numeroPedido == null)
            numeroPedido = "TQC-" + String.format("%08d", id);
    }

    public boolean avanzarEstado(EstadoPedido nuevoEstado) {
        if (nuevoEstado == null)
            return false;
        boolean valido = switch (estado) {
            case PAGADO -> nuevoEstado == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> nuevoEstado == EstadoPedido.ENVIADO;
            case ENVIADO -> nuevoEstado == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
        if (valido)
            this.estado = nuevoEstado;
        return valido;
    }

    public void registrarSeguimiento(String numero) {
        if (estado != EstadoPedido.ENVIADO)
            throw new IllegalStateException("Registrar seguimiento requiere pedido ENVIADO");
        entrega.registrarSeguimiento(numero);
    }

    public boolean puedeCancelarse() {
        return estado == EstadoPedido.PAGADO || estado == EstadoPedido.EN_PREPARACION;
    }

    public boolean cancelar() {
        if (!puedeCancelarse())
            return false;
        estado = EstadoPedido.CANCELADO;
        return true;
    }

    public boolean contieneLibro(Long libroId) {
        return libroId != null && detalles.stream().anyMatch(d -> libroId.equals(d.getLibroId()));
    }

    public Long getId() {
        return id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<DetallePedido> getDetalles() {
        return List.copyOf(detalles);
    }

    public DatosEntregaPedido getEntrega() {
        return entrega;
    }

    public ImportesPedido getImportes() {
        return importes;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }
}
