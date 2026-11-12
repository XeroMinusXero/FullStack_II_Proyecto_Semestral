package com.thequietchapter.dto;

/**
 * Pago SIMULADO (no hay pasarela real): simularRechazo=true permite demostrar
 * la HU de pago rechazado.
 */
public record PagarSolicitud(boolean simularRechazo) {
}
