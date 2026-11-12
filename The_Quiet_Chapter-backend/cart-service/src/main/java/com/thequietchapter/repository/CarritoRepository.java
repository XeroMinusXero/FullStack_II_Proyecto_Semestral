package com.thequietchapter.repository;

import com.thequietchapter.model.Carrito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.clienteId = :clienteId and c.activo = true")
    Optional<Carrito> bloquearCliente(@Param("clienteId") Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.identificadorSesion = :sesion and c.activo = true")
    Optional<Carrito> bloquearSesion(@Param("sesion") String sesion);
}
