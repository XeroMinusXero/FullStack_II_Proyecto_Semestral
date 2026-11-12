package com.thequietchapter.repository;

import com.thequietchapter.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByLibroId(Long libroId);

    Optional<Calificacion> findByClienteIdAndLibroId(Long clienteId, Long libroId);

    List<Calificacion> findByClienteIdAndLibroIdIn(Long clienteId, Collection<Long> libroIds);

    /** [libroId, promedio, cantidad] para tarjetas de catalogo en un solo query. */
    @Query("select c.libroId, avg(c.puntuacion), count(c) from Calificacion c where c.libroId in :ids group by c.libroId")
    List<Object[]> promedios(@Param("ids") Collection<Long> ids);
}
