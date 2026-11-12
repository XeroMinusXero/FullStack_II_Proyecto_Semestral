import { useEffect, type ReactNode } from 'react'

/** Modal de Bootstrap controlado por React (sin el JS de Bootstrap). */
export function Modal({ abierto, titulo, onCerrar, children, pie, ancho = 'lg' }: {
  abierto: boolean
  titulo: string
  onCerrar: () => void
  children: ReactNode
  pie?: ReactNode
  ancho?: 'sm' | 'lg' | 'xl'
}) {
  useEffect(() => {
    if (!abierto) return
    const alTeclear = (e: KeyboardEvent) => { if (e.key === 'Escape') onCerrar() }
    document.addEventListener('keydown', alTeclear)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', alTeclear)
      document.body.style.overflow = ''
    }
  }, [abierto, onCerrar])

  if (!abierto) return null
  return (
    <>
      <div className="modal-backdrop fade show" onClick={onCerrar} />
      <div className="modal fade show d-block" role="dialog" aria-modal="true" aria-label={titulo} onClick={onCerrar}>
        <div className={`modal-dialog modal-${ancho} modal-dialog-scrollable modal-dialog-centered`} onClick={(e) => e.stopPropagation()}>
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title" style={{ fontFamily: "'Playfair Display', serif" }}>{titulo}</h5>
              <button type="button" className="btn-close" aria-label="Cerrar" onClick={onCerrar} />
            </div>
            <div className="modal-body">{children}</div>
            {pie && <div className="modal-footer">{pie}</div>}
          </div>
        </div>
      </div>
    </>
  )
}
