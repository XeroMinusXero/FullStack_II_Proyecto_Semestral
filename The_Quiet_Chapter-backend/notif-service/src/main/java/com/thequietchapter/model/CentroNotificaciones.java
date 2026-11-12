package com.thequietchapter.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DC-08: despacho de notificaciones. Envio SIMULADO: se registra en log y queda
 * en la bandeja (tabla notificaciones).
 */
public class CentroNotificaciones {
    private static final Logger log = LoggerFactory.getLogger(CentroNotificaciones.class);

    public boolean registrarNotificacion(Notificacion n) {
        try {
            log.info("[CORREO SIMULADO] para={} tipo={} asunto=\"{}\"", n.getDestinatario(), n.getTipo(),
                    n.getAsunto());
            n.marcarEnviada();
            return true;
        } catch (RuntimeException e) {
            n.registrarFallo(e.getMessage());
            return false;
        }
    }
}
