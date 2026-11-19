import { clp } from '../../utils/formato'
import type { PrecioPromo } from '../../interfaces/promo'

/** HU-111/113: precio original tachado + precio con descuento cuando hay promocion vigente. */
export function Precio({ precio, promo, grande = false }: { precio: number; promo?: PrecioPromo; grande?: boolean }) {
  const clase = grande ? 'precio-grande' : 'precio'
  if (!promo) return <div className={clase}>{clp(precio)}</div>
  return (
    <div>
      <span className="precio-tachado">{clp(promo.precioOriginal)}</span>{' '}
      <span className="badge badge-oferta">-{promo.porcentaje}%</span>
      <div className={clase}>{clp(promo.precioFinal)}</div>
    </div>
  )
}
