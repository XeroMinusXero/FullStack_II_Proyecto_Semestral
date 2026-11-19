/** Interruptor activo/inactivo de una fila (libros, cupones, promociones). */
export function InterruptorEstado({ id, activo, onCambiar, textos = ['Activo', 'Inactivo'] }: {
  id: string
  activo: boolean
  onCambiar: () => void
  /** Texto cuando esta activo y cuando no (por ejemplo ['Activa', 'Inactiva']). */
  textos?: [string, string]
}) {
  return (
    <div className="form-check form-switch">
      <input className="form-check-input" type="checkbox" role="switch" id={id} checked={activo} onChange={onCambiar} />
      <label className="form-check-label" htmlFor={id}>{activo ? textos[0] : textos[1]}</label>
    </div>
  )
}
