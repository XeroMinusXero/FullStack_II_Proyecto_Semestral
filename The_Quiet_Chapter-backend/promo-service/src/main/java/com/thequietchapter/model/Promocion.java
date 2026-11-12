package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DC-07 Promocion por porcentaje o monto fijo con alcance y vigencia
 * (HU-103..113).
 */
@Entity
@Table(name = "promociones")
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 20)
    private TipoDescuento tipoDescuento;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal valor;
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;
    @Column(nullable = false)
    private boolean activo;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "promocion_id", nullable = false)
    private List<AlcancePromocion> alcances = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id", nullable = false)
    private List<AplicacionPromocion> aplicaciones = new ArrayList<>();

    protected Promocion() {
    }

    public Promocion(String nombre, TipoDescuento tipo, BigDecimal valor, LocalDateTime inicio, LocalDateTime fin) {
        renombrar(nombre);
        editarDescuento(tipo, valor);
        definirVigencia(inicio, fin);
        this.activo = false;
    }

    public void renombrar(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.strip().length() > 120)
            throw new IllegalArgumentException("Nombre de promocion obligatorio (maximo 120)");
        this.nombre = nombre.strip();
    }

    public boolean editarDescuento(TipoDescuento tipo, BigDecimal valor) {
        Descuentos.validarValor(tipo, valor);
        this.tipoDescuento = tipo;
        this.valor = valor;
        return true;
    }

    public boolean definirVigencia(LocalDateTime inicio, LocalDateTime fin) {
        Descuentos.validarVigencia(inicio, fin);
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return true;
    }

    public boolean reemplazarAlcances(List<AlcancePromocion> nuevos) {
        if (nuevos == null || nuevos.isEmpty())
            throw new IllegalArgumentException("Define al menos un alcance (libro, categoria, genero o editorial)");
        alcances.clear();
        alcances.addAll(nuevos);
        return true;
    }

    /** HU-110: sin alcance no se puede activar. */
    public boolean cambiarEstado(boolean activo) {
        if (activo && alcances.isEmpty())
            throw new IllegalStateException("No se puede activar una promocion sin alcance definido");
        boolean cambio = this.activo != activo;
        this.activo = activo;
        return cambio;
    }

    public boolean estaVigente(LocalDateTime fecha) {
        return activo && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public boolean aplicaA(Long libroId, Long categoriaId, List<Long> generoIds, Long editorialId,
            LocalDateTime fecha) {
        return estaVigente(fecha)
                && alcances.stream().anyMatch(a -> a.coincide(libroId, categoriaId, generoIds, editorialId));
    }

    public BigDecimal calcularDescuento(BigDecimal precioBase) {
        return Descuentos.calcular(tipoDescuento, valor, precioBase);
    }

    public AplicacionPromocion registrarAplicacion(Long pedidoId, Long libroId, BigDecimal descuento) {
        return aplicaciones.stream().filter(a -> a.correspondeAPedido(pedidoId) && a.getLibroId().equals(libroId))
                .findFirst()
                .orElseGet(() -> {
                    var a = new AplicacionPromocion(pedidoId, libroId, descuento);
                    aplicaciones.add(a);
                    return a;
                });
    }

    public boolean tieneAplicaciones() {
        return !aplicaciones.isEmpty();
    }

    public int contarAplicaciones() {
        return aplicaciones.size();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDescuento getTipoDescuento() {
        return tipoDescuento;
    }

    public BigDecimal getValor() {
        return valor;
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

    public List<AlcancePromocion> getAlcances() {
        return List.copyOf(alcances);
    }
}
