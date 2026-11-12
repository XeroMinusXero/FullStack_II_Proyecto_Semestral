package com.thequietchapter;

import com.thequietchapter.model.*;
import com.thequietchapter.repository.CarritoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CarritoJpaIntegracionTests {
    @Autowired CarritoRepository repository;
    @Test void persisteCarritoAnonimoEItems() {
        Carrito carrito=new Carrito(null,"test-session-abcd");
        carrito.agregarLibro(new CondicionLibroCarrito(42L,new BigDecimal("15000"),3,true));
        Carrito guardado=repository.saveAndFlush(carrito);
        assertNotNull(guardado.getId());
        Carrito recuperado=repository.bloquearSesion("test-session-abcd").orElseThrow();
        assertEquals(1,recuperado.getItems().size());
        assertEquals(42L,recuperado.getItems().get(0).getLibroId());
    }
}
