package com.thequietchapter.repository;

import com.thequietchapter.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {
    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, Long id);

    Optional<Libro> findByIsbnIgnoreCase(String isbn);

    List<Libro> findByIdIn(Collection<Long> ids);

    /**
     * Serializa cambios de inventario por libro (fila de inventarios) sin bloquear
     * tablas completas.
     * Compatible con MySQL/MariaDB de XAMPP. Se invoca dentro de una transaccion
     * ANTES de cargar la entidad.
     */
    @Query(value = "SELECT i.id FROM inventarios i " +
            "INNER JOIN libros l ON l.inventario_id = i.id " +
            "WHERE l.id = :libroId FOR UPDATE", nativeQuery = true)
    Optional<Long> bloquearInventario(@Param("libroId") Long libroId);
}
