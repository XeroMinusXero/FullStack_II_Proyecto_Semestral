package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkout_metodos_envio")
public class MetodoEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal costoBase;
    @Column(nullable = false)
    private int plazoEstimadoDias;
    @Column(nullable = false)
    private boolean activo;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checkout_metodo_regiones", joinColumns = @JoinColumn(name = "metodo_id"))
    @Column(name = "region", nullable = false, length = 100)
    private List<String> regionesHabilitadas = new ArrayList<>();

    protected MetodoEnvio() {
    }

    public MetodoEnvio(String nombre, BigDecimal costoBase, int plazoEstimadoDias, boolean activo,
            List<String> regionesHabilitadas) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 120 || costoBase == null || costoBase.signum() < 0
                || plazoEstimadoDias < 0)
            throw new IllegalArgumentException("Datos inválidos del método de envío");
        this.nombre = nombre.trim();
        this.costoBase = costoBase;
        this.plazoEstimadoDias = plazoEstimadoDias;
        this.activo = activo;
        this.regionesHabilitadas = new ArrayList<>(regionesHabilitadas == null ? List.of() : regionesHabilitadas);
    }

    /** Mantenedor de administracion (reemplaza la carga manual por SQL). */
    public void actualizar(String nombre, BigDecimal costoBase, int plazoEstimadoDias, boolean activo,
            List<String> regionesHabilitadas) {
        MetodoEnvio v = new MetodoEnvio(nombre, costoBase, plazoEstimadoDias, activo, regionesHabilitadas);
        this.nombre = v.nombre;
        this.costoBase = v.costoBase;
        this.plazoEstimadoDias = v.plazoEstimadoDias;
        this.activo = v.activo;
        this.regionesHabilitadas.clear();
        this.regionesHabilitadas.addAll(v.regionesHabilitadas);
    }

    /** Si no hay regiones configuradas, está disponible en cualquier región. */
    public boolean esAplicable(DireccionEnvio direccion) {
        return activo && direccion != null && direccion.esValida() &&
                (regionesHabilitadas.isEmpty()
                        || regionesHabilitadas.stream().anyMatch(r -> r.equalsIgnoreCase(direccion.getRegion())));
    }

    public BigDecimal calcularCosto(DireccionEnvio direccion) {
        if (!esAplicable(direccion))
            throw new IllegalStateException("Método de envío no aplicable a esa dirección");
        return costoBase;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public int getPlazoEstimadoDias() {
        return plazoEstimadoDias;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<String> getRegionesHabilitadas() {
        return List.copyOf(regionesHabilitadas);
    }
}
