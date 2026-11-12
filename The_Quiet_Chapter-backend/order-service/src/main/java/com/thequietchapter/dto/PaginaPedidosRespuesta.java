package com.thequietchapter.dto;

import org.springframework.data.domain.Page;
import com.thequietchapter.model.Pedido;
import java.util.List;

public record PaginaPedidosRespuesta(List<PedidoRespuesta> pedidos, int pagina, int tamano, long totalElementos,
        int totalPaginas) {
    public PaginaPedidosRespuesta conPedidos(List<PedidoRespuesta> nuevos) {
        return new PaginaPedidosRespuesta(nuevos, pagina, tamano, totalElementos, totalPaginas);
    }

    public static PaginaPedidosRespuesta desde(Page<Pedido> pagina) {
        return new PaginaPedidosRespuesta(
                pagina.getContent().stream().map(PedidoRespuesta::desde).toList(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
