import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { socialApi } from '../../api/social'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Estrellas } from '../../components/comunes/Estrellas'
import { claseEstado, fecha } from '../../utils/formato'
import type { PaginaResenas, Resena } from '../../interfaces/social'

/** HU-089..091: moderacion de resenas reportadas (aprobar o retirar) y listado general. */
export default function AdminResenas() {
  const [reportadas, setReportadas] = useState<Resena[] | null>(null)
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(0)
  const [todas, setTodas] = useState<PaginaResenas | null>(null)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  const cargar = useCallback(() => {
    socialApi.reportadas().then(setReportadas).catch((e) => setError(mensajeDe(e)))
    socialApi.todas(estado || undefined, pagina).then(setTodas).catch((e) => setError(mensajeDe(e)))
  }, [estado, pagina])
  useEffect(() => { cargar() }, [cargar])

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); cargar() } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <h1>Moderación de reseñas</h1>
      <MenuAdmin />
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>

      <h2>Reportadas</h2>
      {!reportadas ? <Cargando /> : reportadas.length === 0 ? <p className="text-muted">No hay reseñas pendientes de moderación.</p> : reportadas.map((r) => (
        <div key={r.id} className="resumen-caja mb-3">
          <div className="d-flex justify-content-between flex-wrap gap-2">
            <span><strong>{r.autorNombre}</strong> en <Link to={`/libros/${r.libroId}`}>libro #{r.libroId}</Link> {r.puntuacion !== null && <Estrellas valor={r.puntuacion} tamano=".85rem" />}</span>
            <small className="text-muted">{fecha(r.fechaPublicacion)}</small>
          </div>
          <p className="my-2">{r.contenido}</p>
          <ul className="mb-2">
            {r.reportes.map((rep) => <li key={rep.id}><small>«{rep.motivo}» — {fecha(rep.fechaReporte)}</small></li>)}
          </ul>
          <div className="d-flex gap-2">
            <button type="button" className="btn" onClick={() => accion(() => socialApi.aprobar(r.id), 'Reseña aprobada: vuelve a estar publicada')}>Aprobar</button>
            <button type="button" className="btn btn-peligro" onClick={() => accion(() => socialApi.retirar(r.id), 'Reseña retirada')}>Retirar</button>
          </div>
        </div>
      ))}

      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-4 mb-2">
        <h2 className="mb-0">Todas las reseñas</h2>
        <select className="form-select" style={{ maxWidth: 220 }} aria-label="Estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPagina(0) }}>
          <option value="">Todos los estados</option>
          <option value="PUBLICADA">Publicadas</option><option value="REPORTADA">Reportadas</option>
          <option value="RETIRADA">Retiradas</option><option value="ELIMINADA_POR_AUTOR">Eliminadas por autor</option>
        </select>
      </div>
      {!todas ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Fecha</th><th>Autor</th><th>Libro</th><th>Reseña</th><th>Útil</th><th>Estado</th></tr></thead>
              <tbody>
                {todas.resenas.length === 0 && <tr><td colSpan={6}>Sin reseñas.</td></tr>}
                {todas.resenas.map((r) => (
                  <tr key={r.id}>
                    <td>{fecha(r.fechaPublicacion)}</td><td>{r.autorNombre}</td><td><Link to={`/libros/${r.libroId}`}>#{r.libroId}</Link></td>
                    <td style={{ maxWidth: 360 }}><small>{r.contenido}</small></td><td>{r.votosUtiles}</td>
                    <td><span className={`badge ${claseEstado(r.estado)}`}>{r.estado.replaceAll('_', ' ').toLowerCase()}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={todas.pagina} totalPaginas={todas.totalPaginas} onCambiar={setPagina} />
        </>
      )}
    </>
  )
}
