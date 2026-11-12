package com.thequietchapter.model;

import java.util.*;
import java.util.function.Predicate;

/**
 * DC-07 RecomendadorLibros: reglas deterministas (relacion por
 * autor/genero/categoria + popularidad).
 */
public class RecomendadorLibros {
    private final Map<Long, PerfilPreferencias> perfiles = new HashMap<>();

    public void agregarPerfil(PerfilPreferencias p) {
        if (p != null)
            perfiles.put(p.getClienteId(), p);
    }

    public void registrarVisita(Long clienteId, Long libroId) {
        perfiles.computeIfAbsent(clienteId, PerfilPreferencias::new).registrarVisita(libroId);
    }

    private static Comparator<LibroRecomendable> porPopularidad() {
        return Comparator.comparing(LibroRecomendable::destacado).reversed()
                .thenComparing(Comparator.comparingInt(LibroRecomendable::popularidad).reversed())
                .thenComparing(LibroRecomendable::libroId, Comparator.reverseOrder());
    }

    private static List<Long> top(List<LibroRecomendable> c, Predicate<LibroRecomendable> filtro, int limite) {
        return c.stream().filter(LibroRecomendable::estaDisponible).filter(filtro).sorted(porPopularidad())
                .limit(Math.max(1, limite)).map(LibroRecomendable::libroId).toList();
    }

    /** HU-114 visitante: mas vendidos / destacados. */
    public List<Long> recomendarGenerales(List<LibroRecomendable> candidatos, int limite) {
        return top(candidatos, x -> true, limite);
    }

    /** HU-116. */
    public List<Long> recomendarPorCategoria(Long categoriaId, List<LibroRecomendable> candidatos, int limite) {
        return top(candidatos, x -> Objects.equals(x.categoriaId(), categoriaId), limite);
    }

    /**
     * Libros con mayor puntaje respecto de un conjunto base, excluyendo los de la
     * base y los excluidos.
     */
    private static List<Long> relacionadosA(Set<Long> base, Set<Long> excluir, List<LibroRecomendable> c, int limite) {
        Map<Long, LibroRecomendable> porId = new HashMap<>();
        c.forEach(x -> porId.put(x.libroId(), x));
        List<LibroRecomendable> origen = base.stream().map(porId::get).filter(Objects::nonNull).toList();
        Map<Long, Integer> puntaje = new HashMap<>();
        for (LibroRecomendable cand : c) {
            if (!cand.estaDisponible() || base.contains(cand.libroId()) || excluir.contains(cand.libroId()))
                continue;
            int p = origen.stream().mapToInt(o -> o.puntaje(cand)).sum();
            if (p > 0)
                puntaje.put(cand.libroId(), p);
        }
        return puntaje.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()
                        .thenComparing(Comparator
                                .comparingInt((Map.Entry<Long, Integer> e) -> porId.get(e.getKey()).popularidad())
                                .reversed()))
                .limit(Math.max(1, limite)).map(Map.Entry::getKey).toList();
    }

    /** HU-118: vacio si no hay relacion (el frontend no muestra el bloque). */
    public List<Long> recomendarRelacionados(Long libroId, List<LibroRecomendable> c, int limite) {
        return relacionadosA(Set.of(libroId), Set.of(), c, limite);
    }

    /** HU-117: sin compras -> generales. */
    public List<Long> recomendarSegunCompras(Set<Long> comprados, List<LibroRecomendable> c, int limite) {
        if (comprados == null || comprados.isEmpty())
            return recomendarGenerales(c, limite);
        List<Long> r = relacionadosA(comprados, Set.of(), c, limite);
        return r.isEmpty() ? top(c, x -> !comprados.contains(x.libroId()), limite) : r;
    }

    /** HU-119: vacio con carrito vacio. */
    public List<Long> recomendarComplementarios(Set<Long> carrito, List<LibroRecomendable> c, int limite) {
        if (carrito == null || carrito.isEmpty())
            return List.of();
        return relacionadosA(carrito, Set.of(), c, limite);
    }

    /**
     * HU-115: compras + visitas + categorias preferidas; sin historial ->
     * generales.
     */
    public List<Long> recomendarPerfil(Long clienteId, Set<Long> comprados, List<LibroRecomendable> c, int limite) {
        PerfilPreferencias p = perfiles.get(clienteId);
        Set<Long> base = new LinkedHashSet<>(comprados == null ? Set.of() : comprados);
        if (p != null)
            base.addAll(p.obtenerLibrosVisitados());
        Set<Long> categorias = p == null ? Set.of() : p.obtenerCategoriasPreferidas();
        if (base.isEmpty() && categorias.isEmpty())
            return recomendarGenerales(c, limite);
        Set<Long> comp = comprados == null ? Set.of() : comprados;
        List<Long> r = new ArrayList<>(relacionadosA(base, comp, c, limite));
        if (r.size() < limite)
            top(c, x -> categorias.contains(x.categoriaId()) && !comp.contains(x.libroId()) && !r.contains(x.libroId()),
                    limite - r.size())
                    .forEach(r::add);
        if (r.isEmpty())
            return recomendarGenerales(c, limite);
        return r.subList(0, Math.min(r.size(), limite));
    }

    /** HU-114 cliente: personalizadas si hay historial. */
    public List<Long> recomendarInicio(Long clienteId, Set<Long> comprados, List<LibroRecomendable> c, int limite) {
        return recomendarPerfil(clienteId, comprados, c, limite);
    }
}
