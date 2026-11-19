import { Link } from 'react-router-dom'
import { Estrellas } from '../comunes/Estrellas'
import { fecha } from '../../utils/formato'
import type { Resena } from '../../interfaces/social'

/** HU-090/091: resena reportada con sus motivos y las dos decisiones del administrador. */
export function TarjetaModeracion({ resena: r, onAprobar, onRetirar }: { resena: Resena; onAprobar: () => void; onRetirar: () => void }) {
  return (
    <article className="resumen-caja mb-3">
      <div className="d-flex justify-content-between flex-wrap gap-2">
        <span><strong>{r.autorNombre}</strong> en <Link to={`/libros/${r.libroId}`}>libro #{r.libroId}</Link> {r.puntuacion !== null && <Estrellas valor={r.puntuacion} tamano=".85rem" />}</span>
        <small className="text-muted">{fecha(r.fechaPublicacion)}</small>
      </div>
      <p className="my-2">{r.contenido}</p>
      <ul className="mb-2">
        {r.reportes.map((rep) => <li key={rep.id}><small>«{rep.motivo}» — {fecha(rep.fechaReporte)}</small></li>)}
      </ul>
      <div className="d-flex gap-2">
        <button type="button" className="btn" onClick={onAprobar}>Aprobar</button>
        <button type="button" className="btn btn-peligro" onClick={onRetirar}>Retirar</button>
      </div>
    </article>
  )
}
