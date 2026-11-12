package com.thequietchapter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/** DC-06 Resena con votos de utilidad y reportes de moderacion. */
@Entity
@Table(name = "resenas", indexes = { @Index(name = "ix_resena_libro", columnList = "libro_id"),
        @Index(name = "ix_resena_cliente", columnList = "cliente_id") })
public class Resena {
    public static final int MIN = 10, MAX = 2000;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "libro_id", nullable = false)
    private Long libroId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoResena estado;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resena_votos_utiles", joinColumns = @JoinColumn(name = "resena_id"))
    @Column(name = "cliente_id", nullable = false)
    private Set<Long> votantesUtiles = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "resena_id", nullable = false)
    @OrderBy("fechaReporte ASC")
    private List<ReporteResena> reportes = new ArrayList<>();

    protected Resena() {
    }

    public Resena(Long clienteId, Long libroId, String contenido) {
        if (clienteId == null || libroId == null)
            throw new IllegalArgumentException("Cliente y libro obligatorios");
        this.clienteId = clienteId;
        this.libroId = libroId;
        this.contenido = validar(contenido);
        this.fechaPublicacion = LocalDateTime.now();
        this.estado = EstadoResena.PUBLICADA;
    }

    private static String validar(String texto) {
        String t = texto == null ? "" : texto.strip();
        if (t.length() < MIN || t.length() > MAX)
            throw new IllegalArgumentException("La resena debe tener entre " + MIN + " y " + MAX + " caracteres");
        return t;
    }

    /** HU-081: solo el autor. */
    public boolean editarTexto(Long clienteId, String nuevoTexto) {
        if (!this.clienteId.equals(clienteId) || !estaVisible())
            return false;
        this.contenido = validar(nuevoTexto);
        this.fechaActualizacion = LocalDateTime.now();
        return true;
    }

    /** HU-082: solo el autor. */
    public boolean eliminarPorAutor(Long clienteId) {
        if (!this.clienteId.equals(clienteId) || !estaVisible())
            return false;
        this.estado = EstadoResena.ELIMINADA_POR_AUTOR;
        this.fechaActualizacion = LocalDateTime.now();
        return true;
    }

    /**
     * HU-088: el autor no puede marcar su propia resena; no se cuenta dos veces.
     */
    public boolean marcarUtil(Long clienteId) {
        if (this.clienteId.equals(clienteId) || !estaVisible())
            return false;
        return votantesUtiles.add(clienteId);
    }

    public boolean retirarMarcaUtil(Long clienteId) {
        return votantesUtiles.remove(clienteId);
    }

    public int contarVotosUtiles() {
        return votantesUtiles.size();
    }

    public boolean marcadaUtilPor(Long clienteId) {
        return clienteId != null && votantesUtiles.contains(clienteId);
    }

    /** HU-089: un cliente reporta una sola vez; queda marcada como REPORTADA. */
    public boolean reportar(Long clienteId, String motivo) {
        if (!estaVisible() || this.clienteId.equals(clienteId)
                || reportes.stream().anyMatch(r -> r.getClienteReportanteId().equals(clienteId)))
            return false;
        reportes.add(new ReporteResena(clienteId, motivo));
        this.estado = EstadoResena.REPORTADA;
        return true;
    }

    /**
     * HU-091/092: resuelve TODOS los reportes pendientes. APROBADO = sigue visible;
     * ELIMINADO = retirada por moderacion.
     */
    public boolean resolverReporte(Long administradorId, EstadoReporte resultado) {
        if (estado != EstadoResena.REPORTADA || resultado == null || resultado == EstadoReporte.PENDIENTE)
            return false;
        reportes.stream().filter(ReporteResena::estaPendiente).forEach(r -> r.resolver(resultado, administradorId));
        this.estado = resultado == EstadoReporte.APROBADO ? EstadoResena.PUBLICADA : EstadoResena.RETIRADA;
        return true;
    }

    public boolean estaVisible() {
        return estado == EstadoResena.PUBLICADA || estado == EstadoResena.REPORTADA;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getLibroId() {
        return libroId;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public EstadoResena getEstado() {
        return estado;
    }

    public List<ReporteResena> getReportes() {
        return List.copyOf(reportes);
    }
}
