import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { socialApi } from '../../api/social'
import { mensajeDe } from '../../api/http'
import { useAuth } from '../../context/auth'
import { useAccion } from '../../hooks/useAccion'
import { Estrellas } from '../comunes/Estrellas'
import { Avisos } from '../comunes/Avisos'
import { Modal } from '../comunes/Modal'
import { Confirmar } from '../comunes/Confirmar'
import { FormularioTexto } from './FormularioTexto'
import { TarjetaResena } from './TarjetaResena'
import { LIMITES, validarReporte, validarResena } from '../../utils/validaciones'
import type { OpinionesLibro, OrdenResenas, Resena } from '../../interfaces/social'
import estilos from './SeccionResenas.module.css'

/**
 * HU-080..091: calificacion, resenas, orden, filtro por estrellas, votos de utilidad, reportes y
 * edicion/eliminacion de la propia. Solo opina quien compro el libro (lo decide social-service: puedoOpinar).
 */
export function SeccionResenas({ libroId, onCambio }: { libroId: number; onCambio?: (o: OpinionesLibro) => void }) {
  const { usuario, esAdmin } = useAuth()
  const ubicacion = useLocation()
  const accion = useAccion()
  const { setError } = accion
  const [datos, setDatos] = useState<OpinionesLibro | null>(null)
  const [orden, setOrden] = useState<OrdenResenas>('MAS_RECIENTES')
  const [puntuacion, setPuntuacion] = useState<number | undefined>()
  const [estrellas, setEstrellas] = useState(0)
  const [editando, setEditando] = useState<Resena | null>(null)
  const [reportando, setReportando] = useState<Resena | null>(null)
  const [eliminando, setEliminando] = useState<Resena | null>(null)

  const cargar = useCallback(() => socialApi.opiniones(libroId, orden, puntuacion)
    .then((d) => { setDatos(d); onCambio?.(d) })
    .catch((e) => setError(mensajeDe(e))), [libroId, orden, puntuacion, onCambio, setError])

  useEffect(() => { cargar() }, [cargar])

  const propia = datos?.resenas.find((r) => r.propia)
  const puedeInteractuar = !!usuario && !esAdmin

  /** Toda accion sobre resenas termina recargando la seccion (promedio, votos y listado). */
  const ejecutar = (operacion: () => Promise<unknown>, mensajeOk?: string) => accion.ejecutar(operacion, mensajeOk, cargar)

  const publicar = async (texto: string) => {
    const ok = await ejecutar(() => socialApi.publicar(libroId, texto, estrellas || undefined), 'Reseña publicada')
    if (ok) setEstrellas(0)
    return ok
  }

  const calificar = (n: number) => {
    // Si ya califico (o ya tiene resena) la estrella se guarda al instante; si no, viaja junto a la resena.
    if (propia || (datos && datos.miCalificacion !== null)) ejecutar(() => socialApi.calificar(libroId, n), 'Calificación actualizada')
    else setEstrellas(n)
  }

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
      <Avisos estado={accion} />

      {!usuario && <p><Link to={`/login?redir=${encodeURIComponent(ubicacion.pathname)}`}>Inicia sesión</Link> para calificar o escribir una reseña.</p>}
      {puedeInteractuar && datos && !datos.puedoOpinar && <p className="text-muted">Solo quienes compraron este libro pueden calificarlo y reseñarlo.</p>}

      {puedeInteractuar && datos?.puedoOpinar && (
        <div className={estilos.caja}>
          <div className="mb-2">
            <span className="me-2">Tu calificación:</span>
            <Estrellas valor={datos.miCalificacion ?? estrellas} onElegir={calificar} />
          </div>
          {!propia && (
            <FormularioTexto id="nuevaResena" etiqueta="Escribe tu reseña" maximo={LIMITES.resenaMax} placeholder="¿Qué te pareció este libro?"
              textoBoton="Publicar reseña" validar={validarResena} onEnviar={publicar} />
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
        <TarjetaResena key={r.id} resena={r} puedeInteractuar={puedeInteractuar}
          onUtil={() => ejecutar(() => (r.marcadaUtilPorMi ? socialApi.quitarUtil(r.id) : socialApi.marcarUtil(r.id)))}
          onReportar={() => setReportando(r)} onEditar={() => setEditando(r)} onEliminar={() => setEliminando(r)} />
      ))}

      <Modal abierto={!!editando} titulo="Editar reseña" onCerrar={() => setEditando(null)}>
        {editando && (
          <FormularioTexto key={editando.id} id="editarResena" etiqueta="Tu reseña" inicial={editando.contenido} maximo={LIMITES.resenaMax}
            textoBoton="Guardar" validar={validarResena}
            onEnviar={async (t) => { const ok = await ejecutar(() => socialApi.editar(editando.id, t), 'Reseña actualizada'); if (ok) setEditando(null); return ok }} />
        )}
      </Modal>

      <Modal abierto={!!reportando} titulo="Reportar reseña" onCerrar={() => setReportando(null)}>
        {reportando && (
          <FormularioTexto key={reportando.id} id="motivoReporte" etiqueta={`Motivo (entre ${LIMITES.motivoReporteMin} y ${LIMITES.motivoReporteMax} caracteres)`}
            maximo={LIMITES.motivoReporteMax} textoBoton="Enviar reporte" peligro validar={validarReporte}
            onEnviar={async (t) => { const ok = await ejecutar(() => socialApi.reportar(reportando.id, t), 'Reporte enviado. Un administrador lo revisará.'); if (ok) setReportando(null); return ok }} />
        )}
      </Modal>

      <Confirmar abierto={!!eliminando} titulo="Eliminar reseña" mensaje="¿Seguro que quieres eliminar tu reseña? Esta acción no se puede deshacer."
        textoConfirmar="Eliminar" peligro onCancelar={() => setEliminando(null)}
        onConfirmar={() => { const r = eliminando!; setEliminando(null); ejecutar(() => socialApi.eliminar(r.id), 'Reseña eliminada') }} />
    </section>
  )
}
