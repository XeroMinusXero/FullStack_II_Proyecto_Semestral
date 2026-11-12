/** Contratos de checkout-service (DC-03). */
export type EstadoCheckout = 'EN_PREPARACION' | 'LISTO_PARA_PAGO' | 'FINALIZADO' | 'CANCELADO'
export type EstadoPago = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'CANCELADO'

export interface MetodoEnvio {
  id: number
  nombre: string
  costoBase: number
  plazoEstimadoDias: number
  activo: boolean
  regionesHabilitadas: string[]
}

export interface MedioPago {
  id: number
  nombre: string
  activo: boolean
}

export interface LineaCompra {
  libroId: number
  titulo: string
  cantidad: number
  precioUnitario: number
  subtotal: number
}

export interface ResumenCompra {
  lineas: LineaCompra[]
  subtotal: number
  costoEnvio: number
  impuestos: number
  descuento: number
  total: number
  ivaIncluido: number
}

export interface IntentoPago {
  id: number
  monto: number
  estado: EstadoPago
  fecha: string
  referencia: string | null
  motivoRechazo: string | null
}

export interface ProcesoCompra {
  id: number
  clienteId: number
  carritoId: number
  estado: EstadoCheckout
  direccion: { id: number; calle: string; numero: string; comuna: string; region: string; formateada: string } | null
  envio: { id: number; nombre: string; costo: number; plazoEstimadoDias: number } | null
  medioPago: { id: number; nombre: string } | null
  resumen: ResumenCompra | null
  intentosPago: IntentoPago[]
  numeroPedidoConfirmado: string | null
  puedePagar: boolean
}
