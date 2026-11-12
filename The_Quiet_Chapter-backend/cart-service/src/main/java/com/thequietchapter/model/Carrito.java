package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Entidad raiz DC-02: tiene comportamientos y conserva sus invariantes. */
@Entity
@Table(name = "carritos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_carrito_cliente", columnNames = "cliente_id"),
        @UniqueConstraint(name = "uk_carrito_sesion", columnNames = "identificador_sesion")
})
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id")
    private Long clienteId;
    @Column(name = "identificador_sesion", length = 80)
    private String identificadorSesion;
    @Column(nullable = false)
    private boolean activo;
    @Version
    private Long version;
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemCarrito> items = new ArrayList<>();
    @Column(name = "cupon_aplicado_id")
    private Long cuponAplicadoId;
    @Column(name = "codigo_cupon_aplicado", length = 100)
    private String codigoCuponAplicado;
    @Column(name = "descuento_cupon", nullable = false, precision = 13, scale = 2)
    private BigDecimal descuentoCupon = BigDecimal.ZERO;

    protected Carrito() {
    }

    public Carrito(Long clienteId, String identificadorSesion) {
        if ((clienteId == null) == (identificadorSesion == null || identificadorSesion.isBlank()))
            throw new IllegalArgumentException("Carrito requiere cliente O sesion anonima");
        if (clienteId != null && clienteId <= 0)
            throw new IllegalArgumentException("Cliente invalido");
        this.clienteId = clienteId;
        this.identificadorSesion = identificadorSesion;
        this.activo = true;
    }

    private static AvisoCarrito aviso(Long id, TipoAvisoCarrito tipo, String msg) {
        return new AvisoCarrito(id, tipo, msg);
    }

    private ItemCarrito buscar(Long libroId) {
        return items.stream().filter(i -> i.getLibroId().equals(libroId)).findFirst().orElse(null);
    }

    private ResultadoOperacionCarrito comprobarActivo() {
        ResultadoOperacionCarrito r = new ResultadoOperacionCarrito();
        if (!activo)
            r.agregarAviso(aviso(null, TipoAvisoCarrito.CARRITO_INACTIVO, "El carrito ya no esta activo"));
        return r;
    }

    public ResultadoOperacionCarrito agregarLibro(CondicionLibroCarrito condicion) {
        Objects.requireNonNull(condicion, "Condicion requerida");
        ResultadoOperacionCarrito r = comprobarActivo();
        if (r.hayBloqueos())
            return r;
        Long libroId = condicion.getLibroId();
        ItemCarrito item = buscar(libroId);
        int solicitada = item == null ? 1 : item.getCantidad() + 1;
        if (!condicion.isActivo())
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_INACTIVO, "Libro inactivo"));
        else if (condicion.getStockDisponible() == 0)
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_AGOTADO, "Libro sin existencias"));
        else if (!condicion.permiteCantidad(solicitada))
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.STOCK_INSUFICIENTE,
                    "Stock insuficiente para la cantidad solicitada"));
        if (r.hayBloqueos())
            return r;
        if (item == null) {
            ItemCarrito nuevo = new ItemCarrito(libroId, condicion.getPrecioVigente());
            nuevo.vincularCarrito(this);
            items.add(nuevo);
        } else {
            if (item.sincronizarPrecio(condicion.getPrecioVigente()))
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRECIO_CAMBIADO, "Se actualizo el precio"));
            item.ajustarCantidad(solicitada, condicion.getStockDisponible());
        }
        quitarCupon(); // Cualquier cambio de articulos obliga a revalidar promociones.
        return r;
    }

    public ResultadoOperacionCarrito actualizarCantidad(Long libroId, int nuevaCantidad,
            CondicionLibroCarrito condicion) {
        ResultadoOperacionCarrito r = comprobarActivo();
        if (r.hayBloqueos())
            return r;
        ItemCarrito item = buscar(libroId);
        if (item == null)
            throw new NoSuchElementException("El libro no esta en el carrito");
        if (nuevaCantidad <= 0)
            throw new IllegalArgumentException("Usar eliminar para quitar un libro del carrito");
        if (condicion == null || !libroId.equals(condicion.getLibroId()))
            throw new IllegalArgumentException("Condicion incorrecta");
        if (!condicion.isActivo())
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_INACTIVO, "Libro inactivo"));
        else if (condicion.getStockDisponible() == 0)
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_AGOTADO, "Libro agotado"));
        else if (!condicion.permiteCantidad(nuevaCantidad))
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.STOCK_INSUFICIENTE, "Stock insuficiente"));
        if (r.hayBloqueos())
            return r;
        if (item.sincronizarPrecio(condicion.getPrecioVigente()))
            r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRECIO_CAMBIADO, "Se actualizo el precio"));
        item.ajustarCantidad(nuevaCantidad, condicion.getStockDisponible());
        quitarCupon();
        return r;
    }

    public boolean eliminarLibro(Long libroId) {
        if (!activo)
            return false;
        boolean eliminado = items.removeIf(i -> i.getLibroId().equals(libroId));
        if (eliminado)
            quitarCupon();
        return eliminado;
    }

    public void vaciar() {
        if (!activo)
            throw new IllegalStateException("Carrito inactivo");
        items.clear();
        quitarCupon();
    }

    public ResultadoOperacionCarrito fusionarDesde(Carrito temporal, List<CondicionLibroCarrito> condiciones) {
        ResultadoOperacionCarrito r = comprobarActivo();
        if (r.hayBloqueos())
            return r;
        if (temporal == null || temporal == this || temporal.clienteId != null || !temporal.activo)
            throw new IllegalArgumentException("Carrito temporal invalido");
        Map<Long, CondicionLibroCarrito> mapa = new HashMap<>();
        for (CondicionLibroCarrito c : condiciones)
            mapa.put(c.getLibroId(), c);
        for (ItemCarrito origen : temporal.items) {
            Long libroId = origen.getLibroId();
            CondicionLibroCarrito c = mapa.get(libroId);
            if (c == null || !c.isActivo()) {
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_INACTIVO, "Libro no disponible para fusion"));
                continue;
            }
            if (c.getStockDisponible() <= 0) {
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_AGOTADO, "Sin stock al fusionar"));
                continue;
            }
            ItemCarrito destino = buscar(libroId);
            int ya = destino == null ? 0 : destino.getCantidad();
            int total = ya + origen.getCantidad();
            int permitido = Math.min(total, c.getStockDisponible());
            if (permitido < total)
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.CANTIDAD_AJUSTADA, "Fusion ajustada al stock actual"));
            if (destino == null) {
                destino = new ItemCarrito(libroId, c.getPrecioVigente());
                destino.vincularCarrito(this);
                items.add(destino);
            } else if (destino.sincronizarPrecio(c.getPrecioVigente()))
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRECIO_CAMBIADO, "Precio actualizado en fusion"));
            destino.ajustarCantidad(permitido, c.getStockDisponible());
        }
        quitarCupon(); // Cupon anterior requiere revalidacion tras una fusion.
        return r;
    }

    public ResultadoOperacionCarrito revisar(List<CondicionLibroCarrito> condiciones) {
        ResultadoOperacionCarrito r = comprobarActivo();
        if (r.hayBloqueos())
            return r;
        Map<Long, CondicionLibroCarrito> mapa = new HashMap<>();
        for (CondicionLibroCarrito c : condiciones)
            mapa.put(c.getLibroId(), c);
        for (ItemCarrito item : items) {
            Long libroId = item.getLibroId();
            CondicionLibroCarrito c = mapa.get(libroId);
            if (c == null || !c.isActivo()) {
                item.verificarDisponibilidad(0, false);
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_INACTIVO, "Libro inactivo o inexistente"));
                continue;
            }
            if (c.getStockDisponible() == 0) {
                item.verificarDisponibilidad(0, true);
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRODUCTO_AGOTADO, "Libro agotado"));
            } else if (!item.verificarDisponibilidad(c.getStockDisponible(), true))
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.STOCK_INSUFICIENTE, "Cantidad supera stock actual"));
            if (c.getPrecioVigente().signum() > 0 && item.sincronizarPrecio(c.getPrecioVigente()))
                r.agregarAviso(aviso(libroId, TipoAvisoCarrito.PRECIO_CAMBIADO, "Se detecto nuevo precio"));
        }
        if (r.tieneAvisos())
            quitarCupon(); // Descuento queda pendiente de revalidacion.
        return r;
    }

    public BigDecimal calcularTotal() {
        return items.stream().map(ItemCarrito::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2,
                RoundingMode.HALF_UP);
    }

    public int contarUnidades() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public boolean puedeComprar() {
        return activo && clienteId != null && !items.isEmpty() && items.stream().allMatch(ItemCarrito::isDisponible);
    }

    /**
     * Solo llamar cuando Promotion Service haya VALIDADO el cupon. NO exponer al
     * cliente como descuento libre.
     */
    public boolean aplicarCuponValidado(Long cuponId, String codigo, BigDecimal descuento) {
        if (!activo || cuponId == null || cuponId <= 0 || codigo == null || codigo.isBlank() || descuento == null
                || descuento.signum() < 0)
            return false;
        cuponAplicadoId = cuponId;
        codigoCuponAplicado = codigo;
        descuentoCupon = descuento;
        return true;
    }

    public boolean quitarCupon() {
        boolean habia = cuponAplicadoId != null;
        cuponAplicadoId = null;
        codigoCuponAplicado = null;
        descuentoCupon = BigDecimal.ZERO;
        return habia;
    }

    public BigDecimal calcularTotalConCupon() {
        return calcularTotal().subtract(descuentoCupon).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public void desactivarTrasFusion() { // Operacion interna: no se permite reusar ni duplicar una fusion de sesion.
        if (clienteId != null)
            throw new IllegalStateException("Solo temporales se fusionan");
        activo = false;
        identificadorSesion = null;
        items.clear();
        quitarCupon();
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getIdentificadorSesion() {
        return identificadorSesion;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<ItemCarrito> getItems() {
        return List.copyOf(items);
    }

    public Long getCuponAplicadoId() {
        return cuponAplicadoId;
    }

    public String getCodigoCuponAplicado() {
        return codigoCuponAplicado;
    }

    public BigDecimal getDescuentoCupon() {
        return descuentoCupon;
    }
}
