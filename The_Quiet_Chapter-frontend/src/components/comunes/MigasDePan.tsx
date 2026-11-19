import { Link } from 'react-router-dom'

/** HU-012: ruta jerarquica con niveles clicables. */
export function MigasDePan({ niveles }: { niveles: { texto: string; a?: string }[] }) {
  return (
    <nav aria-label="Ruta de navegación" className="mb-3">
      <ol className="breadcrumb mb-0 small">
        {niveles.map((n, i) => (
          <li key={i} className={`breadcrumb-item${i === niveles.length - 1 ? ' active' : ''}`} aria-current={i === niveles.length - 1 ? 'page' : undefined}>
            {n.a && i < niveles.length - 1 ? <Link to={n.a}>{n.texto}</Link> : <strong>{n.texto}</strong>}
          </li>
        ))}
      </ol>
    </nav>
  )
}
