package com.thequietchapter.model;

import java.util.*;

/**
 * DC-07: reglas sobre el conjunto de cupones (codigo unico, eliminacion solo
 * sin canjes).
 */
public class RegistroCupones {
    private final List<Cupon> cupones = new ArrayList<>();

    public RegistroCupones(List<Cupon> cupones) {
        if (cupones != null)
            this.cupones.addAll(cupones);
    }

    public boolean codigoDisponible(String codigo, Long exceptoId) {
        String c = Cupon.normalizar(codigo);
        return cupones.stream().noneMatch(x -> x.getCodigo().equals(c) && !Objects.equals(x.getId(), exceptoId));
    }

    public boolean puedeEliminar(Cupon cupon) {
        return cupon != null && !cupon.tieneCanjes();
    }
}
