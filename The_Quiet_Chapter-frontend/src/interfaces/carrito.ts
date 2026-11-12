/** Interfaces del carrito (DC-02), como pide la pauta: el carrito se tipa completo. */
export type TipoAvisoCarrito =
  | 'PRECIO_CAMBIADO'
  | 'PRODUCTO_AGOTADO'
  | 'STOCK_INSUFICIENTE'
  | 'CANTIDAD_AJUSTADA'
  | 'PRODUCTO_INACTIVO'
  | 'CARRITO_INACTIVO'

export interface ItemCarrito {
  libroId: number
  titulo: string
  portadaUrl: string | null
  cantidad: number
  precioUnitario: number
  subtotal: number
  disponible: boolean
  stockDisponible: number
}

export interface AvisoCarrito {
  libroId: number | null
  tipo: TipoAvisoCarrito
  mensaje: string
  bloqueante: boolean
}

export interface Carrito {
  id: number
  clienteId: number | null
  activo: boolean
  items: ItemCarrito[]
  unidades: number
  subtotal: number
  cuponAplicadoId: number | null
  codigoCuponAplicado: string | null
  descuentoCupon: number
  total: number
  puedeComprar: boolean
  exitosa: boolean
  avisos: AvisoCarrito[]
}
