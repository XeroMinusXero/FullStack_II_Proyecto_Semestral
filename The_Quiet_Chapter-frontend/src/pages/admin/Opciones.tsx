import { useCallback, useEffect, useState } from 'react'
import { checkoutApi } from '../../api/checkout'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Modal } from '../../components/comunes/Modal'
import { REGIONES } from '../../utils/regiones'
import { clp } from '../../utils/formato'
import type { MedioPago, MetodoEnvio } from '../../interfaces/checkout'

type FormEnvio = Omit<MetodoEnvio, 'id'> & { id?: number }

/** HU-024/025 (lado administrador): metodos de envio por region y medios de pago habilitados. */
export default function AdminOpciones() {
  const [envios, setEnvios] = useState<MetodoEnvio[] | null>(null)
  const [medios, setMedios] = useState<MedioPago[] | null>(null)
  const [formEnvio, setFormEnvio] = useState<FormEnvio | null>(null)
  const [formMedio, setFormMedio] = useState<{ id?: number; nombre: string; activo: boolean } | null>(null)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  const cargar = useCallback(() => {
    checkoutApi.adminEnvios().then(setEnvios).catch((e) => setError(mensajeDe(e)))
    checkoutApi.adminMedios().then(setMedios).catch((e) => setError(mensajeDe(e)))
  }, [])
  useEffect(() => { cargar() }, [cargar])

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); cargar(); return true } catch (e) { setError(mensajeDe(e)); return false }
  }

  const guardarEnvio = async () => {
    if (!formEnvio) return
    if (!formEnvio.nombre.trim() || formEnvio.costoBase < 0 || formEnvio.plazoEstimadoDias < 0) { setError('Revisa nombre, costo y plazo'); return }
    const { id, ...m } = formEnvio
    if (await accion(() => (id ? checkoutApi.editarEnvio(id, m) : checkoutApi.crearEnvio(m)), 'Método de envío guardado')) setFormEnvio(null)
  }

  const guardarMedio = async () => {
    if (!formMedio || !formMedio.nombre.trim()) { setError('El nombre es obligatorio'); return }
    const { id, ...m } = formMedio
    if (await accion(() => (id ? checkoutApi.editarMedio(id, m) : checkoutApi.crearMedio(m)), 'Medio de pago guardado')) setFormMedio(null)
  }

  return (
    <>
      <h1>Envíos y medios de pago</h1>
      <MenuAdmin />
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <div className="d-flex justify-content-between align-items-center mb-2">
        <h2 className="mb-0">Métodos de envío</h2>
        <button type="button" className="btn" onClick={() => setFormEnvio({ nombre: '', costoBase: 0, plazoEstimadoDias: 3, activo: true, regionesHabilitadas: [] })}>+ Nuevo método</button>
      </div>
      {!envios ? <Cargando /> : (
        <div className="tabla-scroll mb-4">
          <table>
            <thead><tr><th>Nombre</th><th>Costo</th><th>Plazo</th><th>Regiones</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              {envios.map((m) => (
                <tr key={m.id}>
                  <td>{m.nombre}</td><td>{clp(m.costoBase)}</td><td>{m.plazoEstimadoDias} días</td>
                  <td><small>{m.regionesHabilitadas.length ? m.regionesHabilitadas.join(', ') : 'Todas'}</small></td>
                  <td><span className={`badge ${m.activo ? 'badge-disponible' : 'badge-agotado'}`}>{m.activo ? 'Activo' : 'Inactivo'}</span></td>
                  <td><button type="button" className="btn btn-secundario" onClick={() => setFormEnvio({ ...m })}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <div className="d-flex justify-content-between align-items-center mb-2">
        <h2 className="mb-0">Medios de pago</h2>
        <button type="button" className="btn" onClick={() => setFormMedio({ nombre: '', activo: true })}>+ Nuevo medio</button>
      </div>
      {!medios ? <Cargando /> : (
        <div className="tabla-scroll">
          <table>
            <thead><tr><th>Nombre</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              {medios.map((m) => (
                <tr key={m.id}>
                  <td>{m.nombre}</td>
                  <td><span className={`badge ${m.activo ? 'badge-disponible' : 'badge-agotado'}`}>{m.activo ? 'Activo' : 'Inactivo'}</span></td>
                  <td><button type="button" className="btn btn-secundario" onClick={() => setFormMedio({ ...m })}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal abierto={!!formEnvio} titulo={formEnvio?.id ? 'Editar método de envío' : 'Nuevo método de envío'} onCerrar={() => setFormEnvio(null)}
        pie={<button type="button" className="btn" onClick={guardarEnvio}>Guardar</button>}>
        {formEnvio && (
          <>
            <label className="form-label" htmlFor="enom">Nombre</label>
            <input id="enom" className="form-control mb-2" maxLength={120} value={formEnvio.nombre} onChange={(e) => setFormEnvio({ ...formEnvio, nombre: e.target.value })} />
            <div className="row g-2 mb-2">
              <div className="col"><label className="form-label" htmlFor="ecosto">Costo (CLP)</label><input id="ecosto" type="number" min={0} className="form-control" value={formEnvio.costoBase} onChange={(e) => setFormEnvio({ ...formEnvio, costoBase: Number(e.target.value) })} /></div>
              <div className="col"><label className="form-label" htmlFor="eplazo">Plazo (días)</label><input id="eplazo" type="number" min={0} className="form-control" value={formEnvio.plazoEstimadoDias} onChange={(e) => setFormEnvio({ ...formEnvio, plazoEstimadoDias: Number(e.target.value) })} /></div>
            </div>
            <label className="form-label">Regiones habilitadas <small className="text-muted">(ninguna = todas)</small></label>
            <div className="d-flex flex-wrap gap-2 mb-2">
              {REGIONES.map((r) => {
                const sel = formEnvio.regionesHabilitadas.includes(r)
                return (
                  <div key={r} className="form-check">
                    <input className="form-check-input" type="checkbox" id={`reg-${r}`} checked={sel}
                      onChange={() => setFormEnvio({ ...formEnvio, regionesHabilitadas: sel ? formEnvio.regionesHabilitadas.filter((x) => x !== r) : [...formEnvio.regionesHabilitadas, r] })} />
                    <label className="form-check-label" htmlFor={`reg-${r}`}><small>{r}</small></label>
                  </div>
                )
              })}
            </div>
            <div className="form-check"><input className="form-check-input" type="checkbox" id="eact" checked={formEnvio.activo} onChange={(e) => setFormEnvio({ ...formEnvio, activo: e.target.checked })} /><label className="form-check-label" htmlFor="eact">Activo</label></div>
          </>
        )}
      </Modal>
      <Modal abierto={!!formMedio} titulo={formMedio?.id ? 'Editar medio de pago' : 'Nuevo medio de pago'} onCerrar={() => setFormMedio(null)} ancho="sm"
        pie={<button type="button" className="btn" onClick={guardarMedio}>Guardar</button>}>
        {formMedio && (
          <>
            <label className="form-label" htmlFor="mnom">Nombre</label>
            <input id="mnom" className="form-control mb-2" maxLength={120} value={formMedio.nombre} onChange={(e) => setFormMedio({ ...formMedio, nombre: e.target.value })} />
            <div className="form-check"><input className="form-check-input" type="checkbox" id="mact" checked={formMedio.activo} onChange={(e) => setFormMedio({ ...formMedio, activo: e.target.checked })} /><label className="form-check-label" htmlFor="mact">Activo</label></div>
          </>
        )}
      </Modal>
    </>
  )
}
