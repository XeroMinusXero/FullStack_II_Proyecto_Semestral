package com.thequietchapter.service;

import com.thequietchapter.client.DatosHttpClient;
import com.thequietchapter.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Arma los agregados DC-09 a partir de los datos de los otros microservicios.
 */
@Service
public class ReportesService {
    private final DatosHttpClient datos;

    public ReportesService(DatosHttpClient datos) {
        this.datos = datos;
    }

    public record Respuesta<T>(PeriodoReporte periodo, T datos) {
    }

    private List<PedidoAnalitico> pedidos(PeriodoReporte p, Map<Long, DatosHttpClient.Libro> libros,
            boolean conNombres) {
        var crudos = datos.pedidos(p.fechaDesde(), p.fechaHasta());
        Map<Long, String> nombres = conNombres
                ? datos.nombres(crudos.stream().map(DatosHttpClient.Pedido::clienteId).toList())
                : Map.of();
        return crudos.stream()
                .map(x -> new PedidoAnalitico(x.numeroPedido(), x.fechaCreacion(), x.clienteId(),
                        nombres.get(x.clienteId()),
                        EstadoPedido.valueOf(x.estado()), x.importes() == null ? BigDecimal.ZERO : x.importes().total(),
                        x.detalles().stream().map(d -> {
                            var l = libros.get(d.libroId());
                            return new LineaPedidoAnalitica(d.libroId(), d.tituloLibro(), l == null ? "" : l.autores(),
                                    l == null ? null : l.categoriaId(), l == null ? null : l.categoriaNombre(),
                                    d.cantidad(), d.precioUnitario());
                        }).toList()))
                .toList();
    }

    private Map<Long, DatosHttpClient.Libro> librosPorId() {
        Map<Long, DatosHttpClient.Libro> m = new HashMap<>();
        datos.libros().forEach(l -> m.put(l.id(), l));
        return m;
    }

    private static List<LibroAnalitico> analiticos(Map<Long, DatosHttpClient.Libro> libros) {
        return libros.values().stream()
                .map(l -> new LibroAnalitico(l.id(), l.titulo(), l.autores(), l.activo(), l.stock(), l.precio()))
                .toList();
    }

    private List<ClienteAnalitico> clientes() {
        return datos.clientes().stream()
                .map(u -> new ClienteAnalitico(u.id(), u.nombreCompleto(), u.email(), u.fechaRegistro())).toList();
    }

    public ReporteVentas ventas(PeriodoReporte p) {
        return new ReporteVentas(p, pedidos(p, librosPorId(), true));
    }

    public ReporteLibros libros(PeriodoReporte p) {
        var libros = librosPorId();
        var r = new ReporteLibros(p, analiticos(libros));
        r.actualizarMetricas(pedidos(p, libros, false));
        return r;
    }

    public ReporteInventario inventario(PeriodoReporte p) {
        var libros = librosPorId();
        var analiticos = analiticos(libros);
        new ReporteLibros(p, analiticos).actualizarMetricas(pedidos(p, libros, false));
        var movs = datos.movimientos(p.fechaDesde(), p.fechaHasta());
        var nombres = datos.nombres(
                movs.stream().map(DatosHttpClient.Movimiento::responsableId).filter(Objects::nonNull).toList());
        var analiticosMov = movs
                .stream().map(
                        m -> new MovimientoStockAnalitico(m.movimientoId(), m.libroId(), m.tituloLibro(), m.fecha(),
                                TipoMovimiento.valueOf(m.tipo()), m.cantidad(), m.responsableId(),
                                m.responsableId() == null ? "Sistema"
                                        : nombres.getOrDefault(m.responsableId(), "Usuario " + m.responsableId())))
                .toList();
        return new ReporteInventario(p, analiticos, analiticosMov);
    }

    public ReporteClientes clientes(PeriodoReporte p) {
        return new ReporteClientes(p, clientes(), pedidos(p, Map.of(), false));
    }

    public PanelIndicadores panel(PeriodoReporte p, ReporteVentas v) {
        return new PanelIndicadores(v, new ReporteClientes(p, clientes(), v.listarVentas()));
    }
}
