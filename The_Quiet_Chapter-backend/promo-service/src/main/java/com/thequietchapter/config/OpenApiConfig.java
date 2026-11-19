package com.thequietchapter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentacion OpenAPI (Swagger) de promo-service.
 * <ul>
 *   <li>Swagger UI de este servicio: http://127.0.0.1:8086/swagger-ui.html</li>
 *   <li>Swagger UI centralizado (todos los servicios): http://localhost:8090/swagger-ui.html</li>
 * </ul>
 * Las rutas /internal/** no se documentan (springdoc.paths-to-exclude): son solo servicio-a-servicio.
 */
@Configuration
public class OpenApiConfig {

    /** Nombre del esquema de seguridad que muestra el boton "Authorize". */
    static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Quiet Chapter - Cupones, promociones y recomendaciones")
                        .version("1.0")
                        .description("Cupones de descuento, promociones por libro/categoría/género/editorial, precios con promoción y motor de recomendaciones."))
                // "/" = mismo origen: desde el gateway (8090) las pruebas pasan por el gateway;
                // desde la UI propia del servicio, van directo al servicio.
                .servers(List.of(new Server().url("/").description("Origen actual (gateway 8090 o promo-service 8086)")))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token de POST /api/usuarios/login (campo accessToken). Pega solo el token, sin la palabra Bearer.")))
                .tags(List.of(
                        new Tag().name("Cupones").description("Cupones de descuento (administrador)"),
                        new Tag().name("Promociones").description("Promociones vigentes y su administración"),
                        new Tag().name("Recomendaciones").description("Recomendaciones del inicio, por perfil, compras y libro")));
    }

    /**
     * Los controladores leen la cabecera Authorization a mano (@RequestHeader) y la validan con Autenticacion.
     * Para Swagger se quita ese parametro (y la cookie del carrito, que el navegador envia solo) y se reemplaza
     * por el esquema JWT: asi el token se ingresa una vez en "Authorize" y viaja en todas las pruebas.
     */
    @Bean
    public OperationCustomizer autenticacionJwtEnSwagger() {
        return (operacion, metodo) -> {
            if (operacion.getParameters() == null) {
                return operacion;
            }
            List<Parameter> parametros = new ArrayList<>(operacion.getParameters());
            boolean usaToken = parametros.removeIf(p -> "header".equals(p.getIn())
                    && HttpHeaders.AUTHORIZATION.equalsIgnoreCase(p.getName()));
            parametros.removeIf(p -> "cookie".equals(p.getIn()));
            operacion.setParameters(parametros.isEmpty() ? null : parametros);
            if (usaToken) {
                operacion.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
            }
            return operacion;
        };
    }
}
