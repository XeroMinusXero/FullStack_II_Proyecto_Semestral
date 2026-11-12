import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { notifApi } from '../../api/notif'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Cargando } from '../../components/comunes/Cargando'
import { Alerta } from '../../components/comunes/Alerta'
import { Paginacion } from '../../components/comunes/Paginacion'
import { claseEstado, fecha } from '../../utils/formato'
import type { AvisoReabastecimiento, Notificacion } from '../../interfaces/notif'
import type { Pagina } from '../../interfaces/promo'

/** Bandeja de correos simulados (notif-service) y avisos de reabastecimiento activos (HU-104/105). */
export default function Notificaciones() {
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Notificacion> | null>(null)
  const [avisos, setAvisos] = useState<(AvisoReabastecimiento & { titulo?: string })[]>([])
  const [abierta, setAbierta] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => { notifApi.mias(pagina).then(setDatos).catch((e) => setError(mensajeDe(e))) }, [pagina])

  const cargarAvisos = () => notifApi.misAvisos()
    .then((todos) => {
      const a = todos.filter((x) => x.activa)
      return catalogoApi.condiciones(a.map((x) => x.libroId)).catch(() => [])
        .then((cond) => a.map((x) => ({ ...x, titulo: cond.find((c) => c.id === x.libroId)?.titulo })))
    })
    .then(setAvisos)
  useEffect(() => { cargarAvisos().catch(() => undefined) }, [])

  const enlace = (url: string | null) => {
    if (!url) return null
    try { const u = new URL(url, window.location.origin); return u.pathname + u.search } catch { return null }
  }

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <Alerta tipo="error">{error}</Alerta>
      {avisos.length > 0 && (
        <section className="seccion">
          <h2>Avisos de disponibilidad</h2>
          {avisos.map((a) => (
            <div key={a.id} className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
              <span><Link to={`/libros/${a.libroId}`}>{a.titulo ?? `Libro #${a.libroId}`}</Link> <small className="text-muted">desde {fecha(a.fechaSolicitud)}</small></span>
              <button type="button" className="btn btn-secundario" onClick={() => notifApi.cancelarAviso(a.libroId).then(cargarAvisos).catch((e) => setError(mensajeDe(e)))}>Cancelar aviso</button>
            </div>
          ))}
        </section>
      )}
      <h2>Mis correos</h2>
      <p className="text-muted">Copia de los correos que te envió la librería (envío simulado).</p>
      {!datos && !error && <Cargando />}
      {datos && datos.contenido.length === 0 && <p className="text-muted">No tienes correos todavía.</p>}
      {datos?.contenido.map((n) => {
        const ruta = enlace(n.enlaceAccion)
        return (
          <div key={n.id} className="resumen-caja mb-2">
            <button type="button" className="w-100 text-start bg-transparent border-0 p-0 d-flex justify-content-between flex-wrap gap-2" aria-expanded={abierta === n.id} onClick={() => setAbierta(abierta === n.id ? null : n.id)}>
              <strong>{n.asunto}</strong>
              <span><span className={`badge ${claseEstado(n.estado)}`}>{n.tipo.replaceAll('_', ' ').toLowerCase()}</span> <small className="text-muted">{fecha(n.fechaCreacion, true)}</small></span>
            </button>
            {abierta === n.id && (
              <div className="mt-2">
                <p style={{ whiteSpace: 'pre-line' }} className="mb-2">{n.mensaje}</p>
                {ruta && <Link to={ruta} className="btn btn-secundario">Abrir enlace</Link>}
              </div>
            )}
          </div>
        )
      })}
      {datos && <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />}
    </>
  )
}
