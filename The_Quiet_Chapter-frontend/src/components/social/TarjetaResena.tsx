import { Estrellas } from '../comunes/Estrellas'
import { fecha } from '../../utils/formato'
import type { Resena } from '../../interfaces/social'

/**
 * Una resena publicada. Los botones dependen de quien la mira:
 * cliente ajeno -> util / reportar (HU-088/089); autor -> editar / eliminar (HU-086/087).
 */
export function TarjetaResena({ resena: r, puedeInteractuar, onUtil, onReportar, onEditar, onEliminar }: {
  resena: Resena
  /** Cliente con sesion (no admin): puede votar y reportar resenas ajenas. */
  puedeInteractuar: boolean
  onUtil: () => void
  onReportar: () => void
  onEditar: () => void
  onEliminar: () => void
}) {
  const votos = `${r.votosUtiles} ${r.votosUtiles === 1 ? 'persona encontró' : 'personas encontraron'} útil esta reseña`
  return (
    <article className="resena-tarjeta">
      <div className="resena-avatar" aria-hidden="true">{r.autorNombre.charAt(0).toUpperCase()}</div>
      <div>
        <div className="resena-cabecera">
          <span>
            <strong>{r.autorNombre}</strong> {r.puntuacion !== null && <Estrellas valor={r.puntuacion} tamano=".9rem" />}
            {r.propia && <span className="badge badge-oferta ms-2">Tu reseña</span>}
          </span>
          <span className="fecha">{fecha(r.fechaPublicacion)}{r.editada && ' · editada'}</span>
        </div>
        <p className="resena-texto">{r.contenido}</p>
        <div className="resena-acciones">
          <span>{votos}</span>
          {puedeInteractuar && !r.propia && (
            <>
              <button type="button" className="btn btn-secundario" aria-pressed={r.marcadaUtilPorMi} onClick={onUtil}>
                {r.marcadaUtilPorMi ? '✓ Útil' : '👍 Útil'}
              </button>
              <button type="button" className="btn btn-secundario" onClick={onReportar}>Reportar</button>
            </>
          )}
          {r.propia && (
            <>
              <button type="button" className="btn btn-secundario" onClick={onEditar}>Editar</button>
              <button type="button" className="btn btn-peligro" onClick={onEliminar}>Eliminar</button>
            </>
          )}
        </div>
      </div>
    </article>
  )
}
