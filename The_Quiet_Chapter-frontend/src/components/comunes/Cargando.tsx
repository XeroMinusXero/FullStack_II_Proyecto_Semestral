export function Cargando({ texto = 'Cargando...' }: { texto?: string }) {
  return (
    <div className="d-flex align-items-center gap-2 my-4" role="status">
      <div className="spinner-border spinner-border-sm" style={{ color: 'var(--cobre)' }} aria-hidden="true" />
      <span>{texto}</span>
    </div>
  )
}
