import type { ReactNode } from 'react'

/** Tabla simple de solo lectura (reportes): cabeceras y filas de celdas ya formateadas. */
export function TablaDatos({ cabeceras, filas, vacio = 'Sin datos para el período.' }: {
  cabeceras: string[]
  filas: ReactNode[][]
  vacio?: string
}) {
  return (
    <div className="tabla-scroll">
      <table>
        <thead><tr>{cabeceras.map((c) => <th key={c}>{c}</th>)}</tr></thead>
        <tbody>
          {filas.length === 0 && <tr><td colSpan={cabeceras.length}>{vacio}</td></tr>}
          {filas.map((f, i) => <tr key={i}>{f.map((v, j) => <td key={j}>{v}</td>)}</tr>)}
        </tbody>
      </table>
    </div>
  )
}
