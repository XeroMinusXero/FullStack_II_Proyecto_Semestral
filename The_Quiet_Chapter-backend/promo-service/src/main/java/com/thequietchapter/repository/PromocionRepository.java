package com.thequietchapter.repository;

import com.thequietchapter.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByActivoTrue();
}
