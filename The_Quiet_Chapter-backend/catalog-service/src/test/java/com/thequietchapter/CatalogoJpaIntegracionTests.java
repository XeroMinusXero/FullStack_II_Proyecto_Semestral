package com.thequietchapter;

import com.thequietchapter.dto.LibroSolicitud;
import com.thequietchapter.dto.LineasStockSolicitud;
import com.thequietchapter.model.CriteriosCatalogo;
import com.thequietchapter.model.OrdenCatalogo;
import com.thequietchapter.service.CatalogoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogoJpaIntegracionTests {
    @Autowired CatalogoService service;

    private LibroSolicitud solicitud(String isbn, String titulo, String precio, int stock, String autor) {
        return new LibroSolicitud(isbn, titulo, List.of(autor), "Editorial Pruebas", "Novela", List.of("Ficcion"),
            "Sinopsis", "Libro fisico", 200, "Español", 2020, "Tapa blanda", "https://ejemplo.com/p.jpg", "",
            new BigDecimal(precio), stock);
    }

    @Test void registrarBuscarPaginarYStockPorLote() {
        String isbn = "TEST-" + UUID.randomUUID();
        var libro = service.registrar(solicitud(isbn, "Libro de integracion", "12000", 3, "Autora Integracion"));
        assertNotNull(libro.id());
        assertEquals(200, libro.paginas());
        var todos = new CriteriosCatalogo(null, null, null, null, true, false, null, null);
        var pagina = service.buscar("integracion", todos, OrdenCatalogo.PRECIO_ASC, 0, 10);
        assertTrue(pagina.getContent().stream().anyMatch(l -> l.id().equals(libro.id())));
        assertTrue(service.buscar("Autora Integ", todos, null, 0, 10).getTotalElements() >= 1);

        var lote = new LineasStockSolicitud("PAGO-1", List.of(new LineasStockSolicitud.Linea(libro.id(), 2)));
        service.descontarLote(lote);
        service.descontarLote(lote); // idempotente: no descuenta dos veces
        assertEquals(1, service.detalle(libro.id(), true).stock());
        assertThrows(IllegalStateException.class, () -> service.descontarLote(
            new LineasStockSolicitud("PAGO-2", List.of(new LineasStockSolicitud.Linea(libro.id(), 5)))));
        service.reponerLote(new LineasStockSolicitud("PAGO-1", lote.lineas()));
        assertEquals(3, service.detalle(libro.id(), true).stock());
        assertEquals(2, service.consultarMovimientos(libro.id()).size());
    }

    @Test void rangoDePrecioInvalidoYEliminarConPedidos() {
        var malos = new CriteriosCatalogo(null, null, new BigDecimal("100"), new BigDecimal("10"), true, false, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.buscar(null, malos, null, 0, 10));
        var libro = service.registrar(solicitud("TEST-" + UUID.randomUUID(), "Con pedidos", "1000", 1, "Autor X"));
        assertThrows(IllegalStateException.class, () -> service.eliminar(libro.id(), true));
        service.eliminar(libro.id(), false);
        assertThrows(java.util.NoSuchElementException.class, () -> service.detalle(libro.id(), true));
    }

    @Test void edicionConservaInventarioYValidaIsbn() {
        var libro = service.registrar(solicitud("TEST-" + UUID.randomUUID(), "Original", "1000", 5, "Autora Editar"));
        var otro = service.registrar(solicitud("TEST-" + UUID.randomUUID(), "Otro", "1000", 5, "Autora Editar"));
        service.actualizar(libro.id(), solicitud(libro.isbn(), "Editado", "2000", 99, "Autora Editar"));
        var editado = service.detalle(libro.id(), true);
        assertEquals("Editado", editado.titulo());
        assertEquals(5, editado.stock());
        assertThrows(IllegalStateException.class,
            () -> service.actualizar(libro.id(), solicitud(otro.isbn(), "X", "1000", 0, "A")));
    }
}
