package com.thequietchapter.model;

import java.util.*;

/**
 * DC-08: una solicitud activa por cliente y libro; solo para libros agotados.
 */
public class RegistroAvisosStock {
    private final List<SolicitudReabastecimiento> solicitudes = new ArrayList<>();

    public RegistroAvisosStock(List<SolicitudReabastecimiento> s) {
        if (s != null)
            solicitudes.addAll(s);
    }

    public boolean tieneSolicitudActiva(Long clienteId, Long libroId) {
        return solicitudes.stream()
                .anyMatch(s -> s.estaActiva() && s.getClienteId().equals(clienteId) && s.getLibroId().equals(libroId));
    }

    public Optional<SolicitudReabastecimiento> solicitarAviso(Long clienteId, Long libroId, int stockDisponible) {
        if (stockDisponible > 0)
            throw new IllegalStateException("El libro esta disponible: puedes comprarlo ahora");
        if (tieneSolicitudActiva(clienteId, libroId))
            return Optional.empty();
        var s = new SolicitudReabastecimiento(clienteId, libroId);
        solicitudes.add(s);
        return Optional.of(s);
    }

    public List<SolicitudReabastecimiento> listarPorLibro(Long libroId) {
        return solicitudes.stream().filter(s -> s.estaActiva() && s.getLibroId().equals(libroId)).toList();
    }
}
