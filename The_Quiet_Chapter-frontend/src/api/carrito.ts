import { peticion } from './http'
import type { Carrito } from '../interfaces/carrito'

const BASE = '/api/carrito'

/** Visitante: cookie HttpOnly del carrito anonimo. Cliente: el token identifica su carrito. */
export const carritoApi = {
  obtener: () => peticion<Carrito>(BASE),
  agregar: (libroId: number, cantidad = 1) => peticion<Carrito>(`${BASE}/items`, { metodo: 'POST', cuerpo: { libroId, cantidad } }),
  cambiarCantidad: (libroId: number, cantidad: number) =>
    peticion<Carrito>(`${BASE}/items/${libroId}`, { metodo: 'PUT', cuerpo: { cantidad } }),
  eliminar: (libroId: number) => peticion<Carrito>(`${BASE}/items/${libroId}`, { metodo: 'DELETE' }),
  vaciar: () => peticion<Carrito>(`${BASE}/items`, { metodo: 'DELETE' }),
  fusionar: () => peticion<Carrito>(`${BASE}/fusionar`, { metodo: 'POST' }),
  aplicarCupon: (codigo: string) => peticion<Carrito>(`${BASE}/cupon`, { metodo: 'POST', cuerpo: { codigo } }),
  quitarCupon: () => peticion<Carrito>(`${BASE}/cupon`, { metodo: 'DELETE' }),
}
