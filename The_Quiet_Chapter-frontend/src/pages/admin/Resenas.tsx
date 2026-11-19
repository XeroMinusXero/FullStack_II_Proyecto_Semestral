import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { socialApi } from '../../api/social'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Avisos } from '../../components/comunes/Avisos'
import { TarjetaModeracion } from '../../components/admin/TarjetaModeracion'
import { useAccion } from '../../hooks/useAccion'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { claseEstado, fecha } from '../../utils/formato'
import type { PaginaResenas, Resena } from '../../interfaces/social'

/** HU-089..091: moderacion de resenas reportadas (aprobar o retirar) y listado general. */
export default function AdminResenas() {
  const [reportadas, setReportadas] = useState<Resena[] | null>(null)
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(0)
  const [todas, setTodas] = useState<PaginaResenas | null>(null)
  const accion = useAccion()
  const { setError } = accion

  const cargar = useCallback(() => {
    socialApi.reportadas().then(setReportadas).catch((e) => setError(mensajeDe(e)))
    socialApi.todas(estado || undefined, pagina).then(setTodas).catch((e) => setError(mensajeDe(e)))
  }, [estado, pagina, setError])
  useEffect(() => { cargar() }, [cargar])

  return (
    <>
      <h1>Moderación de reseñas</h1>
      <MenuAdmin />
      <Avisos estado={accion} />

      <h2>Reportadas</h2>
      {!reportadas ? <Cargando /> : reportadas.length === 0 ? <p className="text-muted">No hay reseñas pendientes de moderación.</p> : reportadas.map((r) => (
        <TarjetaModeracion key={r.id} resena={r}
          onAprobar={() => accion.ejecutar(() => socialApi.aprobar(r.id), 'Reseña aprobada: vuelve a estar publicada', cargar)}
          onRetirar={() => accion.ejecutar(() => socialApi.retirar(r.id), 'Reseña retirada', cargar)} />
      ))}

      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-4 mb-2">
        <h2 className="mb-0">Todas las reseñas</h2>
        <select className="form-select selector-corto" aria-label="Estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPagina(0) }}>
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
                    <td className="celda-texto"><small>{r.contenido}</small></td><td>{r.votosUtiles}</td>
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
