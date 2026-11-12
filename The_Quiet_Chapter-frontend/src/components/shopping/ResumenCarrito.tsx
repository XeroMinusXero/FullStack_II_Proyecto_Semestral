import { clp } from '../../utils/formato'
import type { Carrito } from '../../interfaces/carrito'

/** HU-102: muestra codigo y descuento solo si hay cupon aplicado. */
export function ResumenCarrito({ carrito }: { carrito: Carrito }) {
  return (
    <div className="resumen-caja">
      <div className="fila"><span>Subtotal ({carrito.unidades} {carrito.unidades === 1 ? 'libro' : 'libros'})</span><span>{clp(carrito.subtotal)}</span></div>
      {carrito.codigoCuponAplicado && (
        <div className="fila"><span>Cupón {carrito.codigoCuponAplicado}</span><span>−{clp(carrito.descuentoCupon)}</span></div>
      )}
      <div className="fila"><span>Envío</span><span>Se calcula en el checkout</span></div>
      <div className="fila"><span>Total</span><span>{clp(carrito.total)}</span></div>
      <small className="text-muted">Precios con IVA incluido.</small>
    </div>
  )
}
