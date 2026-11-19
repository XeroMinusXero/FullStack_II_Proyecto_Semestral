import { clp } from '../../utils/formato'

/** Montos que calcula checkout-service (y que order-service guarda en el pedido). */
export interface Importes {
  subtotal: number
  descuento: number
  costoEnvio: number
  total: number
  ivaIncluido?: number
}

/** Caja de totales de una compra: se usa en el resumen del checkout, la confirmacion y el detalle del pedido. */
export function ResumenImportes({ importes: i, className = '' }: { importes: Importes; className?: string }) {
  return (
    <div className={`resumen-caja ${className}`}>
      <div className="fila"><span>Subtotal</span><span>{clp(i.subtotal)}</span></div>
      {i.descuento > 0 && <div className="fila"><span>Descuentos</span><span>−{clp(i.descuento)}</span></div>}
      <div className="fila"><span>Envío</span><span>{i.costoEnvio > 0 ? clp(i.costoEnvio) : 'Gratis'}</span></div>
      <div className="fila"><span>Total</span><span>{clp(i.total)}</span></div>
      {i.ivaIncluido !== undefined && <small className="text-muted">Incluye IVA (19%): {clp(i.ivaIncluido)}</small>}
    </div>
  )
}
