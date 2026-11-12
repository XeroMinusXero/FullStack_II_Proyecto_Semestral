import { useState } from 'react'
import { useCarrito } from '../../context/carrito'
import { useAuth } from '../../context/auth'
import { mensajeDe } from '../../api/http'

/** HU-013: agrega al carrito (o incrementa). Deshabilitado si el libro esta agotado. */
export function BotonAgregar({ libroId, disponible, cantidad = 1, texto = 'Agregar', className = '' }: {
  libroId: number
  disponible: boolean
  cantidad?: number
  texto?: string
  className?: string
}) {
  const { agregar } = useCarrito()
  const { esAdmin } = useAuth()
  const [estado, setEstado] = useState<'normal' | 'cargando' | 'ok' | 'error'>('normal')
  const [error, setError] = useState('')

  if (esAdmin) return null

  const alHacerClic = async () => {
    setEstado('cargando')
    setError('')
    try {
      await agregar(libroId, cantidad)
      setEstado('ok')
      setTimeout(() => setEstado('normal'), 1200)
    } catch (e) {
      setError(mensajeDe(e))
      setEstado('error')
      setTimeout(() => setEstado('normal'), 3000)
    }
  }

  return (
    <span className="d-inline-flex flex-column">
      <button type="button" className={`btn ${className}`} disabled={!disponible || estado === 'cargando'} onClick={alHacerClic}>
        {!disponible ? 'Agotado' : estado === 'ok' ? '✓ Agregado' : estado === 'cargando' ? 'Agregando...' : texto}
      </button>
      {estado === 'error' && <small className="text-danger mt-1" role="alert">{error}</small>}
    </span>
  )
}
