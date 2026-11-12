package com.thequietchapter.model;

import java.util.*;

/**
 * Agregado de negocio DC-04. La numeración global se completa con el ID de JPA,
 * no con un contador en memoria.
 */
public class RegistroPedidos {
    private final List<Pedido> pedidos = new ArrayList<>();
    private long siguienteNumero = 1;

    public RegistroPedidos() {
    }

    public RegistroPedidos(List<Pedido> pedidos) {
        if (pedidos != null) {
            this.pedidos.addAll(pedidos);
            siguienteNumero = pedidos.size() + 1L;
        }
    }

    public Pedido crearPedido(Long clienteId, List<DetallePedido> detalles, DatosEntregaPedido entrega,
            ImportesPedido importes, String medioPago, String referenciaPago) {
        Pedido p = new Pedido(null, clienteId, detalles, entrega, importes, medioPago, referenciaPago);
        pedidos.add(p);
        siguienteNumero++;
        return p;
    }

    public Optional<Pedido> buscarPorNumero(String numero) {
        return pedidos.stream().filter(p -> Objects.equals(p.getNumeroPedido(), numero)).findFirst();
    }

    public List<Pedido> listar(int pagina, int tamano) {
        if (pagina < 0 || tamano <= 0)
            throw new IllegalArgumentException("Paginación inválida");
        long desde = (long) pagina * tamano;
        if (desde >= pedidos.size())
            return List.of();
        return List.copyOf(pedidos.subList((int) desde, (int) Math.min(desde + tamano, pedidos.size())));
    }

    public List<Pedido> filtrar(CriteriosPedido criterios, int pagina, int tamano) {
        if (criterios == null)
            return listar(pagina, tamano);
        return new RegistroPedidos(pedidos.stream()
                .filter(p -> (criterios.getEstado() == null || p.getEstado() == criterios.getEstado()) &&
                        (criterios.getFechaDesde() == null
                                || !p.getFechaCreacion().toLocalDate().isBefore(criterios.getFechaDesde()))
                        &&
                        (criterios.getFechaHasta() == null
                                || !p.getFechaCreacion().toLocalDate().isAfter(criterios.getFechaHasta())))
                .toList()).listar(pagina, tamano);
    }

    public long getSiguienteNumero() {
        return siguienteNumero;
    }
}
