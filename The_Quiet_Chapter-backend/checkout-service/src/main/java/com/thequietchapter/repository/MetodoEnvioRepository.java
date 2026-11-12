package com.thequietchapter.repository;

import com.thequietchapter.model.MetodoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetodoEnvioRepository extends JpaRepository<MetodoEnvio, Long> {
    List<MetodoEnvio> findByActivoTrueOrderByNombreAsc();
}
