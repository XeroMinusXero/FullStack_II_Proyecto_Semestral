package com.thequietchapter.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.thequietchapter.model.Usuario;

@Service
public class TokenService {
     private static final String EMISOR = "the-quiet-chapter-user-service";
     private final JwtEncoder encoder;
     private final JwtDecoder decoder;
     private final long horas;

     public TokenService(@Value("${tqc.auth.jwt-secret}") String secret, @Value("${tqc.auth.jwt-hours:2}") long horas) {
          byte[] key = secret.getBytes(StandardCharsets.UTF_8);
          if (key.length < 32)
               throw new IllegalArgumentException("TQC_JWT_SECRET debe tener al menos 32 bytes UTF-8");
          if (horas < 1 || horas > 24)
               throw new IllegalArgumentException("tqc.auth.jwt-hours debe estar entre 1 y 24");
          SecretKey hmac = new SecretKeySpec(key, "HmacSHA256");
          this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(hmac));
          this.decoder = NimbusJwtDecoder.withSecretKey(hmac).macAlgorithm(MacAlgorithm.HS256).build();
          this.horas = horas;
     }

     public String generar(Usuario usuario) {
          Instant ahora = Instant.now();
          JwtClaimsSet claims = JwtClaimsSet.builder().issuer(EMISOR).issuedAt(ahora)
                    .expiresAt(ahora.plus(Duration.ofHours(horas)))
                    .subject(usuario.getId().toString()).claim("rol", usuario.getRol().name()).build();
          return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                    .getTokenValue();
     }

     public Long idAutenticado(String authorization) {
          if (authorization == null || !authorization.startsWith("Bearer "))
               throw new org.springframework.web.server.ResponseStatusException(
                         org.springframework.http.HttpStatus.UNAUTHORIZED, "Falta token Bearer");
          try {
               Jwt jwt = decoder.decode(authorization.substring(7).trim());
               if (!EMISOR.equals(jwt.getClaimAsString("iss")))
                    throw new JwtException("Emisor incorrecto");
               long id = Long.parseLong(jwt.getSubject());
               if (id <= 0)
                    throw new JwtException("Sujeto incorrecto");
               return id;
          } catch (Exception e) {
               throw new org.springframework.web.server.ResponseStatusException(
                         org.springframework.http.HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
          }
     }

     public long duracionSegundos() {
          return horas * 3600;
     }
}
