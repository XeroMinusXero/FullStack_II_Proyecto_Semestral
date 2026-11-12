package com.thequietchapter.dto;

import com.thequietchapter.model.ReporteResena;
import com.thequietchapter.model.Resena;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contratos JSON de social-service (records agrupados para mantener el servicio
 * compacto).
 */
public final class Dtos {
    private Dtos() {
    }

    public record FavoritoRespuesta(Long libroId, LocalDateTime fechaAgregado) {
    }

    public record ResenaSolicitud(@NotBlank @Size(min = Resena.MIN, max = Resena.MAX) String contenido,
            @Min(1) @Max(5) Integer puntuacion) {
    }

    public record EdicionSolicitud(@NotBlank @Size(min = Resena.MIN, max = Resena.MAX) String contenido) {
    }

    public record CalificacionSolicitud(@Min(1) @Max(5) int puntuacion) {
    }

    public record ReporteSolicitud(@NotBlank @Size(max = 500) String motivo) {
    }

    public record ReporteRespuesta(Long id, Long clienteReportanteId, String motivo, LocalDateTime fechaReporte,
            String estado, Long administradorId, LocalDateTime fechaResolucion) {
        public static ReporteRespuesta desde(ReporteResena r) {
            return new ReporteRespuesta(r.getId(), r.getClienteReportanteId(), r.getMotivo(), r.getFechaReporte(),
                    r.getEstado().name(), r.getAdministradorId(), r.getFechaResolucion());
        }
    }

    public record ResenaRespuesta(Long id, Long libroId, Long clienteId, String autorNombre, String contenido,
            LocalDateTime fechaPublicacion, LocalDateTime fechaActualizacion, boolean editada,
            String estado, int votosUtiles, Integer puntuacion, boolean marcadaUtilPorMi,
            boolean propia, List<ReporteRespuesta> reportes) {
    }

    public record OpinionesLibro(Long libroId, BigDecimal promedio, int cantidadCalificaciones, Integer miCalificacion,
            boolean puedoOpinar, List<ResenaRespuesta> resenas) {
    }

    public record Promedio(Long libroId, BigDecimal promedio, long cantidad) {
    }

    public record PaginaResenas(List<ResenaRespuesta> resenas, int pagina, int tamano, long totalElementos,
            int totalPaginas) {
    }
}
