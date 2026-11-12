import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { Confirmar } from '../../components/comunes/Confirmar'
import { borrarCheckoutId } from './proceso'

const PASOS = ['Dirección', 'Envío', 'Pago', 'Resumen']

/** Indicador de pasos (.pasos-checkout del sitio) + cancelar compra (HU-031). */
export function PasosCheckout({ actual, procesoId }: { actual: 1 | 2 | 3 | 4; procesoId?: number }) {
  const navegar = useNavigate()
  const [confirmar, setConfirmar] = useState(false)
  return (
    <>
      <div className="pasos-checkout" aria-label={`Paso ${actual} de 4`}>
        {PASOS.map((p, i) => <span key={p} className={i + 1 === actual ? 'activo' : undefined}>{i + 1}. {p}</span>)}
      </div>
      {procesoId && (
        <div className="text-end mb-2">
          <button type="button" className="btn btn-secundario" onClick={() => setConfirmar(true)}>Cancelar compra</button>
        </div>
      )}
      <Confirmar abierto={confirmar} titulo="Cancelar compra" mensaje="Se descartará este proceso de compra. Tu carrito se mantiene intacto."
        textoConfirmar="Sí, cancelar" onCancelar={() => setConfirmar(false)}
        onConfirmar={async () => {
          setConfirmar(false)
          try { if (procesoId) await checkoutApi.cancelar(procesoId) } catch { /* si ya no existe, igual se vuelve al carrito */ }
          borrarCheckoutId()
          navegar('/carrito')
        }} />
    </>
  )
}
