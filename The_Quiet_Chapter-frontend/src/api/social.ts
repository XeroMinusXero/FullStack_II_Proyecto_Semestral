import { peticion } from './http'
import type { Favorito, OpinionesLibro, OrdenResenas, PaginaResenas, PromedioLibro, Resena } from '../interfaces/social'

export const socialApi = {
  favoritos: () => peticion<Favorito[]>('/api/favoritos'),
  idsFavoritos: () => peticion<number[]>('/api/favoritos/ids'),
  agregarFavorito: (libroId: number) => peticion<Favorito[]>(`/api/favoritos/${libroId}`, { metodo: 'POST' }),
  quitarFavorito: (libroId: number) => peticion<Favorito[]>(`/api/favoritos/${libroId}`, { metodo: 'DELETE' }),

  opiniones: (libroId: number, orden?: OrdenResenas, puntuacion?: number) =>
    peticion<OpinionesLibro>(`/api/resenas/libros/${libroId}`, { consulta: { orden, puntuacion } }),
  promedios: (libros: number[]) => libros.length ? peticion<PromedioLibro[]>('/api/resenas/promedios', { consulta: { libros } }) : Promise.resolve([]),
  publicar: (libroId: number, contenido: string, puntuacion?: number) =>
    peticion<Resena>(`/api/resenas/libros/${libroId}`, { metodo: 'POST', cuerpo: { contenido, puntuacion } }),
  calificar: (libroId: number, puntuacion: number) =>
    peticion<OpinionesLibro>(`/api/resenas/libros/${libroId}/calificacion`, { metodo: 'PUT', cuerpo: { puntuacion } }),
  editar: (id: number, contenido: string) => peticion<Resena>(`/api/resenas/${id}`, { metodo: 'PUT', cuerpo: { contenido } }),
  eliminar: (id: number) => peticion<void>(`/api/resenas/${id}`, { metodo: 'DELETE' }),
  marcarUtil: (id: number) => peticion<Resena>(`/api/resenas/${id}/util`, { metodo: 'POST' }),
  quitarUtil: (id: number) => peticion<Resena>(`/api/resenas/${id}/util`, { metodo: 'DELETE' }),
  reportar: (id: number, motivo: string) => peticion<void>(`/api/resenas/${id}/reportes`, { metodo: 'POST', cuerpo: { motivo } }),
  mias: () => peticion<Resena[]>('/api/resenas/mias'),
  // ---- moderacion
  reportadas: () => peticion<Resena[]>('/api/resenas/admin/reportadas'),
  aprobar: (id: number) => peticion<Resena>(`/api/resenas/admin/${id}/aprobar`, { metodo: 'POST' }),
  retirar: (id: number) => peticion<Resena>(`/api/resenas/admin/${id}/eliminar`, { metodo: 'POST' }),
  todas: (estado?: string, pagina = 0) => peticion<PaginaResenas>('/api/resenas/admin', { consulta: { estado, pagina } }),
}
