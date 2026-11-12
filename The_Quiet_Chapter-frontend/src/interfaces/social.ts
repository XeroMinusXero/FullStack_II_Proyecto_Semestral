/** Contratos de social-service (DC-06). */
export type OrdenResenas = 'MAS_RECIENTES' | 'MAS_UTILES' | 'MEJOR_CALIFICADAS'

export interface Favorito {
  libroId: number
  fechaAgregado: string
}

export interface ReporteResena {
  id: number
  clienteReportanteId: number
  motivo: string
  fechaReporte: string
  estado: string
}

export interface Resena {
  id: number
  libroId: number
  clienteId: number
  autorNombre: string
  contenido: string
  fechaPublicacion: string
  fechaActualizacion: string | null
  editada: boolean
  estado: 'PUBLICADA' | 'REPORTADA' | 'RETIRADA' | 'ELIMINADA_POR_AUTOR'
  votosUtiles: number
  puntuacion: number | null
  marcadaUtilPorMi: boolean
  propia: boolean
  reportes: ReporteResena[]
}

export interface OpinionesLibro {
  libroId: number
  promedio: number | null
  cantidadCalificaciones: number
  miCalificacion: number | null
  puedoOpinar: boolean
  resenas: Resena[]
}

export interface PromedioLibro {
  libroId: number
  promedio: number
  cantidad: number
}

export interface PaginaResenas {
  resenas: Resena[]
  pagina: number
  tamano: number
  totalElementos: number
  totalPaginas: number
}
