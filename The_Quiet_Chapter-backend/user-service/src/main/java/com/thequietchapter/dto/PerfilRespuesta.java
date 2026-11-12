package com.thequietchapter.dto;

import com.thequietchapter.model.Usuario;
import com.thequietchapter.model.Direccion;
import java.time.LocalDateTime;
import java.util.List;

public record PerfilRespuesta(Long id, String rol, String nombre, String apellido, String email, String telefono,
        LocalDateTime fechaRegistro, List<DireccionRespuesta> direcciones) {
    public static PerfilRespuesta desde(Usuario u) {
        List<DireccionRespuesta> ds = u.getLibreta() == null ? List.of()
                : u.getLibreta().listarDirecciones().stream().map(DireccionRespuesta::desde).toList();
        return new PerfilRespuesta(u.getId(), u.getRol().name(), u.getNombre(), u.getApellido(), u.getEmail(),
                u.getTelefono(), u.getFechaRegistro(), ds);
    }
}
