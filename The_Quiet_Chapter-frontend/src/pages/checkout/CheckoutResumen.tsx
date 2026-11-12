import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { useCarrito } from '../../context/carrito'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { PasosCheckout } from './PasosCheckout'
import { borrarCheckoutId, useProcesoCompra } from './proceso'
import { clp } from '../../utils/formato'

/**
 * checkoutResumen.html -> HU-026/027/028: resumen final y pago. checkout-service orquesta stock, pedido,
 * cupon, promociones y notificacion; si algo falla compensa y devuelve el motivo.
 */
export default function CheckoutResumen() {
  const navegar = useNavigate()
  const { recargar } = useCarrito()
  const { proceso, error, ejecutar } = useProcesoCompra()
  const [pagando, setPagando] = useState(false)
  const [simularRechazo, setSimularRechazo] = useState(false)
  const [rechazo, setRechazo] = useState('')

  useEffect(() => {
    if (!proceso) return
    if (!proceso.medioPago) { navegar('/checkout/pago', { replace: true }); return }
    if (!proceso.resumen) ejecutar((id) => checkoutApi.resumen(id))
  }, [proceso, navegar, ejecutar])

  const pagar = async () => {
    setPagando(true); setRechazo('')
    const p = await ejecutar((id) => checkoutApi.pagar(id, simularRechazo))
    setPagando(false)
    if (!p) return
    if (p.estado === 'FINALIZADO' && p.numeroPedidoConfirmado) {
      borrarCheckoutId()
      await recargar()
      navegar(`/confirmacion?numero=${encodeURIComponent(p.numeroPedidoConfirmado)}`, { replace: true })
      return
    }
    const ultimo = p.intentosPago[p.intentosPago.length - 1]
    setRechazo(ultimo?.motivoRechazo ?? 'El pago fue rechazado. Puedes intentarlo nuevamente o cambiar el medio de pago.')
  }

  if (!proceso && !error) return <Cargando />
  const r = proceso?.resumen
  return (
    <>
      <h1>Finalizar compra</h1>
      <PasosCheckout actual={4} procesoId={proceso?.id} />
      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="error">{rechazo}</Alerta>
      {proceso && r && (
        <div className="row g-4">
          <div className="col-lg-7">
            <div className="seccion">
              <h2>Productos</h2>
              <div className="tabla-scroll">
                <table>
                  <thead><tr><th>Libro</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
                  <tbody>
                    {r.lineas.map((l) => <tr key={l.libroId}><td>{l.titulo}</td><td>{l.cantidad}</td><td>{clp(l.precioUnitario)}</td><td>{clp(l.subtotal)}</td></tr>)}
                  </tbody>
                </table>
              </div>
              <p className="mt-3 mb-1"><strong>Envío a:</strong> {proceso.direccion?.formateada}</p>
              <p className="mb-1"><strong>Método:</strong> {proceso.envio?.nombre} ({proceso.envio?.plazoEstimadoDias} días hábiles)</p>
              <p className="mb-0"><strong>Pago:</strong> {proceso.medioPago?.nombre}</p>
            </div>
          </div>
          <div className="col-lg-5">
            <div className="resumen-caja">
              <div className="fila"><span>Subtotal</span><span>{clp(r.subtotal)}</span></div>
              {r.descuento > 0 && <div className="fila"><span>Descuentos</span><span>−{clp(r.descuento)}</span></div>}
              <div className="fila"><span>Envío</span><span>{r.costoEnvio > 0 ? clp(r.costoEnvio) : 'Gratis'}</span></div>
              <div className="fila"><span>Total</span><span>{clp(r.total)}</span></div>
              <small className="text-muted">Incluye IVA (19%): {clp(r.ivaIncluido)}</small>
            </div>
            <div className="form-check mt-3">
              <input className="form-check-input" type="checkbox" id="simular" checked={simularRechazo} onChange={(e) => setSimularRechazo(e.target.checked)} />
              <label className="form-check-label" htmlFor="simular"><small>Simular pago rechazado (pruebas)</small></label>
            </div>
            <button type="button" className="btn w-100 mt-2" disabled={pagando || !proceso.puedePagar} onClick={pagar}>
              {pagando ? 'Procesando pago...' : `Pagar ${clp(r.total)}`}
            </button>
            <button type="button" className="btn btn-secundario w-100 mt-2" onClick={() => navegar('/checkout/pago')}>Cambiar medio de pago</button>
          </div>
        </div>
      )}
    </>
  )
}
