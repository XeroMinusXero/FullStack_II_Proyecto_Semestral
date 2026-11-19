import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { PasosCheckout } from './PasosCheckout'
import { useProcesoCompra } from './proceso'
import { clp } from '../../utils/formato'
import { OpcionesRadio } from '../../components/comunes/OpcionesRadio'
import estilos from './Checkout.module.css'
import type { MetodoEnvio } from '../../interfaces/checkout'

/** checkoutEnvio.html -> HU-024: metodos de envio habilitados para la region elegida. */
export default function CheckoutEnvio() {
  const navegar = useNavigate()
  const { proceso, error, ejecutar } = useProcesoCompra()
  const [metodos, setMetodos] = useState<MetodoEnvio[] | null>(null)
  const [elegido, setElegido] = useState<number | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!proceso) return
    if (!proceso.direccion) { navegar('/checkout/direccion', { replace: true }); return }
    checkoutApi.envios(proceso.direccion.region).then((m) => {
      setMetodos(m)
      setElegido(proceso.envio?.id ?? m[0]?.id ?? null)
    }).catch(() => setMetodos([]))
  }, [proceso, navegar])

  const continuar = async () => {
    if (!elegido) return
    setEnviando(true)
    const p = await ejecutar((id) => checkoutApi.envio(id, elegido))
    setEnviando(false)
    if (p) navegar('/checkout/pago')
  }

  if (!proceso && !error) return <Cargando />
  return (
    <>
      <h1>Finalizar compra</h1>
      <PasosCheckout actual={2} procesoId={proceso?.id} />
      <Alerta tipo="error">{error}</Alerta>
      {proceso && (
        <div className={`seccion ${estilos.paso}`}>
          <h2>Método de envío</h2>
          <p className="text-muted">Enviar a: {proceso.direccion?.formateada}</p>
          {metodos === null ? <Cargando /> : metodos.length === 0 ? <p>No hay métodos de envío disponibles para tu región.</p> : (
            <OpcionesRadio nombre="envio" etiqueta="Método de envío" valor={elegido} onCambiar={setElegido}
              opciones={metodos.map((m) => ({
                valor: m.id,
                contenido: <><strong>{m.nombre}</strong> — {m.costoBase > 0 ? clp(m.costoBase) : 'Gratis'} · {m.plazoEstimadoDias} {m.plazoEstimadoDias === 1 ? 'día hábil' : 'días hábiles'}</>,
              }))} />
          )}
          <div className={estilos.acciones}>
            <button type="button" className="btn btn-secundario" onClick={() => navegar('/checkout/direccion')}>Volver</button>
            <button type="button" className="btn" disabled={!elegido || enviando} onClick={continuar}>{enviando ? 'Guardando...' : 'Continuar'}</button>
          </div>
        </div>
      )}
    </>
  )
}
