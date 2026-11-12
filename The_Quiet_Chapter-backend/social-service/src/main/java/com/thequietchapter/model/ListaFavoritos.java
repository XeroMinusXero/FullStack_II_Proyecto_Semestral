package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.*;

/** DC-06 ListaFavoritos: una por cliente. */
@Entity
@Table(name = "listas_favoritos", uniqueConstraints = @UniqueConstraint(name = "uk_lista_cliente", columnNames = "cliente_id"))
public class ListaFavoritos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    @OrderBy("fechaAgregado DESC")
    private List<Favorito> favoritos = new ArrayList<>();

    protected ListaFavoritos() {
    }

    public ListaFavoritos(Long clienteId) {
        if (clienteId == null || clienteId <= 0)
            throw new IllegalArgumentException("Cliente invalido");
        this.clienteId = clienteId;
    }

    /** HU-075: false si ya estaba (no se duplica). */
    public boolean agregarLibro(Long libroId) {
        if (contieneLibro(libroId))
            return false;
        favoritos.add(0, new Favorito(libroId));
        return true;
    }

    /** HU-076: false si no estaba. */
    public boolean quitarLibro(Long libroId) {
        return favoritos.removeIf(f -> f.correspondeALibro(libroId));
    }

    public boolean contieneLibro(Long libroId) {
        return favoritos.stream().anyMatch(f -> f.correspondeALibro(libroId));
    }

    public Set<Long> listarLibros() {
        Set<Long> ids = new LinkedHashSet<>();
        favoritos.forEach(f -> ids.add(f.getLibroId()));
        return ids;
    }

    public boolean estaVacia() {
        return favoritos.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public List<Favorito> getFavoritos() {
        return List.copyOf(favoritos);
    }
}
