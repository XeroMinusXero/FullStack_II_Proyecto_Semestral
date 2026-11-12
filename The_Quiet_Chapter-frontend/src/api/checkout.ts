import { peticion } from './http'
import type { MedioPago, MetodoEnvio, ProcesoCompra } from '../interfaces/checkout'
import type { DireccionSolicitud } from '../interfaces/usuario'

const BASE = '/api/checkout'

export const checkoutApi = {
  envios: (region?: string) => peticion<MetodoEnvio[]>(`${BASE}/opciones/envio`, { consulta: { region } }),
  medios: () => peticion<MedioPago[]>(`${BASE}/opciones/pago`),
  iniciar: (carritoId: number) => peticion<ProcesoCompra>(BASE, { metodo: 'POST', cuerpo: { carritoId } }),
  obtener: (id: number) => peticion<ProcesoCompra>(`${BASE}/${id}`),
  direccion: (id: number, d: DireccionSolicitud) => peticion<ProcesoCompra>(`${BASE}/${id}/direccion`, { metodo: 'PUT', cuerpo: d }),
  envio: (id: number, envioId: number) => peticion<ProcesoCompra>(`${BASE}/${id}/envio`, { metodo: 'PUT', cuerpo: { id: envioId } }),
  medio: (id: number, medioId: number) => peticion<ProcesoCompra>(`${BASE}/${id}/medio-pago`, { metodo: 'PUT', cuerpo: { id: medioId } }),
  resumen: (id: number) => peticion<ProcesoCompra>(`${BASE}/${id}/resumen`, { metodo: 'POST' }),
  pagar: (id: number, simularRechazo = false) => peticion<ProcesoCompra>(`${BASE}/${id}/pagar`, { metodo: 'POST', cuerpo: { simularRechazo } }),
  cancelar: (id: number) => peticion<ProcesoCompra>(`${BASE}/${id}/cancelar`, { metodo: 'POST' }),

  // ---- administracion de opciones
  adminEnvios: () => peticion<MetodoEnvio[]>(`${BASE}/admin/metodos-envio`),
  crearEnvio: (m: Omit<MetodoEnvio, 'id'>) => peticion<MetodoEnvio>(`${BASE}/admin/metodos-envio`, { metodo: 'POST', cuerpo: m }),
  editarEnvio: (id: number, m: Omit<MetodoEnvio, 'id'>) => peticion<MetodoEnvio>(`${BASE}/admin/metodos-envio/${id}`, { metodo: 'PUT', cuerpo: m }),
  adminMedios: () => peticion<MedioPago[]>(`${BASE}/admin/medios-pago`),
  crearMedio: (m: Omit<MedioPago, 'id'>) => peticion<MedioPago>(`${BASE}/admin/medios-pago`, { metodo: 'POST', cuerpo: m }),
  editarMedio: (id: number, m: Omit<MedioPago, 'id'>) => peticion<MedioPago>(`${BASE}/admin/medios-pago/${id}`, { metodo: 'PUT', cuerpo: m }),
}
