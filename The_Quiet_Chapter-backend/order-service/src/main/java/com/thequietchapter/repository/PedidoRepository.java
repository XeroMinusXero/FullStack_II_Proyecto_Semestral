package com.thequietchapter.repository;

import com.thequietchapter.model.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    Optional<Pedido> findByReferenciaPago(String referenciaPago);

    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> bloquear(@Param("id") Long id);

    @Query("select count(d) > 0 from DetallePedido d where d.libroId = :libroId")
    boolean existeLibro(@Param("libroId") Long libroId);

    /**
     * Compra verificada (DC-06 resenas/calificaciones) y recomendaciones segun
     * compras (DC-07). Excluye cancelados.
     */
    @Query("select distinct d.libroId from DetallePedido d where d.pedido.clienteId = :clienteId and d.pedido.estado <> :excluido")
    List<Long> librosComprados(@Param("clienteId") Long clienteId,
            @Param("excluido") com.thequietchapter.model.EstadoPedido excluido);

    List<Pedido> findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThanOrderByFechaCreacionAsc(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}
