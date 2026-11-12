package com.thequietchapter.model;

import java.math.BigDecimal;
import java.util.*;

/** DC-09 / HU-148..150. */
public class ReporteInventario {
    private final PeriodoReporte periodo;
    private final List<LibroAnalitico> libros;
    private final List<MovimientoStockAnalitico> movimientos;

    public ReporteInventario(PeriodoReporte periodo, List<LibroAnalitico> libros,
            List<MovimientoStockAnalitico> movimientos) {
        this.periodo = periodo;
        this.libros = libros;
        this.movimientos = movimientos;
    }

    public List<LibroAnalitico> obtenerRotacion() {
        return libros
                .stream().sorted(Comparator
                        .comparing((LibroAnalitico l) -> l.calcularRotacion().orElse(BigDecimal.ZERO)).reversed())
                .toList();
    }

    public List<LibroAnalitico> obtenerValorizacion() {
        return libros.stream().sorted(Comparator.comparing(LibroAnalitico::calcularValorizacion).reversed()).toList();
    }

    public BigDecimal calcularValorTotal() {
        return libros.stream().map(LibroAnalitico::calcularValorizacion).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<MovimientoStockAnalitico> obtenerMovimientosConsolidados() {
        return movimientos.stream().filter(m -> m.esIngresoOAjuste() && m.ocurrioEn(periodo))
                .sorted(Comparator.comparing(MovimientoStockAnalitico::fecha).reversed()).toList();
    }
}
