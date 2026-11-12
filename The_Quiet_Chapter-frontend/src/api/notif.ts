import { peticion } from './http'
import type { AvisoReabastecimiento, Boletin, Mensaje, Notificacion, Suscriptor } from '../interfaces/notif'
import type { Pagina } from '../interfaces/promo'

export const notifApi = {
  mias: (pagina = 0) => peticion<Pagina<Notificacion>>('/api/notificaciones/mias', { consulta: { pagina } }),
  todas: (f: { tipo?: string; destinatario?: string; pagina?: number }) => peticion<Pagina<Notificacion>>('/api/notificaciones', { consulta: f }),
  misAvisos: () => peticion<AvisoReabastecimiento[]>('/api/notificaciones/reabastecimiento'),
  pedirAviso: (libroId: number) => peticion<AvisoReabastecimiento>(`/api/notificaciones/reabastecimiento/${libroId}`, { metodo: 'POST' }),
  cancelarAviso: (libroId: number) => peticion<void>(`/api/notificaciones/reabastecimiento/${libroId}`, { metodo: 'DELETE' }),
  suscribir: (email: string) => peticion<Mensaje>('/api/boletines/suscripciones', { metodo: 'POST', cuerpo: { email } }),
  confirmar: (token: string) => peticion<Mensaje>('/api/boletines/suscripciones/confirmar', { metodo: 'POST', cuerpo: { token } }),
  cancelar: (token: string) => peticion<Mensaje>('/api/boletines/suscripciones/cancelar', { metodo: 'POST', cuerpo: { token } }),
  // ---- administracion
  suscriptores: (estado?: string, pagina = 0) => peticion<Pagina<Suscriptor>>('/api/boletines/suscriptores', { consulta: { estado, pagina } }),
  boletines: (estado?: string, pagina = 0) => peticion<Pagina<Boletin>>('/api/boletines', { consulta: { estado, pagina } }),
  crearBoletin: (asunto: string, contenido: string) => peticion<Boletin>('/api/boletines', { metodo: 'POST', cuerpo: { asunto, contenido } }),
  editarBoletin: (id: number, asunto: string, contenido: string) => peticion<Boletin>(`/api/boletines/${id}`, { metodo: 'PUT', cuerpo: { asunto, contenido } }),
  enviarBoletin: (id: number) => peticion<Boletin>(`/api/boletines/${id}/enviar`, { metodo: 'POST' }),
}
