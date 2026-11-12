import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { pedidosApi } from '../api/pedidos'
import { mensajeDe } from '../api/http'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import { clp, fecha } from '../utils/formato'
import type { Pedido } from '../interfaces/pedido'

/** confirmacion.html -> HU-029: numero de pedido, detalle y aviso de correo enviado. */
export default function Confirmacion() {
  const numero = useSearchParams()[0].get('numero') ?? ''
  const [pedido, setPedido] = useState<Pedido | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!numero) return
    pedidosApi.miPedidoPorNumero(numero).then(setPedido).catch((e) => setError(mensajeDe(e)))
  }, [numero])

  if (!numero) return <div className="vacio"><p>No hay una compra reciente para mostrar.</p><Link to="/" className="btn">Ir al inicio</Link></div>
  return (
    <>
      <section className="hero">
        <h1>¡Gracias por tu compra!</h1>
        <p>Tu pedido <strong>{numero}</strong> fue confirmado. Te enviamos el detalle a tu correo.</p>
      </section>
      <Alerta tipo="error">{error}</Alerta>
      {!pedido && !error && <Cargando />}
      {pedido && (
        <div className="seccion" style={{ maxWidth: 760, margin: '0 auto' }}>
          <h2>Detalle del pedido</h2>
          <p className="mb-1"><strong>Fecha:</strong> {fecha(pedido.fechaCreacion, true)}</p>
          <p className="mb-1"><strong>Entrega:</strong> {pedido.entrega.direccionFormateada} · {pedido.entrega.metodoEnvio} ({pedido.entrega.plazoEstimadoDias} días hábiles)</p>
          <p><strong>Pago:</strong> {pedido.medioPago} · Ref. {pedido.referenciaPago}</p>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Libro</th><th>Cant.</th><th>Subtotal</th></tr></thead>
              <tbody>{pedido.detalles.map((d) => <tr key={d.id}><td>{d.tituloLibro}</td><td>{d.cantidad}</td><td>{clp(d.subtotal)}</td></tr>)}</tbody>
            </table>
          </div>
          <div className="resumen-caja mt-3">
            <div className="fila"><span>Subtotal</span><span>{clp(pedido.importes.subtotal)}</span></div>
            {pedido.importes.descuento > 0 && <div className="fila"><span>Descuentos</span><span>−{clp(pedido.importes.descuento)}</span></div>}
            <div className="fila"><span>Envío</span><span>{clp(pedido.importes.costoEnvio)}</span></div>
            <div className="fila"><span>Total</span><span>{clp(pedido.importes.total)}</span></div>
          </div>
          <div className="d-flex flex-wrap gap-2 mt-3">
            <Link to={`/cuenta/pedidos/${pedido.id}`} className="btn">Ver seguimiento</Link>
            <Link to="/catalogo" className="btn btn-secundario">Seguir comprando</Link>
          </div>
        </div>
      )}
    </>
  )
}
