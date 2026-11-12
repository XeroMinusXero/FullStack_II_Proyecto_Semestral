package com.thequietchapter.service;

import com.thequietchapter.dto.CarritoRespuesta;
import com.thequietchapter.model.*;
import com.thequietchapter.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * Casos de uso DC-02. La identidad (clienteId) llega SIEMPRE desde un JWT
 * validado o desde una llamada interna.
 */
@Service
public class CarritoService {
    private final CarritoRepository repository;
    private final CatalogoHttpClient catalogo;
    private final PromocionesHttpClient promociones;

    public CarritoService(CarritoRepository repository, CatalogoHttpClient catalogo,
            PromocionesHttpClient promociones) {
        this.repository = repository;
        this.catalogo = catalogo;
        this.promociones = promociones;
    }

    private Carrito obtenerOCrear(Long clienteId, String sesion) {
        if (clienteId != null)
            return repository.bloquearCliente(clienteId)
                    .orElseGet(() -> repository.saveAndFlush(new Carrito(clienteId, null)));
        if (sesion == null || !sesion.matches("[a-fA-F0-9-]{36}"))
            throw new IllegalArgumentException("Sesion anonima invalida");
        return repository.bloquearSesion(sesion)
                .orElseGet(() -> repository.saveAndFlush(new Carrito(null, sesion)));
    }

    private Map<Long, CatalogoHttpClient.CondicionRemota> condiciones(Carrito carrito) {
        Map<Long, BigDecimal> anteriores = new LinkedHashMap<>();
        carrito.getItems().forEach(i -> anteriores.put(i.getLibroId(), i.getPrecioUnitario()));
        return catalogo.consultar(anteriores.keySet(), anteriores);
    }

    private static void anexar(ResultadoOperacionCarrito al, ResultadoOperacionCarrito otro) {
        otro.getAvisos().forEach(al::agregarAviso);
    }

    /**
     * Revisa precios/stock (HU-020) con UNA llamada a catalogo y arma la respuesta
     * para React.
     */
    private CarritoRespuesta respuesta(Carrito carrito, ResultadoOperacionCarrito original) {
        Map<Long, CatalogoHttpClient.CondicionRemota> datos = condiciones(carrito);
        ResultadoOperacionCarrito chequeo = carrito.revisar(datos.values().stream()
                .map(CatalogoHttpClient.CondicionRemota::comoDominio).toList());
        anexar(original, chequeo);
        List<CarritoRespuesta.ItemRespuesta> items = carrito.getItems().stream().map(item -> {
            var info = datos.get(item.getLibroId());
            return new CarritoRespuesta.ItemRespuesta(item.getLibroId(), info.titulo(), info.portadaUrl(),
                    item.getCantidad(), item.getPrecioUnitario(), item.calcularSubtotal(), item.isDisponible(),
                    info.stock());
        }).toList();
        return new CarritoRespuesta(carrito.getId(), carrito.getClienteId(), carrito.isActivo(), items,
                carrito.contarUnidades(), carrito.calcularTotal(), carrito.getCuponAplicadoId(),
                carrito.getCodigoCuponAplicado(),
                carrito.getDescuentoCupon(), carrito.calcularTotalConCupon(),
                carrito.puedeComprar() && !original.hayBloqueos(), original.isExitosa(),
                original.getAvisos().stream().map(CarritoRespuesta.AvisoRespuesta::desde).toList());
    }

    @Transactional
    public CarritoRespuesta consultar(Long clienteId, String sesion) {
        return respuesta(obtenerOCrear(clienteId, sesion), new ResultadoOperacionCarrito());
    }

    /**
     * HU-013: agrega o incrementa sin duplicar el item; respeta el stock vigente.
     */
    @Transactional
    public CarritoRespuesta agregar(Long clienteId, String sesion, Long libroId, int cantidad) {
        if (cantidad <= 0 || cantidad > 999)
            throw new IllegalArgumentException("Cantidad fuera de rango");
        var condicion = catalogo.consultarExistente(libroId).comoDominio();
        Carrito carrito = obtenerOCrear(clienteId, sesion);
        ResultadoOperacionCarrito resultado = new ResultadoOperacionCarrito();
        ItemCarrito existente = carrito.getItems().stream().filter(i -> i.getLibroId().equals(libroId)).findFirst()
                .orElse(null);
        int solicitada = existente == null ? cantidad : Math.addExact(existente.getCantidad(), cantidad);
        if (!condicion.isActivo() || condicion.getStockDisponible() == 0)
            throw new IllegalStateException("El libro no esta disponible");
        if (!condicion.permiteCantidad(solicitada))
            throw new IllegalStateException("Solo quedan " + condicion.getStockDisponible() + " unidades disponibles");
        if (existente == null) {
            anexar(resultado, carrito.agregarLibro(condicion));
            if (!resultado.hayBloqueos() && cantidad > 1)
                anexar(resultado, carrito.actualizarCantidad(libroId, cantidad, condicion));
        } else
            anexar(resultado, carrito.actualizarCantidad(libroId, solicitada, condicion));
        return respuesta(carrito, resultado);
    }

    /**
     * HU-015: si supera el stock, se fija al maximo disponible y se informa (aviso
     * CANTIDAD_AJUSTADA).
     */
    @Transactional
    public CarritoRespuesta actualizar(Long clienteId, String sesion, Long libroId, int cantidad) {
        var condicion = catalogo.consultarExistente(libroId).comoDominio();
        Carrito carrito = obtenerOCrear(clienteId, sesion);
        ResultadoOperacionCarrito r = new ResultadoOperacionCarrito();
        int permitida = cantidad;
        if (condicion.isActivo() && condicion.getStockDisponible() > 0 && cantidad > condicion.getStockDisponible()) {
            permitida = condicion.getStockDisponible();
            r.agregarAviso(new AvisoCarrito(libroId, TipoAvisoCarrito.CANTIDAD_AJUSTADA,
                    "Solo hay " + permitida + " unidades disponibles; se ajusto la cantidad"));
        }
        anexar(r, carrito.actualizarCantidad(libroId, permitida, condicion));
        return respuesta(carrito, r);
    }

    @Transactional
    public CarritoRespuesta eliminar(Long clienteId, String sesion, Long libroId) {
        Carrito carrito = obtenerOCrear(clienteId, sesion);
        if (!carrito.eliminarLibro(libroId))
            throw new NoSuchElementException("El libro no esta en el carrito");
        return respuesta(carrito, new ResultadoOperacionCarrito());
    }

    @Transactional
    public CarritoRespuesta vaciar(Long clienteId, String sesion) {
        Carrito carrito = obtenerOCrear(clienteId, sesion);
        carrito.vaciar();
        return respuesta(carrito, new ResultadoOperacionCarrito());
    }

    /**
     * HU-019: fusion del carrito anonimo de ESTA sesion con el carrito del cliente
     * autenticado (id del token).
     */
    @Transactional
    public CarritoRespuesta fusionar(String sesion, Long clienteId) {
        Carrito destino = obtenerOCrear(clienteId, null);
        var origenOpt = sesion == null ? Optional.<Carrito>empty() : repository.bloquearSesion(sesion);
        if (origenOpt.isEmpty() || origenOpt.get().getItems().isEmpty()) {
            origenOpt.ifPresent(Carrito::desactivarTrasFusion);
            return respuesta(destino, new ResultadoOperacionCarrito()); // carrito anonimo vacio: no se altera nada
        }
        Carrito origen = origenOpt.get();
        Map<Long, BigDecimal> anteriores = new LinkedHashMap<>();
        origen.getItems().forEach(i -> anteriores.put(i.getLibroId(), i.getPrecioUnitario()));
        var datos = catalogo.consultar(anteriores.keySet(), anteriores);
        ResultadoOperacionCarrito r = destino.fusionarDesde(origen,
                datos.values().stream().map(CatalogoHttpClient.CondicionRemota::comoDominio).toList());
        origen.desactivarTrasFusion();
        repository.flush();
        return respuesta(destino, r);
    }

    /**
     * HU-100 (aplicar cupon): promo-service valida; el carrito solo guarda el
     * descuento ya validado.
     */
    @Transactional
    public CarritoRespuesta aplicarCupon(Long clienteId, String codigo) {
        Carrito carrito = obtenerOCrear(clienteId, null);
        if (carrito.getItems().isEmpty())
            throw new IllegalStateException("Agrega libros antes de aplicar un cupon");
        respuesta(carrito, new ResultadoOperacionCarrito()); // sincroniza precios antes de validar
        var valido = promociones.validarCupon(codigo.strip(), carrito.calcularTotal());
        if (!carrito.aplicarCuponValidado(valido.cuponId(), valido.codigo(), valido.descuento()))
            throw new IllegalStateException("No se pudo aplicar el cupon");
        return respuesta(carrito, new ResultadoOperacionCarrito());
    }

    @Transactional
    public CarritoRespuesta quitarCupon(Long clienteId) {
        Carrito carrito = obtenerOCrear(clienteId, null);
        carrito.quitarCupon();
        return respuesta(carrito, new ResultadoOperacionCarrito());
    }

    // ------------------------------------------------ interno (checkout-service)
    @Transactional
    public CarritoRespuesta consultarCliente(Long clienteId) {
        return consultar(clienteId, null);
    }

    /**
     * HU-028: tras pago aprobado se vacia el carrito del cliente (solo si es el
     * mismo carrito del checkout).
     */
    @Transactional
    public void vaciarCliente(Long clienteId, Long carritoId) {
        Carrito carrito = obtenerOCrear(clienteId, null);
        if (!carrito.getId().equals(carritoId))
            throw new IllegalStateException("El carrito no corresponde al checkout");
        carrito.vaciar();
        repository.flush();
    }
}
