import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { PasosCheckout } from './PasosCheckout'
import { useProcesoCompra } from './proceso'
import type { MedioPago } from '../../interfaces/checkout'
import { OpcionesRadio } from '../../components/comunes/OpcionesRadio'
import estilos from './Checkout.module.css'

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
        <div className={`seccion ${estilos.paso}`}>
          <h2>Medio de pago</h2>
          {medios === null ? <Cargando /> : (
            <OpcionesRadio nombre="medio" etiqueta="Medio de pago" valor={elegido} onCambiar={setElegido}
              opciones={medios.map((m) => ({ valor: m.id, contenido: m.nombre }))} />
          )}
          <small className="text-muted d-block">Pago simulado con fines académicos: no se solicitan ni almacenan datos de tarjetas.</small>
          <div className={estilos.acciones}>
            <button type="button" className="btn btn-secundario" onClick={() => navegar('/checkout/envio')}>Volver</button>
            <button type="button" className="btn" disabled={!elegido || enviando} onClick={continuar}>{enviando ? 'Calculando...' : 'Revisar pedido'}</button>
          </div>
        </div>
      )}
    </>
  )
}
