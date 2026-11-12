package com.thequietchapter.repository;

import com.thequietchapter.model.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Traduce CriteriosCatalogo (DC-01) a SQL: el filtrado y la paginacion ocurren
 * en MySQL,
 * no cargando todo el catalogo en memoria. Con collation utf8mb4_unicode_ci la
 * busqueda ignora tildes.
 */
public final class LibroEspecificaciones {
    private LibroEspecificaciones() {
    }

    public static Specification<Libro> segun(String texto, CriteriosCatalogo c) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (c.getActivo() != null)
                p.add(cb.equal(root.get("activo"), c.getActivo()));
            if (c.isSoloAgotados())
                p.add(cb.equal(root.get("inventario").get("stockActual"), 0));
            if (c.getPrecioMinimo() != null)
                p.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("precio"), c.getPrecioMinimo()));
            if (c.getPrecioMaximo() != null)
                p.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("precio"), c.getPrecioMaximo()));
            if (!c.getCategoriaIds().isEmpty())
                p.add(root.get("categoria").get("id").in(c.getCategoriaIds()));
            if (c.getEditorialId() != null)
                p.add(cb.equal(root.get("editorial").get("id"), c.getEditorialId()));
            if (!c.getGeneroIds().isEmpty()) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<Libro> l = sq.from(Libro.class);
                Join<Libro, Genero> g = l.join("generos");
                sq.select(l.get("id")).where(cb.equal(l.get("id"), root.get("id")), g.get("id").in(c.getGeneroIds()));
                p.add(cb.exists(sq));
            }
            if (c.getAutorId() != null) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<Libro> l = sq.from(Libro.class);
                Join<Libro, Autor> a = l.join("autores");
                sq.select(l.get("id")).where(cb.equal(l.get("id"), root.get("id")),
                        cb.equal(a.get("id"), c.getAutorId()));
                p.add(cb.exists(sq));
            }
            if (texto != null && !texto.isBlank()) {
                String patron = "%" + texto.strip().toLowerCase()
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                Subquery<Long> sq = query.subquery(Long.class);
                Root<Libro> l = sq.from(Libro.class);
                Join<Libro, Autor> a = l.join("autores");
                sq.select(l.get("id")).where(cb.equal(l.get("id"), root.get("id")),
                        cb.like(cb.lower(a.<String>get("nombre")), patron, '\\'));
                p.add(cb.or(cb.like(cb.lower(root.<String>get("titulo")), patron, '\\'),
                        cb.like(cb.lower(root.<String>get("isbn")), patron, '\\'),
                        cb.exists(sq)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
