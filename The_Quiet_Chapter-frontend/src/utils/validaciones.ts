/** Reglas de validacion portadas desde js/script.js del sitio HTML (mismos mensajes). */
export const reglas = {
  requerido: (v: string) => v.trim().length > 0,
  email: (v: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim()),
  telefono: (v: string) => /^[+]?[\d\s\-()]{8,15}$/.test(v.trim()),
  soloNumeros: (v: string) => /^\d+$/.test(v.trim()),
  passwordFuerte: (v: string) => v.length >= 8 && v.length <= 72 && /[^A-Za-z0-9\s]/.test(v),
  soloLetras: (v: string) => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ\s'-]+$/.test(v.trim()),
  direccion: (v: string) => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ0-9\s.,#'\-/]+$/.test(v.trim()),
  isbn: (v: string) => /^[\dXx-]{10,17}$/.test(v.trim()),
}

export type Errores<T> = Partial<Record<keyof T, string>>

export const MENSAJES = {
  requerido: 'Este campo es obligatorio',
  email: 'Ingrese un correo válido',
  telefono: 'Ingrese un teléfono válido',
  soloNumeros: 'Solo números',
  passwordFuerte: 'Mínimo 8 caracteres y un carácter especial',
  soloLetras: 'Solo se permiten letras, espacios y guiones',
  direccion: 'Dirección inválida',
}

export function validarDireccion(d: { calle: string; numero: string; comuna: string; region: string }) {
  const e: Errores<typeof d> = {}
  if (!reglas.requerido(d.calle)) e.calle = MENSAJES.requerido
  else if (!reglas.direccion(d.calle) || d.calle.trim().length < 3) e.calle = MENSAJES.direccion
  if (!reglas.requerido(d.numero)) e.numero = MENSAJES.requerido
  else if (d.numero.trim().length > 30) e.numero = 'Máximo 30 caracteres'
  if (!reglas.requerido(d.comuna)) e.comuna = MENSAJES.requerido
  else if (!reglas.soloLetras(d.comuna)) e.comuna = MENSAJES.soloLetras
  if (!reglas.requerido(d.region)) e.region = MENSAJES.requerido
  return e
}
