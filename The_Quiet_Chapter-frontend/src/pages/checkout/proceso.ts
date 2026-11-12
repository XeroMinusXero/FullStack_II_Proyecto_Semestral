import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutApi } from '../../api/checkout'
import { mensajeDe } from '../../api/http'
import type { ProcesoCompra } from '../../interfaces/checkout'

/** El id del proceso de compra vive en sessionStorage: sobrevive a recargas pero no a cerrar la pestana. */
const CLAVE = 'tqc_checkout'

export function guardarCheckoutId(id: number) {
  try { sessionStorage.setItem(CLAVE, String(id)) } catch { /* sin almacenamiento: se reinicia desde el carrito */ }
}
export function leerCheckoutId(): number | null {
  try { const v = sessionStorage.getItem(CLAVE); return v ? Number(v) : null } catch { return null }
}
export function borrarCheckoutId() {
  try { sessionStorage.removeItem(CLAVE) } catch { /* nada */ }
}

/** Carga el proceso activo; si no existe o ya termino, devuelve al carrito. */
export function useProcesoCompra() {
  const navegar = useNavigate()
  const [proceso, setProceso] = useState<ProcesoCompra | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const id = leerCheckoutId()
    if (!id) { navegar('/carrito', { replace: true }); return }
    checkoutApi.obtener(id)
      .then((p) => {
        if (p.estado === 'CANCELADO' || p.estado === 'FINALIZADO') { borrarCheckoutId(); navegar('/carrito', { replace: true }); return }
        setProceso(p)
      })
      .catch((e) => setError(mensajeDe(e)))
  }, [navegar])

  const ejecutar = useCallback(async (fn: (id: number) => Promise<ProcesoCompra>) => {
    if (!proceso) return null
    setError('')
    try { const p = await fn(proceso.id); setProceso(p); return p } catch (e) { setError(mensajeDe(e)); return null }
  }, [proceso])

  return { proceso, error, setError, ejecutar }
}
