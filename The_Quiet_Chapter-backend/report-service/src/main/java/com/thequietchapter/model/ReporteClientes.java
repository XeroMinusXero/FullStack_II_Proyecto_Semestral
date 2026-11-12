package com.thequietchapter.model;

import java.util.*;

/** DC-09 / HU-151..153. */
public class ReporteClientes {
    private final PeriodoReporte periodo;
    private final List<ClienteAnalitico> clientes;

    public ReporteClientes(PeriodoReporte periodo, List<ClienteAnalitico> clientes, List<PedidoAnalitico> pedidos) {
        this.periodo = periodo;
        this.clientes = clientes;
        Map<Long, ClienteAnalitico> porId = new HashMap<>();
        clientes.forEach(c -> porId.put(c.getClienteId(), c));
        pedidos.stream().filter(p -> p.esVentaContabilizable() && p.estaEnPeriodo(periodo))
                .forEach(p -> {
                    var c = porId.get(p.clienteId());
                    if (c != null)
                        c.sumarPedido(p.total());
                });
    }

    public long contarClientesNuevos() {
        return clientes.stream().filter(c -> c.estaRegistradoEn(periodo)).count();
    }

    public long contarClientesAcumulados() {
        return clientes.stream().filter(c -> c.registradoAntesDe(periodo.fechaHasta())).count();
    }

    public List<ClienteAnalitico> obtenerMayoresCompradores(int limite) {
        return clientes.stream().filter(ClienteAnalitico::tieneComprasEnPeriodo)
                .sorted(Comparator.comparing(ClienteAnalitico::getMontoCompradoPeriodo).reversed())
                .limit(Math.max(1, limite)).toList();
    }

    public List<ClienteAnalitico> obtenerClientesSinCompras() {
        return clientes.stream().filter(c -> c.registradoAntesDe(periodo.fechaHasta()) && !c.tieneComprasEnPeriodo())
                .sorted(Comparator.comparing(ClienteAnalitico::getNombre)).toList();
    }

    public List<ClienteAnalitico> getClientes() {
        return clientes;
    }
}
