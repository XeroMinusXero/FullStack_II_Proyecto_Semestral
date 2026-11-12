/** Contratos de notif-service (DC-08). */
export interface Notificacion {
  id: number
  destinatario: string
  tipo: string
  asunto: string
  mensaje: string
  enlaceAccion: string | null
  estado: 'PENDIENTE' | 'ENVIADA' | 'FALLIDA'
  fechaCreacion: string
  fechaEnvio: string | null
}

export interface AvisoReabastecimiento {
  id: number
  libroId: number
  fechaSolicitud: string
  activa: boolean
}

export interface Suscriptor {
  id: number
  email: string
  fechaSuscripcion: string
  fechaConfirmacion: string | null
  estado: 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA'
}

export interface Boletin {
  id: number
  asunto: string
  contenido: string
  estado: 'BORRADOR' | 'ENVIADO'
  fechaCreacion: string
  fechaEnvio: string | null
  cantidadDestinatarios: number
}

export interface Mensaje {
  mensaje: string
}
