package com.thequietchapter.dto;

import com.thequietchapter.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoPedidoSolicitud(@NotNull EstadoPedido estado) {
}
