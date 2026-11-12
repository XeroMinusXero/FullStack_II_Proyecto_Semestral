package com.thequietchapter.model;

import java.util.*;

/** DC-08: alta de suscripcion; un correo CONFIRMADO no se duplica (HU-132). */
public class RegistroSuscriptores {
    public Optional<SuscripcionBoletin> registrarSuscripcion(Optional<SuscripcionBoletin> existente, String email,
            String tokenConfirmacion, String tokenCancelacion) {
        if (existente.isPresent()) {
            SuscripcionBoletin s = existente.get();
            if (s.estaActiva())
                return Optional.empty();
            s.reiniciar(tokenConfirmacion, tokenCancelacion);
            return Optional.of(s);
        }
        return Optional.of(new SuscripcionBoletin(email, tokenConfirmacion, tokenCancelacion));
    }
}
