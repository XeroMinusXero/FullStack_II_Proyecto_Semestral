package com.thequietchapter.repository;

import com.thequietchapter.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProcesoCompraRepository extends JpaRepository<ProcesoCompra, Long> {
    Optional<ProcesoCompra> findByIdAndClienteId(Long id, Long clienteId);

    List<ProcesoCompra> findByClienteIdOrderByIdDesc(Long clienteId);

    Optional<ProcesoCompra> findFirstByClienteIdAndCarritoIdAndEstadoInOrderByIdDesc(Long clienteId, Long carritoId,
            List<EstadoCheckout> estados);
}
