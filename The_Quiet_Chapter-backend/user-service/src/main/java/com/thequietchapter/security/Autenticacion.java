package com.thequietchapter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * Valida el JWT HS256 emitido por user-service (mismo secreto TQC_JWT_SECRET en
 * todos los servicios)
 * y la clave interna usada SOLO en llamadas servicio-a-servicio (rutas
 * /internal, no expuestas por el gateway).
 * La identidad NUNCA se toma de cabeceras como X-TQC-Cliente-Id ni del cuerpo
 * de la solicitud.
 */
@Component
public class Autenticacion {
    public static final String CABECERA_INTERNA = "X-TQC-Internal-Key";
    private static final String EMISOR = "the-quiet-chapter-user-service";
    private final JwtDecoder decoder;
    private final byte[] claveInterna;

    public Autenticacion(@Value("${tqc.auth.jwt-secret}") String secreto,
            @Value("${tqc.internal.key}") String claveInterna) {
        byte[] key = secreto.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32)
            throw new IllegalArgumentException("TQC_JWT_SECRET debe tener al menos 32 bytes UTF-8");
        if (claveInterna == null || claveInterna.length() < 16)
            throw new IllegalArgumentException("TQC_INTERNAL_KEY debe tener al menos 16 caracteres");
        this.decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        this.claveInterna = claveInterna.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Usuario autenticado (cliente o administrador). 401 si falta o es invalido.
     */
    public Identidad requerida(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicie sesion");
        try {
            Jwt jwt = decoder.decode(authorization.substring(7).trim());
            if (!EMISOR.equals(jwt.getClaimAsString("iss")))
                throw new IllegalStateException("Emisor incorrecto");
            long id = Long.parseLong(jwt.getSubject());
            String rol = jwt.getClaimAsString("rol");
            if (id <= 0 || rol == null)
                throw new IllegalStateException("Token incompleto");
            return new Identidad(id, rol);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion invalida o expirada");
        }
    }

    /**
     * Identidad si viene un token; vacio para visitantes anonimos. Un token
     * invalido igual es 401.
     */
    public Optional<Identidad> opcional(String authorization) {
        if (authorization == null || authorization.isBlank())
            return Optional.empty();
        return Optional.of(requerida(authorization));
    }

    /** Solo ADMINISTRADOR. 401 sin sesion, 403 con rol insuficiente. */
    public Identidad admin(String authorization) {
        Identidad identidad = requerida(authorization);
        if (!identidad.esAdmin())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requiere rol ADMINISTRADOR");
        return identidad;
    }

    /** Llamadas internas entre microservicios. Comparacion en tiempo constante. */
    public void interna(String clave) {
        if (clave == null || !MessageDigest.isEqual(claveInterna, clave.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Llamada interna no autorizada");
    }
}
