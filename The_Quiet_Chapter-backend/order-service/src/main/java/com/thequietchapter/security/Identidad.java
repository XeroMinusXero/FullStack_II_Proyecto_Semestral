package com.thequietchapter.security;

/** Identidad extraida de un JWT valido emitido por user-service. */
public record Identidad(Long usuarioId, String rol) {
    public static final String ADMINISTRADOR = "ADMINISTRADOR";

    public boolean esAdmin() {
        return ADMINISTRADOR.equals(rol);
    }
}
