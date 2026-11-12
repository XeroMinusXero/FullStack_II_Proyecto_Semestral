package com.thequietchapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS centralizado en el gateway: los microservicios no necesitan configurarlo. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] origenes;
    public CorsConfig(@Value("${tqc.cors.origins}") String[] origenes) { this.origenes = origenes; }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origenes)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*").allowCredentials(true).maxAge(3600);
    }
}
