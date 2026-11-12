import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { CarritoContext } from './carrito'
import { carritoApi } from '../api/carrito'
import { useAuth } from './auth'
import type { AvisoCarrito, Carrito } from '../interfaces/carrito'

/**
 * Estado global del carrito (HU-021: contador en la barra). El backend es la fuente de verdad:
 * cada operacion devuelve el carrito completo ya revisado contra stock y precios (HU-020).
 */
export function CarritoProvider({ children }: { children: ReactNode }) {
  const { usuario, esAdmin, cargando: cargandoSesion } = useAuth()
  const usuarioId = usuario?.id
  const [carrito, setCarrito] = useState<Carrito | null>(null)
  const [avisos, setAvisos] = useState<AvisoCarrito[]>([])
  const [cargando, setCargando] = useState(false)
  // Clave de la sesion para la que ya se cargo el carrito: mientras no coincida, se esta cargando.
  const [sesionCargada, setSesionCargada] = useState<string | null>(null)
  const claveSesion = `${usuarioId ?? 'anonimo'}-${esAdmin}`

  const aplicar = useCallback((c: Carrito) => {
    setCarrito(c)
    setAvisos(c.avisos ?? [])
  }, [])

  const recargar = useCallback(async () => {
    if (esAdmin) {
      setCarrito(null)
      return
    }
    setCargando(true)
    try {
      aplicar(await carritoApi.obtener())
    } catch {
      setCarrito(null)
    } finally {
      setCargando(false)
    }
  }, [aplicar, esAdmin])

  // Con sesion de cliente se llama a /fusionar (HU-019): integra el carrito anonimo de este navegador
  // (si existe) y devuelve el carrito de la cuenta. Sin sesion se usa el carrito anonimo (cookie).
  useEffect(() => {
    if (cargandoSesion) return
    let vigente = true
    const tarea = esAdmin
      ? Promise.resolve(null)
      : (usuarioId ? carritoApi.fusionar().catch(() => carritoApi.obtener()) : carritoApi.obtener()).catch(() => null)
    tarea.then((c) => {
      if (!vigente) return
      if (c) aplicar(c)
      else { setCarrito(null); setAvisos([]) }
      setSesionCargada(`${usuarioId ?? 'anonimo'}-${esAdmin}`)
    })
    return () => { vigente = false }
  }, [usuarioId, esAdmin, cargandoSesion, aplicar])

  const operar = useCallback(async (op: () => Promise<Carrito>) => {
    aplicar(await op())
  }, [aplicar])

  const valor = useMemo(() => ({
    carrito,
    cargando: cargando || cargandoSesion || sesionCargada !== claveSesion,
    avisos,
    unidades: carrito?.unidades ?? 0,
    recargar,
    agregar: (libroId: number, cantidad = 1) => operar(() => carritoApi.agregar(libroId, cantidad)),
    cambiarCantidad: (libroId: number, cantidad: number) => operar(() => carritoApi.cambiarCantidad(libroId, cantidad)),
    eliminar: (libroId: number) => operar(() => carritoApi.eliminar(libroId)),
    vaciar: () => operar(() => carritoApi.vaciar()),
    aplicarCupon: (codigo: string) => operar(() => carritoApi.aplicarCupon(codigo)),
    quitarCupon: () => operar(() => carritoApi.quitarCupon()),
  }), [carrito, cargando, cargandoSesion, sesionCargada, claveSesion, avisos, recargar, operar])

  return <CarritoContext.Provider value={valor}>{children}</CarritoContext.Provider>
}
