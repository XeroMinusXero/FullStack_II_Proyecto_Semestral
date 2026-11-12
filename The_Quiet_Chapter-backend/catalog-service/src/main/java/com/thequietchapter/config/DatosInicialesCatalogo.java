package com.thequietchapter.config;

import com.thequietchapter.dto.LibroSolicitud;
import com.thequietchapter.model.Libro;
import com.thequietchapter.repository.LibroRepository;
import com.thequietchapter.service.CatalogoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.List;

/**
 * Datos demo idempotentes POR ISBN: agrega los que falten y completa la portada
 * si estaba vacia.
 * Las portadas apuntan a /img/... (carpeta public/img del frontend React).
 * Desactivar con TQC_DEMO_SEED=false.
 */
@Configuration
public class DatosInicialesCatalogo {
    private record Demo(String isbn, String titulo, String precio, int stock, List<String> autores, String editorial,
            String categoria, List<String> generos, String sinopsis, String formato, int paginas,
            int anio, String encuadernacion, String portada) {
    }

    private static final List<Demo> DEMOS = List.of(
            new Demo("9780307474728", "Cien años de soledad", "18900", 10, List.of("Gabriel García Márquez"),
                    "Sudamericana",
                    "Novela", List.of("Realismo mágico"),
                    "La historia de siete generaciones de la familia Buendía en Macondo.",
                    "Libro físico", 496, 1967, "Tapa blanda", "/img/cien-anos.jpg"),
            new Demo("9780451524935", "1984", "12900", 8, List.of("George Orwell"), "Debolsillo",
                    "Novela", List.of("Distopía"),
                    "Una sociedad marcada por la vigilancia y el control del Gran Hermano.",
                    "Libro físico", 352, 1949, "Tapa blanda", "/img/1984.jpg"),
            new Demo("9780156012195", "El Principito", "9900", 15, List.of("Antoine de Saint-Exupéry"), "Salamandra",
                    "Infantil", List.of("Fábula"),
                    "Un pequeño príncipe viaja por planetas e invita a mirar con el corazón.",
                    "Libro físico", 96, 1943, "Tapa dura", "/img/principito.jpg"),
            new Demo("9788437604572", "Rayuela", "16500", 6, List.of("Julio Cortázar"), "Cátedra",
                    "Novela", List.of("Experimental"),
                    "Una novela que puede leerse en más de un orden, entre París y Buenos Aires.",
                    "Libro físico", 736, 1963, "Tapa blanda", "/img/rayuela.jpg"),
            new Demo("9788408163435", "La sombra del viento", "17900", 0, List.of("Carlos Ruiz Zafón"), "Planeta",
                    "Novela", List.of("Misterio"),
                    "Daniel descubre en el Cementerio de los Libros Olvidados un libro que cambiará su vida.",
                    "Libro físico", 576, 2001, "Tapa blanda", "/img/sombra-viento.jpg"));

    @Bean
    @ConditionalOnProperty(name = "tqc.demo.seed", havingValue = "true", matchIfMissing = true)
    CommandLineRunner datosDemo(CatalogoService service, LibroRepository repo, TransactionTemplate tx) {
        return args -> DEMOS.forEach(d -> tx.executeWithoutResult(status -> {
            var existente = repo.findByIsbnIgnoreCase(d.isbn());
            if (existente.isEmpty()) {
                service.registrar(new LibroSolicitud(d.isbn(), d.titulo(), d.autores(), d.editorial(), d.categoria(),
                        d.generos(), d.sinopsis(), d.formato(), d.paginas(), "Español", d.anio(), d.encuadernacion(),
                        d.portada(), "", new BigDecimal(d.precio()), d.stock()));
            } else {
                Libro l = existente.get();
                if (l.getFicha().getPortadaUrl().isBlank()) {
                    l.getFicha().actualizarPortada(d.portada());
                    repo.save(l);
                }
            }
        }));
    }
}
