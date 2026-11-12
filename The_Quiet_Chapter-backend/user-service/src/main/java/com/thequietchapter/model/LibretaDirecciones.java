package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "libretas_direcciones")
public class LibretaDirecciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "libreta_id", nullable = false)
    @OrderBy("id ASC")
    private List<Direccion> direcciones = new ArrayList<>();

    protected LibretaDirecciones() {
    }

    public static LibretaDirecciones crear() {
        return new LibretaDirecciones();
    }

    public boolean agregarDireccion(Direccion direccion) {
        if (direccion == null || !direccion.esValida())
            return false;
        if (direcciones.isEmpty())
            direccion.cambiarPredeterminada(true);
        else
            direccion.cambiarPredeterminada(false);
        direcciones.add(direccion);
        return true;
    }

    public List<Direccion> listarDirecciones() {
        return List.copyOf(direcciones);
    }

    public Optional<Direccion> buscarDireccion(Long id) {
        return direcciones.stream().filter(d -> Objects.equals(d.getId(), id)).findFirst();
    }

    public boolean eliminarDireccion(Long id) {
        Optional<Direccion> opc = buscarDireccion(id);
        if (opc.isEmpty())
            return false;
        boolean eraPredeterminada = opc.get().isPredeterminada();
        direcciones.remove(opc.get());
        if (eraPredeterminada && !direcciones.isEmpty())
            direcciones.get(0).cambiarPredeterminada(true);
        return true;
    }

    public boolean establecerPredeterminada(Long id) {
        Optional<Direccion> opc = buscarDireccion(id);
        if (opc.isEmpty())
            return false;
        direcciones.forEach(d -> d.cambiarPredeterminada(Objects.equals(d.getId(), id)));
        return true;
    }

    public Optional<Direccion> obtenerPredeterminada() {
        return direcciones.stream().filter(Direccion::isPredeterminada).findFirst();
    }

    public Long getId() {
        return id;
    }
}
