import { descargar, peticion } from './http'
import type { Panel, PeriodoRapido, ReporteClientes, ReporteInventario, ReporteLibros, ReporteVentas } from '../interfaces/reportes'

export interface FiltroPeriodo {
  periodo?: PeriodoRapido
  desde?: string
  hasta?: string
}

export const reportesApi = {
  panel: (f: FiltroPeriodo) => peticion<Panel>('/api/reportes/panel', { consulta: { ...f } }),
  ventas: (f: FiltroPeriodo) => peticion<ReporteVentas>('/api/reportes/ventas', { consulta: { ...f } }),
  libros: (f: FiltroPeriodo, limite = 10) => peticion<ReporteLibros>('/api/reportes/libros', { consulta: { ...f, limite } }),
  inventario: (f: FiltroPeriodo) => peticion<ReporteInventario>('/api/reportes/inventario', { consulta: { ...f } }),
  clientes: (f: FiltroPeriodo, limite = 10) => peticion<ReporteClientes>('/api/reportes/clientes', { consulta: { ...f, limite } }),
  csv: (tipo: 'ventas' | 'libros-mas-vendidos' | 'inventario' | 'clientes', f: FiltroPeriodo) =>
    descargar(`/api/reportes/${tipo}/csv`, { ...f }, `tqc-${tipo}.csv`),
}
