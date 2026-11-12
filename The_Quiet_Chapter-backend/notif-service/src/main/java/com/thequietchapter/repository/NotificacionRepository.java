package com.thequietchapter.repository;

import com.thequietchapter.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long>, JpaSpecificationExecutor<Notificacion> {
    Page<Notificacion> findByDestinatario(String destinatario, Pageable pageable);
}
