/** Contratos de promo-service (DC-07). */
export type TipoDescuento = 'PORCENTAJE' | 'MONTO_FIJO'
export type TipoAlcance = 'LIBRO' | 'CATEGORIA' | 'GENERO' | 'EDITORIAL'

export interface Pagina<T> {
  contenido: T[]
  pagina: number
  tamano: number
  totalElementos: number
  totalPaginas: number
}

export interface Cupon {
  id: number
  codigo: string
  tipoDescuento: TipoDescuento
  valor: number
  montoMinimo: number
  fechaInicio: string
  fechaFin: string
  activo: boolean
  vigente: boolean
  canjes: number
}

export interface CuponSolicitud {
  codigo: string
  tipoDescuento: TipoDescuento
  valor: number
  montoMinimo: number
  fechaInicio: string
  fechaFin: string
}

export interface Canje {
  id: number
  clienteId: number
  pedidoId: number
  fechaCanje: string
  montoDescuento: number
}

export interface Alcance {
  tipo: TipoAlcance
  referenciaId: number
}

export interface Promocion {
  id: number
  nombre: string
  tipoDescuento: TipoDescuento
  valor: number
  fechaInicio: string
  fechaFin: string
  activo: boolean
  vigente: boolean
  alcances: Alcance[]
  aplicaciones: number
}

export interface PromocionSolicitud {
  nombre: string
  tipoDescuento: TipoDescuento
  valor: number
  fechaInicio: string
  fechaFin: string
  alcances: Alcance[]
  activo: boolean
}

export interface PrecioPromo {
  libroId: number
  precioOriginal: number
  precioFinal: number
  descuento: number
  porcentaje: number
  promocion: string
  vigenteHasta: string
}

export interface Oferta {
  libroId: number
  titulo: string
  autores: string
  portadaUrl: string
  disponible: boolean
  precioOriginal: number
  precioFinal: number
  porcentaje: number
  promocion: string
  vigenteHasta: string
}

export interface LibroSugerido {
  id: number
  titulo: string
  autores: string
  precio: number
  portadaUrl: string
  disponible: boolean
  categoriaId: number
}
