/**
 * Cliente HTTP unico (fetch + async/await, como en los tutoriales 11 y 12 del curso).
 * Todas las rutas son relativas (/api/...): Vite las reenvia al API Gateway (8090).
 */
const CLAVE_SESION = 'tqc_sesion'

export interface SesionGuardada {
  token: string
  expira: number
}

export class ErrorApi extends Error {
  estado: number
  campos: Record<string, string>
  constructor(mensaje: string, estado: number, campos: Record<string, string> = {}) {
    super(mensaje)
    this.estado = estado
    this.campos = campos
  }
}

export function leerSesion(): SesionGuardada | null {
  try {
    const crudo = localStorage.getItem(CLAVE_SESION)
    if (!crudo) return null
    const s = JSON.parse(crudo) as SesionGuardada
    if (!s.token || Date.now() >= s.expira) {
      localStorage.removeItem(CLAVE_SESION)
      return null
    }
    return s
  } catch {
    return null
  }
}

export function guardarSesion(token: string, segundos: number) {
  try {
    localStorage.setItem(CLAVE_SESION, JSON.stringify({ token, expira: Date.now() + segundos * 1000 }))
  } catch {
    /* navegador sin almacenamiento: la sesion dura mientras la pestaña este abierta */
  }
}

export function borrarSesion() {
  try {
    localStorage.removeItem(CLAVE_SESION)
  } catch {
    /* sin almacenamiento */
  }
}

type ValorConsulta = string | number | boolean | undefined | null | (string | number)[]

export function construirUrl(ruta: string, consulta?: Record<string, ValorConsulta>): string {
  const params = new URLSearchParams()
  Object.entries(consulta ?? {}).forEach(([clave, valor]) => {
    if (valor === undefined || valor === null || valor === '') return
    if (Array.isArray(valor)) {
      if (valor.length) params.set(clave, valor.join(','))
      return
    }
    params.set(clave, String(valor))
  })
  const qs = params.toString()
  return qs ? `${ruta}?${qs}` : ruta
}

interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  cuerpo?: unknown
  consulta?: Record<string, ValorConsulta>
}

export async function peticion<T>(ruta: string, opciones: Opciones = {}): Promise<T> {
  const encabezados: Record<string, string> = { Accept: 'application/json' }
  const sesion = leerSesion()
  if (sesion) encabezados.Authorization = `Bearer ${sesion.token}`
  if (opciones.cuerpo !== undefined) encabezados['Content-Type'] = 'application/json'

  let respuesta: Response
  try {
    respuesta = await fetch(construirUrl(ruta, opciones.consulta), {
      method: opciones.metodo ?? 'GET',
      headers: encabezados,
      body: opciones.cuerpo !== undefined ? JSON.stringify(opciones.cuerpo) : undefined,
      credentials: 'include',
    })
  } catch {
    throw new ErrorApi('No se pudo conectar con el servidor. Revisa que el API Gateway este corriendo.', 0)
  }

  if (respuesta.status === 401 && sesion) {
    borrarSesion()
    window.dispatchEvent(new Event('tqc:sesion-expirada'))
  }

  const esJson = respuesta.headers.get('content-type')?.includes('json')
  const datos = respuesta.status === 204 || !esJson ? undefined : await respuesta.json().catch(() => undefined)

  if (!respuesta.ok) {
    const cuerpo = (datos ?? {}) as { error?: string; detail?: string; message?: string; campos?: Record<string, string> }
    const mensaje = cuerpo.error ?? cuerpo.detail ?? cuerpo.message ?? `Error ${respuesta.status}`
    throw new ErrorApi(mensaje, respuesta.status, cuerpo.campos ?? {})
  }
  return datos as T
}

/** Descarga un archivo (CSV de reportes) respetando el token. */
export async function descargar(ruta: string, consulta: Record<string, ValorConsulta>, nombre: string) {
  const sesion = leerSesion()
  const respuesta = await fetch(construirUrl(ruta, consulta), {
    headers: sesion ? { Authorization: `Bearer ${sesion.token}` } : {},
  })
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => ({}))
    throw new ErrorApi(cuerpo.error ?? `Error ${respuesta.status}`, respuesta.status)
  }
  const blob = await respuesta.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = nombre
  a.click()
  URL.revokeObjectURL(url)
}

export function mensajeDe(error: unknown): string {
  if (error instanceof Error) return error.message
  return 'Ocurrio un error inesperado'
}
