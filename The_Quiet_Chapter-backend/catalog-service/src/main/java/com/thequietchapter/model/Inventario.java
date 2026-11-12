package com.thequietchapter.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** DC-01: unico dueno del stock; Libro NO tiene un campo stock. */
@Entity
@Table(name = "inventarios")
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    /** Composicion UML: los movimientos pertenecen al inventario. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id")
    @OrderBy("fecha ASC, id ASC")
    private List<MovimientoInventario> movimientos = new ArrayList<>();

    protected Inventario() {
    } // JPA

    public Inventario(int stockInicial) {
        if (stockInicial < 0)
            throw new IllegalArgumentException("Stock inicial negativo");
        this.stockActual = stockInicial;
    }

    public synchronized int consultarStock() {
        return stockActual;
    }

    public synchronized MovimientoInventario actualizarStock(int nuevoStock, Long responsableId) {
        return cambiar(nuevoStock, TipoMovimiento.ACTUALIZACION, "Actualizacion de stock", responsableId);
    }

    public synchronized MovimientoInventario registrarIngreso(int cantidad, Long responsableId) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("Ingreso debe ser positivo");
        return cambiar(Math.addExact(stockActual, cantidad), TipoMovimiento.INGRESO, "Ingreso de stock", responsableId);
    }

    public synchronized MovimientoInventario registrarAjuste(int nuevoStock, String motivo, Long responsableId) {
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El ajuste requiere motivo");
        return cambiar(nuevoStock, TipoMovimiento.AJUSTE, motivo, responsableId);
    }

    public synchronized MovimientoInventario descontarStock(int cantidad) {
        return descontarStock(cantidad, "Venta");
    }

    /**
     * Variante con motivo trazable (ej. referencia de pago) para la orquestacion de
     * compra.
     */
    public synchronized MovimientoInventario descontarStock(int cantidad, String motivo) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("Cantidad debe ser positiva");
        if (cantidad > stockActual)
            throw new IllegalStateException("Stock insuficiente");
        return cambiar(stockActual - cantidad, TipoMovimiento.VENTA, motivo, null);
    }

    public synchronized MovimientoInventario reponerStock(int cantidad) {
        return reponerStock(cantidad, "Reposicion por cancelacion");
    }

    public synchronized MovimientoInventario reponerStock(int cantidad, String motivo) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("Cantidad debe ser positiva");
        return cambiar(Math.addExact(stockActual, cantidad), TipoMovimiento.REPOSICION_CANCELACION, motivo, null);
    }

    public synchronized List<MovimientoInventario> consultarHistorial() {
        return List.copyOf(movimientos);
    }

    public synchronized boolean estaAgotado() {
        return stockActual == 0;
    }

    public Long getId() {
        return id;
    }

    private MovimientoInventario cambiar(int nuevoStock, TipoMovimiento tipo, String motivo, Long responsable) {
        if (nuevoStock < 0)
            throw new IllegalArgumentException("El stock no puede ser negativo");
        var movimiento = new MovimientoInventario(tipo, stockActual, nuevoStock, motivo, responsable);
        stockActual = nuevoStock;
        movimientos.add(movimiento);
        return movimiento;
    }
}
