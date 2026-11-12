package com.thequietchapter.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;

/** DC-09 / HU-141..144. */
public class ReporteVentas {
    private final PeriodoReporte periodo;
    private final List<PedidoAnalitico> pedidos;

    public ReporteVentas(PeriodoReporte periodo, List<PedidoAnalitico> pedidos) {
        this.periodo = periodo;
        this.pedidos = pedidos.stream().filter(p -> p.estaEnPeriodo(periodo))
                .sorted(Comparator.comparing(PedidoAnalitico::fecha)).toList();
    }

    private List<PedidoAnalitico> ventas() {
        return pedidos.stream().filter(PedidoAnalitico::esVentaContabilizable).toList();
    }

    public List<PedidoAnalitico> listarVentas() {
        return pedidos;
    }

    public long contarPedidos() {
        return ventas().size();
    }

    public BigDecimal calcularMontoTotal() {
        return ventas().stream().map(PedidoAnalitico::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String clave(LocalDate d, AgrupacionTemporal a) {
        return switch (a) {
            case DIA -> d.toString();
            case SEMANA -> d.get(IsoFields.WEEK_BASED_YEAR) + "-S"
                    + String.format("%02d", d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MES -> d.getYear() + "-" + String.format("%02d", d.getMonthValue());
        };
    }

    /**
     * Serie completa (incluye buckets en 0) para que el grafico no tenga huecos
     * (HU-141).
     */
    public Map<String, BigDecimal> calcularEvolucion(AgrupacionTemporal agrupacion) {
        AgrupacionTemporal a = agrupacion == null ? sugerida() : agrupacion;
        Map<String, BigDecimal> serie = new LinkedHashMap<>();
        for (LocalDate d = periodo.fechaDesde(); !d.isAfter(periodo.fechaHasta()); d = d.plusDays(1))
            serie.putIfAbsent(clave(d, a), BigDecimal.ZERO);
        ventas().forEach(p -> serie.merge(clave(p.fecha().toLocalDate(), a), p.total(), BigDecimal::add));
        return serie;
    }

    /** Dia hasta 31 dias, semana hasta ~6 meses, si no mes. */
    public AgrupacionTemporal sugerida() {
        long dias = periodo.fechaHasta().toEpochDay() - periodo.fechaDesde().toEpochDay() + 1;
        return dias <= 31 ? AgrupacionTemporal.DIA : dias <= 186 ? AgrupacionTemporal.SEMANA : AgrupacionTemporal.MES;
    }

    public Map<String, Long> contarPedidosPorEstado() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (EstadoPedido e : EstadoPedido.values())
            m.put(e.name(), 0L);
        pedidos.forEach(p -> m.merge(p.estado().name(), 1L, Long::sum));
        return m;
    }

    public List<VentaPorCategoria> ventasPorCategoria() {
        Map<Long, VentaPorCategoria> m = new LinkedHashMap<>();
        ventas().stream().flatMap(
                p -> p.lineas().stream()).forEach(
                        l -> m.computeIfAbsent(l.categoriaId() == null ? -1L : l.categoriaId(),
                                k -> new VentaPorCategoria(l.categoriaId(),
                                        l.categoriaNombre() == null ? "Sin categoria" : l.categoriaNombre()))
                                .sumarLinea(l));
        return m.values().stream().sorted(Comparator.comparing(VentaPorCategoria::getMontoTotal).reversed()).toList();
    }

    public PeriodoReporte getPeriodo() {
        return periodo;
    }
}
