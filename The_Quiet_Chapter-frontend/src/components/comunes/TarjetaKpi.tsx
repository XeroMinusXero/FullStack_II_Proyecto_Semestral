import type { ReactNode } from 'react'

/** Indicador del dashboard (HU-140): etiqueta pequena y valor destacado, dentro de un .kpi-grid. */
export function TarjetaKpi({ etiqueta, valor }: { etiqueta: string; valor: ReactNode }) {
  return <div className="kpi-card"><div className="etiqueta">{etiqueta}</div><div className="valor">{valor}</div></div>
}
