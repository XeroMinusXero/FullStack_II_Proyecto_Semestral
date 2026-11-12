package com.thequietchapter.controller;

import com.thequietchapter.model.*;
import com.thequietchapter.security.Autenticacion;
import com.thequietchapter.service.ReportesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * DC-09 (EP-09). Todo requiere ADMINISTRADOR. Periodo:
 * ?periodo=HOY|SEMANA|MES|ANIO o ?desde=YYYY-MM-DD&hasta=YYYY-MM-DD.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesController {
    private static final String AUTH = HttpHeaders.AUTHORIZATION;
    private final ReportesService service;
    private final Autenticacion auth;

    public ReportesController(ReportesService service, Autenticacion auth) {
        this.service = service;
        this.auth = auth;
    }

    private PeriodoReporte periodo(String a, String periodo, LocalDate desde, LocalDate hasta) {
        auth.admin(a);
        return PeriodoReporte.resolver(periodo, desde, hasta);
    }

    /**
     * HU-140..142: KPIs + evolucion + pedidos por estado en una sola llamada para
     * el dashboard.
     */
    @GetMapping("/panel")
    public Map<String, Object> panel(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        PeriodoReporte p = periodo(a, periodo, desde, hasta);
        ReporteVentas v = service.ventas(p);
        PanelIndicadores k = service.panel(p, v);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("periodo", p);
        r.put("ventasPeriodo", k.obtenerVentasPeriodo());
        r.put("cantidadPedidos", k.obtenerCantidadPedidos());
        r.put("ticketPromedio", k.calcularTicketPromedio());
        r.put("clientesRegistrados", k.obtenerClientesRegistrados());
        r.put("agrupacion", v.sugerida());
        r.put("evolucion", v.calcularEvolucion(null));
        r.put("pedidosPorEstado", v.contarPedidosPorEstado());
        r.put("sinDatos", v.listarVentas().isEmpty());
        return r;
    }

    /** HU-141, 143, 144. */
    @GetMapping("/ventas")
    public Map<String, Object> ventas(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) AgrupacionTemporal agrupacion) {
        ReporteVentas v = service.ventas(periodo(a, periodo, desde, hasta));
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("periodo", v.getPeriodo());
        r.put("cantidadPedidos", v.contarPedidos());
        r.put("montoTotal", v.calcularMontoTotal());
        r.put("agrupacion", agrupacion == null ? v.sugerida() : agrupacion);
        r.put("evolucion", v.calcularEvolucion(agrupacion));
        r.put("pedidosPorEstado", v.contarPedidosPorEstado());
        r.put("porCategoria", v.ventasPorCategoria());
        r.put("pedidos", v.listarVentas());
        return r;
    }

    /** HU-145..147. */
    @GetMapping("/libros")
    public Map<String, Object> libros(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limite) {
        PeriodoReporte p = periodo(a, periodo, desde, hasta);
        ReporteLibros r = service.libros(p);
        return Map.of("periodo", p, "masVendidos", r.obtenerMasVendidos(limite), "menosVendidos",
                r.obtenerMenosVendidos(limite),
                "sinVentas", r.obtenerSinVentas());
    }

    /** HU-148..150. */
    @GetMapping("/inventario")
    public Map<String, Object> inventario(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        PeriodoReporte p = periodo(a, periodo, desde, hasta);
        ReporteInventario r = service.inventario(p);
        List<Map<String, Object>> rotacion = r.obtenerRotacion().stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("libroId", l.getLibroId());
            m.put("titulo", l.getTitulo());
            m.put("autor", l.getAutor());
            m.put("unidadesVendidas", l.getUnidadesVendidas());
            m.put("stockActual", l.getStockActual());
            m.put("stockPromedio", l.getStockPromedio());
            m.put("rotacion", l.calcularRotacion().orElse(null));
            m.put("precioVenta", l.getPrecioVenta());
            m.put("valorTotal", l.calcularValorizacion());
            m.put("activo", l.isActivo());
            return m;
        }).toList();
        return Map.of("periodo", p, "libros", rotacion, "valorTotalInventario", r.calcularValorTotal(),
                "movimientos", r.obtenerMovimientosConsolidados());
    }

    /** HU-151..153. */
    @GetMapping("/clientes")
    public Map<String, Object> clientes(@RequestHeader(value = AUTH, required = false) String a,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limite) {
        PeriodoReporte p = periodo(a, periodo, desde, hasta);
        ReporteClientes r = service.clientes(p);
        return Map.of("periodo", p, "clientesNuevos", r.contarClientesNuevos(), "clientesAcumulados",
                r.contarClientesAcumulados(),
                "mayoresCompradores", r.obtenerMayoresCompradores(limite), "sinCompras", r.obtenerClientesSinCompras());
    }

    /** HU-154..157: ventas | libros-mas-vendidos | inventario | clientes. */
    @GetMapping("/{tipo}/csv")
    public ResponseEntity<byte[]> csv(@RequestHeader(value = AUTH, required = false) String a,
            @PathVariable String tipo,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limite) {
        PeriodoReporte p = periodo(a, periodo, desde, hasta);
        ExportadorCsv csv = new ExportadorCsv(';', true);
        Optional<String> contenido = switch (tipo) {
            case "ventas" -> csv.exportarVentas(service.ventas(p));
            case "libros-mas-vendidos" -> csv.exportarMasVendidos(service.libros(p), limite);
            case "inventario" -> csv.exportarInventario(service.inventario(p));
            case "clientes" -> csv.exportarClientes(service.clientes(p));
            default -> throw new IllegalArgumentException("Reporte desconocido: " + tipo);
        };
        String texto = contenido
                .orElseThrow(() -> new NoSuchElementException("No hay datos para exportar en el periodo seleccionado"));
        byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] cuerpo = texto.getBytes(StandardCharsets.UTF_8);
        byte[] salida = new byte[bom.length + cuerpo.length];
        System.arraycopy(bom, 0, salida, 0, bom.length);
        System.arraycopy(cuerpo, 0, salida, bom.length, cuerpo.length);
        String nombre = "tqc-" + tipo + "-" + p.fechaDesde() + "_" + p.fechaHasta() + ".csv";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(salida);
    }
}
