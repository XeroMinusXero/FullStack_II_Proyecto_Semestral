package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** DC-07 Cupon de descuento (HU-094..102). */
@Entity
@Table(name = "cupones", uniqueConstraints = @UniqueConstraint(name = "uk_cupon_codigo", columnNames = "codigo"))
public class Cupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40)
    private String codigo;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 20)
    private TipoDescuento tipoDescuento;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal valor;
    @Column(name = "monto_minimo", nullable = false, precision = 13, scale = 2)
    private BigDecimal montoMinimo;
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;
    @Column(nullable = false)
    private boolean activo;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cupon_id", nullable = false)
    @OrderBy("fechaCanje DESC")
    private List<CanjeCupon> canjes = new ArrayList<>();

    protected Cupon() {
    }

    public Cupon(String codigo, TipoDescuento tipo, BigDecimal valor, BigDecimal montoMinimo, LocalDateTime inicio,
            LocalDateTime fin) {
        actualizarCodigo(codigo);
        if (!editarCondiciones(tipo, valor, montoMinimo, inicio, fin))
            throw new IllegalArgumentException("Condiciones invalidas");
        this.activo = true;
    }

    public static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.strip().toUpperCase(Locale.ROOT);
    }

    void actualizarCodigo(String nuevo) {
        String c = normalizar(nuevo);
        if (!c.matches("[A-Z0-9_-]{3,40}"))
            throw new IllegalArgumentException("El codigo debe tener 3 a 40 caracteres (letras, numeros, - o _)");
        this.codigo = c;
    }

    public void cambiarCodigo(String nuevo) {
        actualizarCodigo(nuevo);
    }

    public boolean editarCondiciones(TipoDescuento tipo, BigDecimal valor, BigDecimal montoMinimo, LocalDateTime inicio,
            LocalDateTime fin) {
        Descuentos.validarValor(tipo, valor);
        Descuentos.validarVigencia(inicio, fin);
        if (montoMinimo == null || montoMinimo.signum() < 0)
            throw new IllegalArgumentException("El monto minimo no puede ser negativo");
        this.tipoDescuento = tipo;
        this.valor = valor;
        this.montoMinimo = montoMinimo;
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return true;
    }

    public boolean cambiarEstado(boolean activo) {
        boolean cambio = this.activo != activo;
        this.activo = activo;
        return cambio;
    }

    /**
     * Motivo por el que NO es canjeable (HU-100: informar el motivo), vacio si es
     * canjeable.
     */
    public Optional<String> motivoRechazo(BigDecimal subtotal, LocalDateTime fecha) {
        if (!activo)
            return Optional.of("El cupon no esta activo");
        if (fecha.isBefore(fechaInicio))
            return Optional.of("El cupon aun no esta vigente");
        if (fecha.isAfter(fechaFin))
            return Optional.of("El cupon esta vencido");
        if (subtotal == null || subtotal.compareTo(montoMinimo) < 0)
            return Optional.of("El monto minimo de compra para este cupon es $"
                    + montoMinimo.stripTrailingZeros().toPlainString());
        return Optional.empty();
    }

    public boolean esCanjeable(BigDecimal subtotal, LocalDateTime fecha) {
        return motivoRechazo(subtotal, fecha).isEmpty();
    }

    public BigDecimal calcularDescuento(BigDecimal subtotal) {
        return Descuentos.calcular(tipoDescuento, valor, subtotal);
    }

    /** Idempotente por pedido. */
    public CanjeCupon registrarCanje(Long clienteId, Long pedidoId, BigDecimal descuento) {
        return canjes.stream().filter(c -> c.correspondeAPedido(pedidoId)).findFirst().orElseGet(() -> {
            CanjeCupon c = new CanjeCupon(clienteId, pedidoId, descuento);
            canjes.add(c);
            return c;
        });
    }

    public List<CanjeCupon> listarCanjes() {
        return List.copyOf(canjes);
    }

    public boolean tieneCanjes() {
        return !canjes.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoDescuento getTipoDescuento() {
        return tipoDescuento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getMontoMinimo() {
        return montoMinimo;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }
}
