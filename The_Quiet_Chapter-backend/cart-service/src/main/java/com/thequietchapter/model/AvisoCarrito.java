package com.thequietchapter.model;

/** Objeto de resultado del dominio: no es entidad JPA. */
public class AvisoCarrito {
    private final Long libroId;
    private final TipoAvisoCarrito tipo;
    private final String mensaje;

    public AvisoCarrito(Long libroId, TipoAvisoCarrito tipo, String mensaje) {
        this.libroId = libroId;
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    public boolean esBloqueante() {
        return tipo == TipoAvisoCarrito.PRODUCTO_AGOTADO
                || tipo == TipoAvisoCarrito.STOCK_INSUFICIENTE
                || tipo == TipoAvisoCarrito.PRODUCTO_INACTIVO
                || tipo == TipoAvisoCarrito.CARRITO_INACTIVO;
    }

    public Long getLibroId() {
        return libroId;
    }

    public TipoAvisoCarrito getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
