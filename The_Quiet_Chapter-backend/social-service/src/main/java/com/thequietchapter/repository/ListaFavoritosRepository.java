package com.thequietchapter.repository;

import com.thequietchapter.model.ListaFavoritos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ListaFavoritosRepository extends JpaRepository<ListaFavoritos, Long> {
    Optional<ListaFavoritos> findByClienteId(Long clienteId);
}
