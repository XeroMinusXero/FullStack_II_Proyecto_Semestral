import { createContext, useContext } from 'react'
import type { AvisoCarrito, Carrito } from '../interfaces/carrito'

export interface EstadoCarrito {
  carrito: Carrito | null
  cargando: boolean
  unidades: number
  avisos: AvisoCarrito[]
  recargar: () => Promise<void>
  agregar: (libroId: number, cantidad?: number) => Promise<void>
  cambiarCantidad: (libroId: number, cantidad: number) => Promise<void>
  eliminar: (libroId: number) => Promise<void>
  vaciar: () => Promise<void>
  aplicarCupon: (codigo: string) => Promise<void>
  quitarCupon: () => Promise<void>
}

export const CarritoContext = createContext<EstadoCarrito | null>(null)

export function useCarrito(): EstadoCarrito {
  const ctx = useContext(CarritoContext)
  if (!ctx) throw new Error('useCarrito debe usarse dentro de <CarritoProvider>')
  return ctx
}
