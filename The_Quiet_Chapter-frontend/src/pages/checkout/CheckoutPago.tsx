import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { PasosCheckout } from './PasosCheckout'
import { useProcesoCompra } from './proceso'
import type { MedioPago } from '../../interfaces/checkout'

/** checkoutPago.html -> HU-025: medio de pago (pasarela simulada, no se piden datos de tarjeta). */
export default function CheckoutPago() {
  const navegar = useNavigate()
  const { proceso, error, ejecutar } = useProcesoCompra()
  const [medios, setMedios] = useState<MedioPago[] | null>(null)
  const [elegido, setElegido] = useState<number | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!proceso) return
    if (!proceso.envio) { navegar('/checkout/envio', { replace: true }); return }
    checkoutApi.medios().then((m) => { setMedios(m); setElegido(proceso.medioPago?.id ?? m[0]?.id ?? null) }).catch(() => setMedios([]))
  }, [proceso, navegar])

  const continuar = async () => {
    if (!elegido) return
    setEnviando(true)
    const p = await ejecutar(async (id) => { await checkoutApi.medio(id, elegido); return checkoutApi.resumen(id) })
    setEnviando(false)
    if (p) navegar('/checkout/resumen')
  }

  if (!proceso && !error) return <Cargando />
  return (
    <>
      <h1>Finalizar compra</h1>
      <PasosCheckout actual={3} procesoId={proceso?.id} />
      <Alerta tipo="error">{error}</Alerta>
      {proceso && (
        <div className="seccion" style={{ maxWidth: 640 }}>
          <h2>Medio de pago</h2>
          {medios === null ? <Cargando /> : medios.map((m) => (
            <div className="form-check mb-2" key={m.id}>
              <input className="form-check-input" type="radio" name="medio" id={`med${m.id}`} checked={elegido === m.id} onChange={() => setElegido(m.id)} />
              <label className="form-check-label" htmlFor={`med${m.id}`}>{m.nombre}</label>
            </div>
          ))}
          <small className="text-muted d-block">Pago simulado con fines académicos: no se solicitan ni almacenan datos de tarjetas.</small>
          <div className="d-flex justify-content-between gap-2 mt-3">
            <button type="button" className="btn btn-secundario" onClick={() => navegar('/checkout/envio')}>Volver</button>
            <button type="button" className="btn" disabled={!elegido || enviando} onClick={continuar}>{enviando ? 'Calculando...' : 'Revisar pedido'}</button>
          </div>
        </div>
      )}
    </>
  )
}
