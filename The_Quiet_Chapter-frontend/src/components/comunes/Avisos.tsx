import { Alerta } from './Alerta'
import type { EstadoAccion } from '../../hooks/useAccion'

/** Muestra el exito y el error de un useAccion(), cada uno con su boton para cerrarlo. */
export function Avisos({ estado }: { estado: EstadoAccion }) {
  return (
    <>
      <Alerta tipo="exito" onCerrar={() => estado.setExito('')}>{estado.exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => estado.setError('')}>{estado.error}</Alerta>
    </>
  )
}
