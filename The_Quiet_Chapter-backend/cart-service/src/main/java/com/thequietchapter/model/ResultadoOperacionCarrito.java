package com.thequietchapter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * El resultado informa cambios, advertencias y bloqueos sin excepciones de
 * negocio innecesarias.
 */
public class ResultadoOperacionCarrito {
    private boolean exitosa = true;
    private final List<AvisoCarrito> avisos = new ArrayList<>();

    public void agregarAviso(AvisoCarrito aviso) {
        if (aviso == null)
            throw new IllegalArgumentException("Aviso requerido");
        avisos.add(aviso);
        if (aviso.esBloqueante())
            exitosa = false;
    }

    public boolean tieneAvisos() {
        return !avisos.isEmpty();
    }

    public boolean hayBloqueos() {
        return avisos.stream().anyMatch(AvisoCarrito::esBloqueante);
    }

    public boolean isExitosa() {
        return exitosa;
    }

    public List<AvisoCarrito> getAvisos() {
        return List.copyOf(avisos);
    }
}
