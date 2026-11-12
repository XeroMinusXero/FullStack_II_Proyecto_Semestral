package com.thequietchapter.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * DC-06: reglas sobre el conjunto de resenas y calificaciones de un libro (se
 * construye con los datos de BD).
 */
public class RegistroOpiniones {
    private final List<Resena> resenas = new ArrayList<>();
    private final List<Calificacion> calificaciones = new ArrayList<>();

    public RegistroOpiniones(List<Resena> resenas, List<Calificacion> calificaciones) {
        if (resenas != null)
            this.resenas.addAll(resenas);
        if (calificaciones != null)
            this.calificaciones.addAll(calificaciones);
    }

    /**
     * HU-080: requiere compra y no tener otra resena vigente sobre el mismo libro.
     */
    public boolean publicarResena(Resena resena, boolean compraVerificada) {
        if (!compraVerificada || resena == null)
            return false;
        boolean yaTiene = resenas.stream().anyMatch(r -> r.getLibroId().equals(resena.getLibroId())
                && r.getClienteId().equals(resena.getClienteId()) && r.estaVisible());
        if (yaTiene)
            return false;
        resenas.add(resena);
        return true;
    }

    public boolean registrarCalificacion(Calificacion calificacion, boolean compraVerificada) {
        if (!compraVerificada || calificacion == null || !calificacion.esValida())
            return false;
        calificaciones.removeIf(c -> c.getClienteId().equals(calificacion.getClienteId())
                && c.getLibroId().equals(calificacion.getLibroId()));
        calificaciones.add(calificacion);
        return true;
    }

    public Optional<Resena> buscarResena(Long id) {
        return resenas.stream().filter(r -> Objects.equals(r.getId(), id)).findFirst();
    }

    public List<Resena> listarResenas(Long libroId) {
        return resenas.stream().filter(r -> r.getLibroId().equals(libroId) && r.estaVisible()).toList();
    }

    public Optional<Integer> puntuacionDe(Long clienteId, Long libroId) {
        return calificaciones.stream().filter(c -> c.getClienteId().equals(clienteId) && c.getLibroId().equals(libroId))
                .map(Calificacion::getPuntuacion).findFirst();
    }

    /** HU-086. */
    public List<Resena> ordenarResenas(Long libroId, OrdenResenas orden) {
        Comparator<Resena> recientes = Comparator.comparing(Resena::getFechaPublicacion).reversed();
        Comparator<Resena> c = switch (orden == null ? OrdenResenas.MAS_RECIENTES : orden) {
            case MAS_RECIENTES -> recientes;
            case MAS_UTILES -> Comparator.comparingInt(Resena::contarVotosUtiles).reversed().thenComparing(recientes);
            case MEJOR_CALIFICADAS ->
                Comparator.<Resena>comparingInt(r -> puntuacionDe(r.getClienteId(), libroId).orElse(0)).reversed()
                        .thenComparing(recientes);
        };
        return listarResenas(libroId).stream().sorted(c).toList();
    }

    /** HU-087. */
    public List<Resena> filtrarPorCalificacion(Long libroId, int puntuacion) {
        return listarResenas(libroId).stream()
                .filter(r -> puntuacionDe(r.getClienteId(), libroId).orElse(0) == puntuacion).toList();
    }

    /** HU-085: null si no hay calificaciones. */
    public BigDecimal calcularPromedio(Long libroId) {
        var notas = calificaciones.stream().filter(c -> c.getLibroId().equals(libroId))
                .mapToInt(Calificacion::getPuntuacion).toArray();
        if (notas.length == 0)
            return null;
        return BigDecimal.valueOf(Arrays.stream(notas).sum()).divide(BigDecimal.valueOf(notas.length), 1,
                RoundingMode.HALF_UP);
    }

    public int contarCalificaciones(Long libroId) {
        return (int) calificaciones.stream().filter(c -> c.getLibroId().equals(libroId)).count();
    }

    public List<Resena> listarReportadas() {
        return resenas.stream().filter(r -> r.getEstado() == EstadoResena.REPORTADA).toList();
    }
}
