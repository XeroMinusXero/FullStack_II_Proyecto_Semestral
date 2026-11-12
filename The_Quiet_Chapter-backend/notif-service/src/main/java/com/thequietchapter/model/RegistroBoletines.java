package com.thequietchapter.model;

import java.util.List;

/**
 * DC-08: prepara un correo por suscriptor activo (los cancelados no reciben,
 * HU-134).
 */
public class RegistroBoletines {
    public interface Enlaces {
        String cancelacion(SuscripcionBoletin s);
    }

    public List<Notificacion> prepararEnvio(Boletin boletin, List<SuscripcionBoletin> suscriptores, Enlaces enlaces) {
        if (boletin == null || !boletin.puedeEnviarse())
            throw new IllegalStateException("El boletin ya fue enviado");
        return suscriptores.stream().filter(SuscripcionBoletin::estaActiva).map(s -> {
            Notificacion n = new Notificacion(s.getEmail(), TipoNotificacion.BOLETIN, boletin.getAsunto(),
                    boletin.getContenido() + "\n\nPara dejar de recibir el boletin: " + enlaces.cancelacion(s));
            n.asignarEnlaceAccion(enlaces.cancelacion(s));
            return n;
        }).toList();
    }
}
