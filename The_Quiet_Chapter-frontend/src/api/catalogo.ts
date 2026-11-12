import { peticion } from './http'
import type {
  Autor, Clasificacion, CondicionLibro, FiltrosCatalogo, LibroDetalle, LibroResumen, LibroSolicitud,
  MovimientoStock, OrdenCatalogo, PaginaLibros,
} from '../interfaces/catalogo'

const BASE = '/api/libros'

export const catalogoApi = {
  buscar: (f: FiltrosCatalogo) => peticion<PaginaLibros>(BASE, { consulta: { ...f } }),
  sugerencias: (texto: string) => peticion<LibroResumen[]>(`${BASE}/sugerencias`, { consulta: { texto, limite: 6 } }),
  detalle: (id: number) => peticion<LibroDetalle>(`${BASE}/${id}`),
  condiciones: (ids: number[]) => ids.length ? peticion<CondicionLibro[]>(`${BASE}/condiciones`, { consulta: { ids } }) : Promise.resolve([]),
  categorias: () => peticion<Clasificacion[]>(`${BASE}/categorias`),
  generos: () => peticion<Clasificacion[]>(`${BASE}/generos`),
  autores: () => peticion<Clasificacion[]>(`${BASE}/autores`),
  editoriales: () => peticion<Clasificacion[]>(`${BASE}/editoriales`),
  autor: (id: number) => peticion<Autor>(`${BASE}/autores/${id}`),

  // ---- administracion
  mantenedor: (c: { texto?: string; estado?: string; agotados?: boolean; orden?: OrdenCatalogo; pagina?: number; tamanio?: number }) =>
    peticion<PaginaLibros>(`${BASE}/admin`, { consulta: c }),
  crear: (s: LibroSolicitud) => peticion<LibroDetalle>(BASE, { metodo: 'POST', cuerpo: s }),
  editar: (id: number, s: LibroSolicitud) => peticion<LibroDetalle>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo: s }),
  eliminar: (id: number) => peticion<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
  precio: (id: number, precio: number) => peticion<LibroDetalle>(`${BASE}/${id}/precio`, { metodo: 'PATCH', cuerpo: { precio } }),
  estado: (id: number, activo: boolean) => peticion<LibroDetalle>(`${BASE}/${id}/estado`, { metodo: 'PATCH', cuerpo: { activo } }),
  actualizarStock: (id: number, nuevoStock: number) => peticion<MovimientoStock>(`${BASE}/${id}/stock`, { metodo: 'PUT', cuerpo: { nuevoStock } }),
  ajuste: (id: number, nuevoStock: number, motivo: string) =>
    peticion<MovimientoStock>(`${BASE}/${id}/stock/ajustes`, { metodo: 'POST', cuerpo: { nuevoStock, motivo } }),
  ingreso: (id: number, cantidad: number) => peticion<MovimientoStock>(`${BASE}/${id}/stock/ingresos`, { metodo: 'POST', cuerpo: { cantidad } }),
  movimientos: (id: number) => peticion<MovimientoStock[]>(`${BASE}/${id}/stock/movimientos`),
  biografia: (id: number, biografia: string) => peticion<Autor>(`${BASE}/autores/${id}/biografia`, { metodo: 'PUT', cuerpo: { biografia } }),
}
