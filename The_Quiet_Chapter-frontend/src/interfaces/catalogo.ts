/** Contratos de catalog-service (DC-01). */
export interface Clasificacion {
  id: number
  nombre: string
}

export interface LibroResumen {
  id: number
  titulo: string
  precio: number
  stock: number
  isbn: string
  portadaUrl: string
  categoriaId: number
  categoria: string
  editorialId: number
  editorial: string
  autores: string[]
  generos: string[]
  activo: boolean
  disponible: boolean
}

export interface PaginaLibros {
  libros: LibroResumen[]
  pagina: number
  tamanio: number
  totalElementos: number
  totalPaginas: number
}

export interface LibroDetalle {
  id: number
  isbn: string
  titulo: string
  precio: number
  activo: boolean
  stock: number
  disponible: boolean
  sinopsis: string
  formato: string
  paginas: number
  idioma: string
  anioPublicacion: number
  encuadernacion: string
  portadaUrl: string
  extractoUrl: string
  tieneExtracto: boolean
  categoria: Clasificacion
  editorial: Clasificacion
  autores: Clasificacion[]
  generos: Clasificacion[]
  fechaRegistro: string
}

export interface Autor {
  id: number
  nombre: string
  biografia: string
}

export type OrdenCatalogo = 'PRECIO_ASC' | 'PRECIO_DESC' | 'TITULO_ASC' | 'NOVEDADES'

export interface FiltrosCatalogo {
  texto?: string
  categorias?: number[]
  generos?: number[]
  minimo?: number
  maximo?: number
  autorId?: number
  editorialId?: number
  orden?: OrdenCatalogo
  pagina?: number
  tamanio?: number
}

export interface MovimientoStock {
  id: number
  tipo: string
  cantidad: number
  variacion: number
  stockAnterior: number
  stockPosterior: number
  fecha: string
  motivo: string
  responsableId: number | null
}

export interface LibroSolicitud {
  isbn: string
  titulo: string
  autores: string[]
  editorial: string
  categoria: string
  generos: string[]
  sinopsis: string
  formato: string
  paginas: number
  idioma: string
  anioPublicacion: number
  encuadernacion: string
  portadaUrl: string
  extractoUrl: string
  precio: number
  stockInicial: number
}

export interface CondicionLibro {
  id: number
  titulo: string
  precio: number
  stock: number
  activo: boolean
  portadaUrl: string
  autores: string
}
