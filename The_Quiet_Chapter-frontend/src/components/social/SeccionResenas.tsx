import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { socialApi } from '../../api/social'
import { mensajeDe } from '../../api/http'
import { useAuth } from '../../context/auth'
import { Estrellas } from '../comunes/Estrellas'
import { Alerta } from '../comunes/Alerta'
import { Modal } from '../comunes/Modal'
import { Confirmar } from '../comunes/Confirmar'
import { fecha } from '../../utils/formato'
import type { OpinionesLibro, OrdenResenas, Resena } from '../../interfaces/social'
import estilos from './SeccionResenas.module.css'

/**
 * HU-080..091: calificacion, resenas, orden, filtro por estrellas, votos de utilidad, reportes y
 * edicion/eliminacion de la propia. Solo opina quien compro el libro (lo decide social-service: puedoOpinar).
 */
export function SeccionResenas({ libroId, onCambio }: { libroId: number; onCambio?: (o: OpinionesLibro) => void }) {
  const { usuario, esAdmin } = useAuth()
  const ubicacion = useLocation()
  const [datos, setDatos] = useState<OpinionesLibro | null>(null)
  const [orden, setOrden] = useState<OrdenResenas>('MAS_RECIENTES')
  const [puntuacion, setPuntuacion] = useState<number | undefined>()
  const [texto, setTexto] = useState('')
  const [estrellas, setEstrellas] = useState(0)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [editando, setEditando] = useState<Resena | null>(null)
  const [textoEdicion, setTextoEdicion] = useState('')
  const [reportando, setReportando] = useState<Resena | null>(null)
  const [motivo, setMotivo] = useState('')
  const [eliminando, setEliminando] = useState<Resena | null>(null)

  const cargar = useCallback(() => socialApi.opiniones(libroId, orden, puntuacion)
    .then((d) => { setDatos(d); onCambio?.(d) })
    .catch((e) => setError(mensajeDe(e))), [libroId, orden, puntuacion, onCambio])

  useEffect(() => { cargar() }, [cargar])

  const accion = async (fn: () => Promise<unknown>, ok?: string) => {
    setError(''); setExito('')
    try { await fn(); if (ok) setExito(ok); await cargar() } catch (e) { setError(mensajeDe(e)) }
  }

  const publicar = (e: FormEvent) => {
    e.preventDefault()
    const limpio = texto.trim()
    if (limpio.length < 10) { setError('La reseña debe tener al menos 10 caracteres'); return }
    if (limpio.length > 1000) { setError('La reseña no puede superar 1000 caracteres'); return }
    accion(async () => { await socialApi.publicar(libroId, limpio, estrellas || undefined); setTexto(''); setEstrellas(0) }, 'Reseña publicada')
  }

  const propia = datos?.resenas.find((r) => r.propia)

  return (
    <section className="seccion" id="resenas">
      <h2>Reseñas y calificaciones</h2>
      {datos && (
        <div className="resumen-calificacion">
          <span className="nota">{datos.promedio !== null ? datos.promedio.toFixed(1) : '—'}</span>
          <div>
            <Estrellas valor={datos.promedio} />
            <div className="text-muted small">
              {datos.cantidadCalificaciones === 0 ? 'Sin calificaciones aún' : `${datos.cantidadCalificaciones} ${datos.cantidadCalificaciones === 1 ? 'calificación' : 'calificaciones'}`}
            </div>
          </div>
        </div>
      )}
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>

      {!usuario && <p><Link to={`/login?redir=${encodeURIComponent(ubicacion.pathname)}`}>Inicia sesión</Link> para calificar o escribir una reseña.</p>}
      {usuario && !esAdmin && datos && !datos.puedoOpinar && <p className="text-muted">Solo quienes compraron este libro pueden calificarlo y reseñarlo.</p>}

      {usuario && !esAdmin && datos?.puedoOpinar && (
        <div className={estilos.caja}>
          <div className="mb-2">
            <span className="me-2">Tu calificación:</span>
            <Estrellas valor={datos.miCalificacion ?? estrellas} onElegir={(n) => (propia || datos.miCalificacion !== null
              ? accion(() => socialApi.calificar(libroId, n), 'Calificación actualizada')
              : setEstrellas(n))} />
          </div>
          {!propia && (
            <form className="ancho" onSubmit={publicar} style={{ boxShadow: 'none', padding: 0, background: 'none', border: 0 }}>
              <div className="campo">
                <label htmlFor="nuevaResena">Escribe tu reseña</label>
                <textarea id="nuevaResena" rows={4} maxLength={1000} value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="¿Qué te pareció este libro?" />
                <small className="text-muted">{texto.length}/1000</small>
              </div>
              <button className="btn" type="submit">Publicar reseña</button>
            </form>
          )}
        </div>
      )}

      <div className="d-flex flex-wrap gap-2 align-items-center my-3">
        <label htmlFor="ordenResenas" className="mb-0">Ordenar:</label>
        <select id="ordenResenas" className={estilos.selector} value={orden} onChange={(e) => setOrden(e.target.value as OrdenResenas)}>
          <option value="MAS_RECIENTES">Más recientes</option>
          <option value="MAS_UTILES">Más útiles</option>
          <option value="MEJOR_CALIFICADAS">Mejor calificadas</option>
        </select>
        <label htmlFor="filtroEstrellas" className="mb-0 ms-2">Estrellas:</label>
        <select id="filtroEstrellas" className={estilos.selector} value={puntuacion ?? ''} onChange={(e) => setPuntuacion(e.target.value ? Number(e.target.value) : undefined)}>
          <option value="">Todas</option>
          {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} ★</option>)}
        </select>
      </div>

      {datos && datos.resenas.length === 0 && <p className="text-muted">{puntuacion ? 'No hay reseñas con esa calificación.' : 'Aún no hay reseñas. ¡Sé el primero!'}</p>}
      {datos?.resenas.map((r) => (
        <div key={r.id} className="resena-tarjeta">
          <div className="resena-avatar" aria-hidden="true">{r.autorNombre.charAt(0).toUpperCase()}</div>
          <div>
          <div className="resena-cabecera">
            <span><strong>{r.autorNombre}</strong> {r.puntuacion !== null && <Estrellas valor={r.puntuacion} tamano=".9rem" />}{r.propia && <span className="badge badge-oferta ms-2">Tu reseña</span>}</span>
            <span className="fecha">{fecha(r.fechaPublicacion)}{r.editada && ' · editada'}</span>
          </div>
          <p className="resena-texto">{r.contenido}</p>
          <div className="resena-acciones">
            <span>{r.votosUtiles} {r.votosUtiles === 1 ? 'persona encontró' : 'personas encontraron'} útil esta reseña</span>
            {usuario && !esAdmin && !r.propia && (
              <>
                <button type="button" className="btn btn-secundario"
                  onClick={() => accion(() => (r.marcadaUtilPorMi ? socialApi.quitarUtil(r.id) : socialApi.marcarUtil(r.id)))}>
                  {r.marcadaUtilPorMi ? '✓ Útil' : '👍 Útil'}
                </button>
                <button type="button" className="btn btn-secundario" onClick={() => { setReportando(r); setMotivo('') }}>Reportar</button>
              </>
            )}
            {r.propia && (
              <>
                <button type="button" className="btn btn-secundario" onClick={() => { setEditando(r); setTextoEdicion(r.contenido) }}>Editar</button>
                <button type="button" className="btn btn-peligro" onClick={() => setEliminando(r)}>Eliminar</button>
              </>
            )}
          </div>
          </div>
        </div>
      ))}

      <Modal abierto={!!editando} titulo="Editar reseña" onCerrar={() => setEditando(null)}
        pie={<button className="btn" onClick={() => {
          const t = textoEdicion.trim()
          if (t.length < 10) { setError('La reseña debe tener al menos 10 caracteres'); return }
          const r = editando!
          setEditando(null)
          accion(() => socialApi.editar(r.id, t), 'Reseña actualizada')
        }}>Guardar</button>}>
        <textarea className="form-control" rows={5} maxLength={1000} value={textoEdicion} onChange={(e) => setTextoEdicion(e.target.value)} />
      </Modal>

      <Modal abierto={!!reportando} titulo="Reportar reseña" onCerrar={() => setReportando(null)}
        pie={<button className="btn btn-peligro" disabled={motivo.trim().length < 5} onClick={() => {
          const r = reportando!
          setReportando(null)
          accion(() => socialApi.reportar(r.id, motivo.trim()), 'Reporte enviado. Un administrador lo revisará.')
        }}>Enviar reporte</button>}>
        <label htmlFor="motivo" className="form-label">Motivo (mínimo 5 caracteres)</label>
        <textarea id="motivo" className="form-control" rows={3} maxLength={300} value={motivo} onChange={(e) => setMotivo(e.target.value)} />
      </Modal>

      <Confirmar abierto={!!eliminando} titulo="Eliminar reseña" mensaje="¿Seguro que quieres eliminar tu reseña? Esta acción no se puede deshacer."
        textoConfirmar="Eliminar" peligro onCancelar={() => setEliminando(null)}
        onConfirmar={() => { const r = eliminando!; setEliminando(null); accion(() => socialApi.eliminar(r.id), 'Reseña eliminada') }} />
    </section>
  )
}
