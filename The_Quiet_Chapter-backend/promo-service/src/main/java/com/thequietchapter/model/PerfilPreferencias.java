package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DC-07: actividad del cliente para recomendaciones personalizadas (HU-114,
 * HU-115).
 */
@Entity
@Table(name = "perfiles_preferencias", uniqueConstraints = @UniqueConstraint(name = "uk_perfil_cliente", columnNames = "cliente_id"))
public class PerfilPreferencias {
    private static final int MAX = 50;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_libros_visitados", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "libro_id", nullable = false)
    private Set<Long> librosVisitados = new LinkedHashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_categorias", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "categoria_id", nullable = false)
    private Set<Long> categoriasPreferidas = new LinkedHashSet<>();

    protected PerfilPreferencias() {
    }

    public PerfilPreferencias(Long clienteId) {
        this.clienteId = clienteId;
    }

    public void registrarVisita(Long libroId) {
        if (librosVisitados.size() < MAX || librosVisitados.contains(libroId))
            librosVisitados.add(libroId);
    }

    public void registrarInteresCategoria(Long categoriaId) {
        if (categoriaId != null && categoriasPreferidas.size() < MAX)
            categoriasPreferidas.add(categoriaId);
    }

    public boolean tieneActividad() {
        return !librosVisitados.isEmpty() || !categoriasPreferidas.isEmpty();
    }

    public Set<Long> obtenerLibrosVisitados() {
        return Set.copyOf(librosVisitados);
    }

    public Set<Long> obtenerCategoriasPreferidas() {
        return Set.copyOf(categoriasPreferidas);
    }

    public Long getClienteId() {
        return clienteId;
    }
}
