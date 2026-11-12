import { peticion } from './http'
import type { EstadoPedido, PaginaPedidos, Pedido } from '../interfaces/pedido'

const BASE = '/api/pedidos'

interface Filtro {
  estado?: EstadoPedido | ''
  fechaDesde?: string
  fechaHasta?: string
  pagina?: number
  tamano?: number
}

export const pedidosApi = {
  mios: (f: Filtro = {}) => peticion<PaginaPedidos>(`${BASE}/mis-pedidos`, { consulta: { ...f } }),
  miPedido: (id: number) => peticion<Pedido>(`${BASE}/mis-pedidos/${id}`),
  miPedidoPorNumero: (numero: string) => peticion<Pedido>(`${BASE}/mis-pedidos/numero/${encodeURIComponent(numero)}`),
  cancelarMio: (id: number) => peticion<Pedido>(`${BASE}/mis-pedidos/${id}/cancelar`, { metodo: 'POST' }),
  // ---- administracion
  listar: (f: Filtro = {}) => peticion<PaginaPedidos>(BASE, { consulta: { ...f } }),
  porNumero: (numero: string) => peticion<Pedido>(`${BASE}/numero/${encodeURIComponent(numero)}`),
  detalle: (id: number) => peticion<Pedido>(`${BASE}/${id}`),
  cambiarEstado: (id: number, estado: EstadoPedido) => peticion<Pedido>(`${BASE}/${id}/estado`, { metodo: 'PUT', cuerpo: { estado } }),
  seguimiento: (id: number, numeroSeguimiento: string) =>
    peticion<Pedido>(`${BASE}/${id}/seguimiento`, { metodo: 'PUT', cuerpo: { numeroSeguimiento } }),
  cancelar: (id: number) => peticion<Pedido>(`${BASE}/${id}/cancelar`, { metodo: 'POST' }),
}
