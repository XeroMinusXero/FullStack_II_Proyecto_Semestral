import type { ReactNode } from 'react'

export type TipoAlerta = 'exito' | 'error' | 'info'

/** Mensajes con el estilo del sitio original (.mensaje-exito) y Bootstrap para error/info. */
export function Alerta({ tipo, children, onCerrar }: { tipo: TipoAlerta; children: ReactNode; onCerrar?: () => void }) {
  if (!children) return null
  const clase = tipo === 'exito' ? 'mensaje-exito' : tipo === 'error' ? 'alert alert-danger' : 'alert alert-warning'
  return (
    <div className={`${clase} d-flex justify-content-between align-items-start gap-2`} role={tipo === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onCerrar && <button type="button" className="btn-close" aria-label="Cerrar" onClick={onCerrar} />}
    </div>
  )
}
