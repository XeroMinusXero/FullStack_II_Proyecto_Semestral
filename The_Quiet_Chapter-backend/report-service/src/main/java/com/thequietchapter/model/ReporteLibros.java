package com.thequietchapter.model;

import java.util.*;

/** DC-09 / HU-145..147. */
public class ReporteLibros {
    private final PeriodoReporte periodo;
    private final List<LibroAnalitico> libros;

    public ReporteLibros(PeriodoReporte periodo, List<LibroAnalitico> libros) {
        this.periodo = periodo;
        this.libros = libros;
    }

    public void actualizarMetricas(List<PedidoAnalitico> pedidos) {
        Map<Long, LibroAnalitico> porId = new HashMap<>();
        libros.forEach(l -> porId.put(l.getLibroId(), l));
        pedidos.stream().filter(p -> p.esVentaContabilizable() && p.estaEnPeriodo(periodo))
                .flatMap(p -> p.lineas().stream())
                .forEach(l -> {
                    var a = porId.get(l.libroId());
                    if (a != null)
                        a.actualizarMetricas(l.cantidad(), l.calcularSubtotal());
                });
    }

    public List<LibroAnalitico> obtenerMasVendidos(int limite) {
        return libros.stream().filter(LibroAnalitico::tieneVentas)
                .sorted(Comparator.comparingInt(LibroAnalitico::getUnidadesVendidas).reversed()
                        .thenComparing(LibroAnalitico::getTitulo))
                .limit(Math.max(1, limite)).toList();
    }

    public List<LibroAnalitico> obtenerMenosVendidos(int limite) {
        return libros.stream().filter(LibroAnalitico::isActivo)
                .sorted(Comparator.comparingInt(LibroAnalitico::getUnidadesVendidas)
                        .thenComparing(LibroAnalitico::getTitulo))
                .limit(Math.max(1, limite)).toList();
    }

    /** HU-147: solo libros activos. */
    public List<LibroAnalitico> obtenerSinVentas() {
        return libros.stream().filter(l -> l.isActivo() && !l.tieneVentas())
                .sorted(Comparator.comparing(LibroAnalitico::getTitulo)).toList();
    }

    public List<LibroAnalitico> getLibros() {
        return libros;
    }
}
