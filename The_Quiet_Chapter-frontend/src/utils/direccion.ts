import type { DatosDireccion } from './validaciones'

export const DIRECCION_VACIA: DatosDireccion = { calle: '', numero: '', comuna: '', region: '' }

/** Datos listos para enviar: sin espacios sobrantes. */
export const limpiarDireccion = (d: DatosDireccion): DatosDireccion =>
  ({ calle: d.calle.trim(), numero: d.numero.trim(), comuna: d.comuna.trim(), region: d.region })

/** Copia solo los 4 campos editables de una direccion guardada (que trae ademas id y predeterminada). */
export const camposDe = (d: DatosDireccion): DatosDireccion =>
  ({ calle: d.calle, numero: d.numero, comuna: d.comuna, region: d.region })
