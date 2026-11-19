export function Cargando({ texto = 'Cargando...' }: { texto?: string }) {
  return (
    <div className="d-flex align-items-center gap-2 my-4" role="status">
      <div className="spinner-border spinner-border-sm spinner-tema" aria-hidden="true" />
      <span>{texto}</span>
    </div>
  )
}
