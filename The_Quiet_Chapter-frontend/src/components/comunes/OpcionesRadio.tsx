import type { ReactNode } from 'react'
import estilos from './OpcionesRadio.module.css'

export interface OpcionRadio<T extends string | number> { valor: T; contenido: ReactNode }

/**
 * Grupo de opciones excluyentes (radio buttons) presentado como tarjetas seleccionables.
 * Se usa en los pasos del checkout: direccion guardada, metodo de envio y medio de pago.
 */
export function OpcionesRadio<T extends string | number>({ nombre, etiqueta, opciones, valor, onCambiar }: {
  nombre: string
  etiqueta: string
  opciones: OpcionRadio<T>[]
  valor: T | null
  onCambiar: (v: T) => void
}) {
  return (
    <div role="radiogroup" aria-label={etiqueta} className={estilos.grupo}>
      {opciones.map((o) => {
        const id = `${nombre}-${o.valor}`
        return (
          <label key={o.valor} htmlFor={id} className={o.valor === valor ? estilos.elegida : estilos.opcion}>
            <input className="form-check-input m-0" type="radio" name={nombre} id={id} checked={o.valor === valor} onChange={() => onCambiar(o.valor)} />
            <span>{o.contenido}</span>
          </label>
        )
      })}
    </div>
  )
}
