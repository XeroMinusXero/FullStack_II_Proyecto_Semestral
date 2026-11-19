import { useCallback, useEffect, useState } from 'react'
import { checkoutApi } from '../../api/checkout'
import { mensajeDe } from '../../api/http'
import { useAccion } from '../../hooks/useAccion'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Avisos } from '../../components/comunes/Avisos'
import { Cargando } from '../../components/comunes/Cargando'
import { Modal } from '../../components/comunes/Modal'
import { EditorMetodoEnvio, type DatosMetodoEnvioForm } from '../../components/admin/EditorMetodoEnvio'
import { EditorMedioPago, type DatosMedioPagoForm } from '../../components/admin/EditorMedioPago'
import { clp } from '../../utils/formato'
import type { MedioPago, MetodoEnvio } from '../../interfaces/checkout'

const ENVIO_NUEVO: DatosMetodoEnvioForm = { nombre: '', costoBase: 0, plazoEstimadoDias: 3, activo: true, regionesHabilitadas: [] }
const MEDIO_NUEVO: DatosMedioPagoForm = { nombre: '', activo: true }

/** HU-024/025 (lado administrador): metodos de envio por region y medios de pago habilitados. */
export default function AdminOpciones() {
  const [envios, setEnvios] = useState<MetodoEnvio[] | null>(null)
  const [medios, setMedios] = useState<MedioPago[] | null>(null)
  const [editorEnvio, setEditorEnvio] = useState<{ id?: number; inicial: DatosMetodoEnvioForm } | null>(null)
  const [editorMedio, setEditorMedio] = useState<{ id?: number; inicial: DatosMedioPagoForm } | null>(null)
  const accion = useAccion()
  const { setError } = accion

  const cargar = useCallback(() => {
    checkoutApi.adminEnvios().then(setEnvios).catch((e) => setError(mensajeDe(e)))
    checkoutApi.adminMedios().then(setMedios).catch((e) => setError(mensajeDe(e)))
  }, [setError])
  useEffect(() => { cargar() }, [cargar])

  const guardarEnvio = async (m: DatosMetodoEnvioForm) => {
    const id = editorEnvio?.id
    const ok = await accion.ejecutar(() => (id ? checkoutApi.editarEnvio(id, m) : checkoutApi.crearEnvio(m)), 'Método de envío guardado', cargar)
    if (ok) setEditorEnvio(null)
    return ok
  }

  const guardarMedio = async (m: DatosMedioPagoForm) => {
    const id = editorMedio?.id
    const ok = await accion.ejecutar(() => (id ? checkoutApi.editarMedio(id, m) : checkoutApi.crearMedio(m)), 'Medio de pago guardado', cargar)
    if (ok) setEditorMedio(null)
    return ok
  }

  const estado = (activo: boolean) => <span className={`badge ${activo ? 'badge-disponible' : 'badge-agotado'}`}>{activo ? 'Activo' : 'Inactivo'}</span>

  return (
    <>
      <h1>Envíos y medios de pago</h1>
      <MenuAdmin />
      <Avisos estado={accion} />
      <div className="d-flex justify-content-between align-items-center mb-2">
        <h2 className="mb-0">Métodos de envío</h2>
        <button type="button" className="btn" onClick={() => setEditorEnvio({ inicial: ENVIO_NUEVO })}>+ Nuevo método</button>
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
                  <td>{estado(m.activo)}</td>
                  <td><button type="button" className="btn btn-secundario" onClick={() => setEditorEnvio({ id: m.id, inicial: { nombre: m.nombre, costoBase: m.costoBase, plazoEstimadoDias: m.plazoEstimadoDias, activo: m.activo, regionesHabilitadas: m.regionesHabilitadas } })}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <div className="d-flex justify-content-between align-items-center mb-2">
        <h2 className="mb-0">Medios de pago</h2>
        <button type="button" className="btn" onClick={() => setEditorMedio({ inicial: MEDIO_NUEVO })}>+ Nuevo medio</button>
      </div>
      {!medios ? <Cargando /> : (
        <div className="tabla-scroll">
          <table>
            <thead><tr><th>Nombre</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              {medios.map((m) => (
                <tr key={m.id}>
                  <td>{m.nombre}</td><td>{estado(m.activo)}</td>
                  <td><button type="button" className="btn btn-secundario" onClick={() => setEditorMedio({ id: m.id, inicial: { nombre: m.nombre, activo: m.activo } })}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal abierto={!!editorEnvio} titulo={editorEnvio?.id ? 'Editar método de envío' : 'Nuevo método de envío'} onCerrar={() => setEditorEnvio(null)}>
        {editorEnvio && <EditorMetodoEnvio key={editorEnvio.id ?? 'nuevo'} inicial={editorEnvio.inicial} onGuardar={guardarEnvio} />}
      </Modal>
      <Modal abierto={!!editorMedio} titulo={editorMedio?.id ? 'Editar medio de pago' : 'Nuevo medio de pago'} onCerrar={() => setEditorMedio(null)} ancho="sm">
        {editorMedio && <EditorMedioPago key={editorMedio.id ?? 'nuevo'} inicial={editorMedio.inicial} onGuardar={guardarMedio} />}
      </Modal>
    </>
  )
}
