package com.thequietchapter.config;

import com.thequietchapter.model.MedioPago;
import com.thequietchapter.model.MetodoEnvio;
import com.thequietchapter.repository.MedioPagoRepository;
import com.thequietchapter.repository.MetodoEnvioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

/**
 * Opciones por defecto SOLO si las tablas estan vacias (reemplaza
 * OPCIONES_DESARROLLO.sql). Luego se administran por API.
 */
@Configuration
public class OpcionesIniciales {
    @Bean
    CommandLineRunner opcionesCheckout(MetodoEnvioRepository envios, MedioPagoRepository medios) {
        return args -> {
            if (envios.count() == 0)
                envios.saveAll(List.of(
                        new MetodoEnvio("Despacho estandar", new BigDecimal("3990"), 5, true, List.of()),
                        new MetodoEnvio("Despacho express", new BigDecimal("6990"), 2, true,
                                List.of("Metropolitana", "Region Metropolitana")),
                        new MetodoEnvio("Retiro en tienda", BigDecimal.ZERO, 1, true, List.of())));
            if (medios.count() == 0)
                medios.saveAll(List.of(
                        new MedioPago("Tarjeta de credito (simulado)", true),
                        new MedioPago("Tarjeta de debito (simulado)", true),
                        new MedioPago("Transferencia bancaria (simulado)", true)));
        };
    }
}
