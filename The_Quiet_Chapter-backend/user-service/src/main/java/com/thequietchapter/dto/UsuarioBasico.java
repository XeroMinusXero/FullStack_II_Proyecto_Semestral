package com.thequietchapter.dto;

import com.thequietchapter.model.Usuario;
import java.time.LocalDateTime;

/**
 * Datos minimos que otros microservicios (order, report, notif) necesitan: sin
 * hash ni direcciones.
 */
public record UsuarioBasico(Long id, String rol, String nombre, String apellido, String nombreCompleto, String email,
        LocalDateTime fechaRegistro) {
    public static UsuarioBasico desde(Usuario u) {
        return new UsuarioBasico(u.getId(), u.getRol().name(), u.getNombre(), u.getApellido(),
                u.obtenerNombreCompleto(), u.getEmail(), u.getFechaRegistro());
    }
}
