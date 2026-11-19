import { useCallback, useState } from 'react'
import { mensajeDe } from '../api/http'

/** Estado que comparte una pantalla de gestion: el ultimo mensaje de exito y el ultimo error. */
export interface EstadoAccion {
  error: string
  exito: string
  setError: (m: string) => void
  setExito: (m: string) => void
}

/**
 * Ejecuta una operacion contra la API y deja el resultado listo para mostrar:
 * limpia los mensajes, llama a `operacion`, y si sale bien muestra `mensajeOk` y corre `despues`
 * (por ejemplo, recargar la tabla). Si falla, guarda el mensaje del backend. Devuelve true si salio bien.
 *
 *   const accion = useAccion()
 *   accion.ejecutar(() => promoApi.eliminarCupon(id), 'Cupón eliminado', recargar)
 */
export function useAccion() {
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  const ejecutar = useCallback(async (operacion: () => Promise<unknown>, mensajeOk?: string, despues?: () => unknown) => {
    setError('')
    setExito('')
    try {
      await operacion()
      if (mensajeOk) setExito(mensajeOk)
      await despues?.()
      return true
    } catch (e) {
      setError(mensajeDe(e))
      return false
    }
  }, [])

  return { error, exito, setError, setExito, ejecutar }
}

/** Lo que devuelve useAccion(): se pasa a componentes hijos que comparten los mensajes de la pagina. */
export type Accion = ReturnType<typeof useAccion>
