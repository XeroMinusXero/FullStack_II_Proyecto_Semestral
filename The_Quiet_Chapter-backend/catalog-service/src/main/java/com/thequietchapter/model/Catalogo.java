package com.thequietchapter.model;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Raiz del dominio DC-01: posee la coleccion y sus operaciones. */
public final class Catalogo {
    private final List<Libro> libros = new ArrayList<>();

    public synchronized Libro registrarLibro(Libro libro) {
        if (libro == null || libro.getInventario() == null || libro.getFicha() == null)
            throw new IllegalArgumentException("Libro, ficha e inventario requeridos");
        if (libro.getCategoria() == null || libro.getEditorial() == null || libro.getAutores().isEmpty())
            throw new IllegalArgumentException("Categoria, editorial y autor son obligatorios");
        if (!isbnDisponible(libro.getIsbn(), null))
            throw new IllegalStateException("ISBN ya registrado");
        // Catálogo comparte clasificaciones con el mismo nombre, evitando categorías
        // distintas con IDs diferentes para libros de la misma categoría.
        libros.stream().map(Libro::getCategoria)
                .filter(c -> c != null
                        && normalizar(c.getNombre()).equals(normalizar(libro.getCategoria().getNombre())))
                .findFirst().ifPresent(libro::asignarCategoria);
        libros.stream().map(Libro::getEditorial)
                .filter(e -> e != null
                        && normalizar(e.getNombre()).equals(normalizar(libro.getEditorial().getNombre())))
                .findFirst().ifPresent(libro::asignarEditorial);
        var autoresCanonicos = libro.getAutores().stream().map(a -> libros.stream()
                .flatMap(l -> l.getAutores().stream())
                .filter(ex -> normalizar(ex.getNombre()).equals(normalizar(a.getNombre())))
                .findFirst().orElse(a)).toList();
        libro.reemplazarAutores(autoresCanonicos);
        var generosCanonicos = libro.getGeneros().stream().map(g -> libros.stream()
                .flatMap(l -> l.getGeneros().stream())
                .filter(ex -> normalizar(ex.getNombre()).equals(normalizar(g.getNombre())))
                .findFirst().orElse(g)).toList();
        libro.reemplazarGeneros(generosCanonicos);
        libros.add(libro);
        return libro;
    }

    public synchronized boolean eliminarLibro(Long libroId, boolean tienePedidos) {
        if (tienePedidos)
            return false;
        return libros.removeIf(l -> l.getId().equals(libroId));
    }

    public synchronized Optional<Libro> obtenerLibro(Long id) {
        return libros.stream().filter(l -> l.getId().equals(id)).findFirst();
    }

    public synchronized List<Libro> listarLibros(boolean incluirInactivos) {
        return libros.stream().filter(l -> incluirInactivos || l.isActivo()).toList();
    }

    public synchronized List<Libro> buscar(String texto, boolean incluirInactivos) {
        String clave = normalizar(texto);
        return listarLibros(incluirInactivos).stream()
                .filter(l -> normalizar(l.getTitulo()).contains(clave)
                        || normalizar(l.getIsbn()).contains(clave)
                        || l.getAutores().stream().anyMatch(a -> normalizar(a.getNombre()).contains(clave)))
                .toList();
    }

    public synchronized List<Libro> sugerir(String texto, int limite) {
        if (texto == null || texto.isBlank() || limite <= 0)
            return List.of();
        return buscar(texto, false).stream().limit(limite).toList();
    }

    public synchronized List<Libro> filtrar(CriteriosCatalogo c) {
        if (c == null || !c.esRangoPrecioValido())
            throw new IllegalArgumentException("Filtros de precio invalidos");
        return libros.stream()
                .filter(l -> c.getActivo() == null || l.isActivo() == c.getActivo())
                .filter(l -> !c.isSoloAgotados() || l.getInventario().estaAgotado())
                .filter(l -> c.getPrecioMinimo() == null || l.getPrecio().compareTo(c.getPrecioMinimo()) >= 0)
                .filter(l -> c.getPrecioMaximo() == null || l.getPrecio().compareTo(c.getPrecioMaximo()) <= 0)
                .filter(l -> c.getCategoriaIds().isEmpty()
                        || (l.getCategoria() != null && c.getCategoriaIds().contains(l.getCategoria().getId())))
                .filter(l -> c.getGeneroIds().isEmpty()
                        || l.getGeneros().stream().anyMatch(g -> c.getGeneroIds().contains(g.getId())))
                .filter(l -> c.getAutorId() == null
                        || l.getAutores().stream().anyMatch(a -> a.getId().equals(c.getAutorId())))
                .filter(l -> c.getEditorialId() == null
                        || (l.getEditorial() != null && l.getEditorial().getId().equals(c.getEditorialId())))
                .toList();
    }

    public List<Libro> ordenar(List<Libro> libros, OrdenCatalogo criterio) {
        if (libros == null)
            throw new IllegalArgumentException("Lista requerida");
        if (criterio == null)
            return List.copyOf(libros);
        Comparator<Libro> comparator = switch (criterio) {
            case PRECIO_ASC -> Comparator.comparing(Libro::getPrecio);
            case PRECIO_DESC -> Comparator.comparing(Libro::getPrecio).reversed();
            case TITULO_ASC -> Comparator.comparing(l -> normalizar(l.getTitulo()));
            case NOVEDADES -> Comparator.comparing(Libro::getFechaRegistro).reversed();
        };
        return libros.stream().sorted(comparator).toList();
    }

    public List<Libro> paginar(List<Libro> libros, int pagina, int tamanio) {
        if (libros == null || pagina < 0 || tamanio <= 0 || tamanio > 200)
            throw new IllegalArgumentException("Paginacion invalida");
        long desde = (long) pagina * tamanio;
        if (desde >= libros.size())
            return List.of();
        return List.copyOf(libros.subList((int) desde, (int) Math.min(desde + tamanio, libros.size())));
    }

    public synchronized boolean isbnDisponible(String isbn, Long exceptoLibroId) {
        if (isbn == null || isbn.isBlank())
            return false;
        String clave = isbn.strip().toLowerCase(Locale.ROOT);
        return libros.stream().noneMatch(l -> l.getIsbn().strip().toLowerCase(Locale.ROOT).equals(clave)
                && (exceptoLibroId == null || !l.getId().equals(exceptoLibroId)));
    }

    private static String normalizar(String t) {
        if (t == null)
            return "";
        return Normalizer.normalize(t.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").strip();
    }
}
