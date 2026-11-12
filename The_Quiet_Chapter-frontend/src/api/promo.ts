import { peticion } from './http'
import type { Canje, Cupon, CuponSolicitud, LibroSugerido, Oferta, Pagina, PrecioPromo, Promocion, PromocionSolicitud } from '../interfaces/promo'

export const promoApi = {
  precios: (libros: number[]) => libros.length ? peticion<PrecioPromo[]>('/api/promociones/precios', { consulta: { libros } }) : Promise.resolve([]),
  ofertas: () => peticion<Oferta[]>('/api/promociones/ofertas'),
  // ---- recomendaciones
  inicio: (limite = 8) => peticion<LibroSugerido[]>('/api/recomendaciones/inicio', { consulta: { limite } }),
  perfil: (limite = 8) => peticion<LibroSugerido[]>('/api/recomendaciones/perfil', { consulta: { limite } }),
  compras: (limite = 8) => peticion<LibroSugerido[]>('/api/recomendaciones/compras', { consulta: { limite } }),
  porCategoria: (id: number, limite = 6) => peticion<LibroSugerido[]>(`/api/recomendaciones/categorias/${id}`, { consulta: { limite } }),
  relacionados: (libroId: number, limite = 5) => peticion<LibroSugerido[]>(`/api/recomendaciones/libros/${libroId}/relacionados`, { consulta: { limite } }),
  complementarios: (libros: number[], limite = 4) =>
    libros.length ? peticion<LibroSugerido[]>('/api/recomendaciones/complementarios', { consulta: { libros, limite } }) : Promise.resolve([]),
  visita: (libroId: number) => peticion<void>(`/api/recomendaciones/visitas/${libroId}`, { metodo: 'POST' }),
  // ---- administracion
  cupones: (pagina = 0) => peticion<Pagina<Cupon>>('/api/cupones', { consulta: { pagina } }),
  crearCupon: (s: CuponSolicitud) => peticion<Cupon>('/api/cupones', { metodo: 'POST', cuerpo: s }),
  editarCupon: (id: number, s: CuponSolicitud) => peticion<Cupon>(`/api/cupones/${id}`, { metodo: 'PUT', cuerpo: s }),
  estadoCupon: (id: number, activo: boolean) => peticion<Cupon>(`/api/cupones/${id}/estado`, { metodo: 'PATCH', cuerpo: { activo } }),
  eliminarCupon: (id: number) => peticion<void>(`/api/cupones/${id}`, { metodo: 'DELETE' }),
  canjes: (id: number) => peticion<Canje[]>(`/api/cupones/${id}/canjes`),
  promociones: (pagina = 0) => peticion<Pagina<Promocion>>('/api/promociones/admin', { consulta: { pagina } }),
  crearPromocion: (s: PromocionSolicitud) => peticion<Promocion>('/api/promociones', { metodo: 'POST', cuerpo: s }),
  editarPromocion: (id: number, s: PromocionSolicitud) => peticion<Promocion>(`/api/promociones/${id}`, { metodo: 'PUT', cuerpo: s }),
  estadoPromocion: (id: number, activo: boolean) => peticion<Promocion>(`/api/promociones/${id}/estado`, { metodo: 'PATCH', cuerpo: { activo } }),
  eliminarPromocion: (id: number) => peticion<void>(`/api/promociones/${id}`, { metodo: 'DELETE' }),
}
