/** Contratos de report-service (DC-09). */
export type PeriodoRapido = 'HOY' | 'SEMANA' | 'MES' | 'ANIO'

export interface Periodo {
  fechaDesde: string
  fechaHasta: string
}

export interface Panel {
  periodo: Periodo
  ventasPeriodo: number
  cantidadPedidos: number
  ticketPromedio: number
  clientesRegistrados: number
  agrupacion: 'DIA' | 'SEMANA' | 'MES'
  evolucion: Record<string, number>
  pedidosPorEstado: Record<string, number>
  sinDatos: boolean
}

export interface LibroAnalitico {
  libroId: number
  titulo: string
  autor: string
  activo: boolean
  stockActual: number
  precioVenta: number
  unidadesVendidas: number
  montoTotal: number
}

export interface ReporteLibros {
  periodo: Periodo
  masVendidos: LibroAnalitico[]
  menosVendidos: LibroAnalitico[]
  sinVentas: LibroAnalitico[]
}

export interface FilaInventario {
  libroId: number
  titulo: string
  autor: string
  unidadesVendidas: number
  stockActual: number
  stockPromedio: number
  rotacion: number | null
  precioVenta: number
  valorTotal: number
  activo: boolean
}

export interface MovimientoAnalitico {
  movimientoId: number
  libroId: number
  tituloLibro: string
  fecha: string
  tipo: string
  cantidad: number
  responsableId: number | null
  responsableNombre: string
}

export interface ReporteInventario {
  periodo: Periodo
  libros: FilaInventario[]
  valorTotalInventario: number
  movimientos: MovimientoAnalitico[]
}

export interface ClienteAnalitico {
  clienteId: number
  nombre: string
  email: string
  fechaRegistro: string
  cantidadPedidosPeriodo: number
  montoCompradoPeriodo: number
}

export interface ReporteClientes {
  periodo: Periodo
  clientesNuevos: number
  clientesAcumulados: number
  mayoresCompradores: ClienteAnalitico[]
  sinCompras: ClienteAnalitico[]
}

export interface ReporteVentas {
  periodo: Periodo
  cantidadPedidos: number
  montoTotal: number
  agrupacion: string
  evolucion: Record<string, number>
  pedidosPorEstado: Record<string, number>
  porCategoria: { categoriaId: number | null; categoriaNombre: string; unidadesVendidas: number; montoTotal: number }[]
  pedidos: { numeroPedido: string; fecha: string; clienteId: number; clienteNombre: string | null; estado: string; total: number }[]
}
