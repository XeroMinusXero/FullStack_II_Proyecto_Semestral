package com.thequietchapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada UNICO para React (puerto 8090). Enruta /api/** a cada microservicio; /internal/** NO se publica. */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) { SpringApplication.run(ApiGatewayApplication.class, args); }
}
