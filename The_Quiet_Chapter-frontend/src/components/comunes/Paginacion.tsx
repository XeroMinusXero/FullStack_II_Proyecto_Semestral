/** HU-002: controles con total de paginas y pagina actual marcada. */
export function Paginacion({ pagina, totalPaginas, onCambiar }: { pagina: number; totalPaginas: number; onCambiar: (p: number) => void }) {
  if (totalPaginas <= 1) return null
  const paginas = Array.from({ length: totalPaginas }, (_, i) => i)
  return (
    <nav className="paginacion" aria-label="Paginación">
      <button type="button" disabled={pagina === 0} onClick={() => onCambiar(pagina - 1)} aria-label="Anterior">‹</button>
      {paginas.map((p) => (
        <button type="button" key={p} className={p === pagina ? 'activo' : ''} aria-current={p === pagina ? 'page' : undefined} onClick={() => onCambiar(p)}>
          {p + 1}
        </button>
      ))}
      <button type="button" disabled={pagina >= totalPaginas - 1} onClick={() => onCambiar(pagina + 1)} aria-label="Siguiente">›</button>
      <span className="align-self-center small text-muted ms-2">Página {pagina + 1} de {totalPaginas}</span>
    </nav>
  )
}
