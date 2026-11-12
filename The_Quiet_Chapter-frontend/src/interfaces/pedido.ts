/** Contratos de order-service (DC-04). */
export type EstadoPedido = 'PAGADO' | 'EN_PREPARACION' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO'

export interface Pedido {
  id: number
  numeroPedido: string
  clienteId: number
  fechaCreacion: string
  estado: EstadoPedido
  detalles: { id: number; libroId: number; tituloLibro: string; cantidad: number; precioUnitario: number; subtotal: number }[]
  entrega: {
    calle: string
    numero: string
    comuna: string
    region: string
    metodoEnvio: string
    plazoEstimadoDias: number
    numeroSeguimiento: string | null
    direccionFormateada: string
  }
  importes: { subtotal: number; costoEnvio: number; impuestos: number; descuento: number; total: number; ivaIncluido: number }
  medioPago: string
  referenciaPago: string
  puedeCancelarse: boolean
  clienteNombre: string | null
  clienteEmail: string | null
}

export interface PaginaPedidos {
  pedidos: Pedido[]
  pagina: number
  tamano: number
  totalElementos: number
  totalPaginas: number
}
