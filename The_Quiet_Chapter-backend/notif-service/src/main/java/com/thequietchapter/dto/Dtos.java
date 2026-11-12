package com.thequietchapter.dto;

import com.thequietchapter.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {
    }

    /**
     * Interno: destinatario directo O clienteId (se resuelve el correo en
     * user-service).
     */
    public record EnviarSolicitud(String destinatario, Long clienteId, @NotBlank String tipo,
            @NotBlank @Size(max = 200) String asunto,
            @NotBlank String mensaje, String enlaceAccion) {
    }

    public record DisponibleSolicitud(String titulo) {
    }

    public record NotificacionRespuesta(Long id, String destinatario, String tipo, String asunto, String mensaje,
            String enlaceAccion,
            String estado, LocalDateTime fechaCreacion, LocalDateTime fechaEnvio) {
        public static NotificacionRespuesta desde(Notificacion n) {
            return new NotificacionRespuesta(n.getId(), n.getDestinatario(), n.getTipo().name(), n.getAsunto(),
                    n.getMensaje(),
                    n.getEnlaceAccion(), n.getEstado().name(), n.getFechaCreacion(), n.getFechaEnvio());
        }
    }

    public record AvisoRespuesta(Long id, Long libroId, LocalDateTime fechaSolicitud, boolean activa) {
        public static AvisoRespuesta desde(SolicitudReabastecimiento s) {
            return new AvisoRespuesta(s.getId(), s.getLibroId(), s.getFechaSolicitud(), s.estaActiva());
        }
    }

    public record SuscribirSolicitud(@NotBlank @Email @Size(max = 180) String email) {
    }

    public record TokenSolicitud(@NotBlank @Size(max = 64) String token) {
    }

    public record Mensaje(String mensaje) {
    }

    public record SuscriptorRespuesta(Long id, String email, LocalDateTime fechaSuscripcion,
            LocalDateTime fechaConfirmacion, String estado) {
        public static SuscriptorRespuesta desde(SuscripcionBoletin s) {
            return new SuscriptorRespuesta(s.getId(), s.getEmail(), s.getFechaSuscripcion(), s.getFechaConfirmacion(),
                    s.getEstado().name());
        }
    }

    public record BoletinSolicitud(@NotBlank @Size(max = 200) String asunto, @NotBlank String contenido) {
    }

    public record BoletinRespuesta(Long id, String asunto, String contenido, String estado, LocalDateTime fechaCreacion,
            LocalDateTime fechaEnvio, int cantidadDestinatarios) {
        public static BoletinRespuesta desde(Boletin b) {
            return new BoletinRespuesta(b.getId(), b.getAsunto(), b.getContenido(), b.getEstado().name(),
                    b.getFechaCreacion(),
                    b.getFechaEnvio(), b.getCantidadDestinatarios());
        }
    }

    public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
    }
}
