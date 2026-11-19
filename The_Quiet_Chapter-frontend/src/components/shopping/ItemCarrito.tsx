import { Link } from 'react-router-dom'
import { Portada } from './Portada'
import { ControlCantidad } from './ControlCantidad'
import { clp } from '../../utils/formato'
import type { ItemCarrito as Item } from '../../interfaces/carrito'
import estilos from './ItemCarrito.module.css'

/** HU-014..016: un item con portada, precio unitario, cantidad, subtotal y eliminar. */
export function ItemCarrito({ item, ocupado, onCantidad, onEliminar }: {
  item: Item
  ocupado: boolean
  onCantidad: (cantidad: number) => void
  onEliminar: () => void
}) {
  return (
    <div className="item-carrito" style={item.disponible ? undefined : { opacity: 0.75 }}>
      <Link to={`/libros/${item.libroId}`}><Portada url={item.portadaUrl} titulo={item.titulo} variante="mini" id={item.libroId} /></Link>
      <div>
        <Link to={`/libros/${item.libroId}`} className="titulo fw-bold">{item.titulo}</Link>
        <div className={`precio ${estilos.precio}`}>{clp(item.precioUnitario)}</div>
        {!item.disponible && <span className="badge badge-agotado">No disponible: ajusta o elimina</span>}
        {item.disponible && item.stockDisponible <= 3 && <small className="d-block text-muted">Quedan {item.stockDisponible}</small>}
      </div>
      <div className="acciones-item">
        <ControlCantidad valor={item.cantidad} maximo={Math.max(item.stockDisponible, 1)} deshabilitado={ocupado}
          onCambiar={(v) => (v <= 0 ? onEliminar() : onCantidad(v))} />
        <div className="mt-2 fw-bold">Subtotal: {clp(item.subtotal)}</div>
        <button type="button" className="btn btn-peligro mt-2" disabled={ocupado} onClick={onEliminar}>Eliminar</button>
      </div>
    </div>
  )
}
