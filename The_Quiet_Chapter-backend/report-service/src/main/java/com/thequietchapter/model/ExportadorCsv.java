package com.thequietchapter.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * DC-09 / HU-154..157. Separador ';' (Excel en configuracion regional de
 * Chile). Vacio si no hay datos.
 */
public class ExportadorCsv {
    private final char separador;
    private final boolean incluirEncabezados;

    public ExportadorCsv(char separador, boolean incluirEncabezados) {
        this.separador = separador;
        this.incluirEncabezados = incluirEncabezados;
    }

    private String celda(Object v) {
        String s = v == null ? "" : (v instanceof BigDecimal b ? b.toPlainString() : String.valueOf(v));
        boolean comillas = s.indexOf(separador) >= 0 || s.contains("\"") || s.contains("\n");
        return comillas ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private Optional<String> tabla(List<String> encabezados, List<List<Object>> filas) {
        if (filas.isEmpty())
            return Optional.empty();
        StringBuilder sb = new StringBuilder();
        String sep = String.valueOf(separador);
        if (incluirEncabezados)
            sb.append(String.join(sep, encabezados)).append("\r\n");
        for (List<Object> f : filas)
            sb.append(String.join(sep, f.stream().map(this::celda).toList())).append("\r\n");
        return Optional.of(sb.toString());
    }

    public Optional<String> exportarVentas(ReporteVentas r) {
        return tabla(List.of("numero_pedido", "fecha", "cliente", "estado", "unidades", "total"),
                r.listarVentas().stream().map(p -> Arrays.<Object>asList(p.numeroPedido(), p.fecha().toString(),
                        p.clienteNombre() == null ? p.clienteId() : p.clienteNombre(), p.estado().name(),
                        p.cantidadUnidades(), p.total())).toList());
    }

    public Optional<String> exportarMasVendidos(ReporteLibros r, int limite) {
        return tabla(List.of("libro_id", "titulo", "autor", "unidades_vendidas", "monto_total", "stock_actual"),
                r.obtenerMasVendidos(limite).stream()
                        .map(l -> Arrays.<Object>asList(l.getLibroId(), l.getTitulo(), l.getAutor(),
                                l.getUnidadesVendidas(), l.getMontoTotal(), l.getStockActual()))
                        .toList());
    }

    public Optional<String> exportarInventario(ReporteInventario r) {
        return tabla(
                List.of("libro_id", "titulo", "autor", "activo", "stock_actual", "precio_venta", "valor_total",
                        "unidades_vendidas", "rotacion"),
                r.obtenerValorizacion().stream().map(l -> Arrays.<Object>asList(l.getLibroId(), l.getTitulo(),
                        l.getAutor(), l.isActivo() ? "SI" : "NO",
                        l.getStockActual(), l.getPrecioVenta(), l.calcularValorizacion(), l.getUnidadesVendidas(),
                        l.calcularRotacion().map(BigDecimal::toPlainString).orElse(""))).toList());
    }

    public Optional<String> exportarClientes(ReporteClientes r) {
        return tabla(List.of("cliente_id", "nombre", "email", "fecha_registro", "pedidos_periodo", "monto_periodo"),
                r.getClientes().stream().map(c -> Arrays.<Object>asList(c.getClienteId(), c.getNombre(), c.getEmail(),
                        c.getFechaRegistro() == null ? "" : c.getFechaRegistro().toLocalDate().toString(),
                        c.getCantidadPedidosPeriodo(), c.getMontoCompradoPeriodo())).toList());
    }
}
