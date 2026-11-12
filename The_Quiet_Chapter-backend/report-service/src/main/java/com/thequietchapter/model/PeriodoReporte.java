package com.thequietchapter.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/** DC-09: rango de fechas inclusivo. */
public record PeriodoReporte(LocalDate fechaDesde, LocalDate fechaHasta) {
    public PeriodoReporte {
        if (fechaDesde == null || fechaHasta == null)
            throw new IllegalArgumentException("Indica fecha desde y hasta");
        if (fechaDesde.isAfter(fechaHasta))
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
    }

    public boolean esValido() {
        return !fechaDesde.isAfter(fechaHasta);
    }

    public boolean incluye(LocalDateTime fecha) {
        return fecha != null && !fecha.toLocalDate().isBefore(fechaDesde) && !fecha.toLocalDate().isAfter(fechaHasta);
    }

    /** HU-140: periodos rapidos del dashboard. Sin nada: mes en curso. */
    public static PeriodoReporte resolver(String periodo, LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde != null || hasta != null)
            return new PeriodoReporte(desde == null ? hoy.withDayOfMonth(1) : desde, hasta == null ? hoy : hasta);
        return switch (periodo == null ? "MES" : periodo.toUpperCase()) {
            case "HOY" -> new PeriodoReporte(hoy, hoy);
            case "SEMANA" -> new PeriodoReporte(hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), hoy);
            case "ANIO" -> new PeriodoReporte(hoy.withDayOfYear(1), hoy);
            case "MES" -> new PeriodoReporte(hoy.withDayOfMonth(1), hoy);
            default -> throw new IllegalArgumentException("periodo debe ser HOY, SEMANA, MES o ANIO");
        };
    }
}
