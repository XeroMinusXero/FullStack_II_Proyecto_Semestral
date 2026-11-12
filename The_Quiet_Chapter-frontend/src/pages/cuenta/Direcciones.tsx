import { useEffect, useState, type FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { mensajeDe } from '../../api/http'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Campo } from '../../components/comunes/Campo'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Confirmar } from '../../components/comunes/Confirmar'
import { REGIONES } from '../../utils/regiones'
import { validarDireccion, type Errores } from '../../utils/validaciones'
import type { Direccion, DireccionSolicitud } from '../../interfaces/usuario'

const VACIA: DireccionSolicitud = { calle: '', numero: '', comuna: '', region: '' }

/** misDirecciones.html -> HU-044: CRUD de direcciones y predeterminada. */
export default function Direcciones() {
  const [lista, setLista] = useState<Direccion[] | null>(null)
  const [form, setForm] = useState<DireccionSolicitud>(VACIA)
  const [editando, setEditando] = useState<number | null>(null)
  const [errores, setErrores] = useState<Errores<DireccionSolicitud>>({})
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [eliminar, setEliminar] = useState<Direccion | null>(null)

  const cargar = () => usuariosApi.direcciones().then(setLista).catch((e) => setError(mensajeDe(e)))
  useEffect(() => { cargar() }, [])

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); await cargar() } catch (e) { setError(mensajeDe(e)) }
  }

  const guardar = (e: FormEvent) => {
    e.preventDefault()
    const err = validarDireccion(form)
    setErrores(err)
    if (Object.keys(err).length) return
    const datos = { calle: form.calle.trim(), numero: form.numero.trim(), comuna: form.comuna.trim(), region: form.region }
    accion(async () => {
      if (editando) await usuariosApi.editarDireccion(editando, datos)
      else await usuariosApi.agregarDireccion(datos)
      setForm(VACIA); setEditando(null)
    }, editando ? 'Dirección actualizada' : 'Dirección agregada')
  }

  const campo = (k: keyof DireccionSolicitud) => ({
    value: form[k], error: errores[k], onChange: (e: { target: { value: string } }) => setForm({ ...form, [k]: e.target.value }),
  })

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <div className="row g-4">
        <div className="col-lg-7">
          <h2>Mis direcciones</h2>
          {!lista ? <Cargando /> : lista.length === 0 ? <p className="text-muted">No tienes direcciones guardadas.</p> : lista.map((d) => (
            <div key={d.id} className="resumen-caja mb-3">
              <div className="d-flex justify-content-between flex-wrap gap-2">
                <div>
                  <strong>{d.calle} {d.numero}</strong><br />{d.comuna}, {d.region}
                  {d.predeterminada && <div><span className="badge badge-disponible mt-1">Predeterminada</span></div>}
                </div>
                <div className="d-flex flex-wrap gap-2 align-items-start">
                  {!d.predeterminada && <button type="button" className="btn btn-secundario" onClick={() => accion(() => usuariosApi.predeterminada(d.id), 'Dirección predeterminada actualizada')}>Predeterminar</button>}
                  <button type="button" className="btn btn-secundario" onClick={() => { setEditando(d.id); setForm({ calle: d.calle, numero: d.numero, comuna: d.comuna, region: d.region }); setErrores({}) }}>Editar</button>
                  <button type="button" className="btn btn-peligro" onClick={() => setEliminar(d)}>Eliminar</button>
                </div>
              </div>
            </div>
          ))}
        </div>
        <div className="col-lg-5">
          <form onSubmit={guardar} noValidate className="ancho">
            <h2>{editando ? 'Editar dirección' : 'Nueva dirección'}</h2>
            <Campo id="calle" etiqueta="Calle" maxLength={120} {...campo('calle')} />
            <Campo id="numero" etiqueta="Número / Depto" maxLength={30} {...campo('numero')} />
            <Campo id="comuna" etiqueta="Comuna" maxLength={80} {...campo('comuna')} />
            <div className={`campo${errores.region ? ' error' : ''}`}>
              <label htmlFor="region">Región</label>
              <select id="region" value={form.region} onChange={(e) => setForm({ ...form, region: e.target.value })}>
                <option value="">Seleccione una región</option>
                {REGIONES.map((r) => <option key={r} value={r}>{r}</option>)}
              </select>
              <span className="mensaje-error">{errores.region}</span>
            </div>
            <div className="d-flex gap-2">
              <button className="btn" type="submit">{editando ? 'Guardar cambios' : 'Agregar dirección'}</button>
              {editando && <button type="button" className="btn btn-secundario" onClick={() => { setEditando(null); setForm(VACIA); setErrores({}) }}>Cancelar</button>}
            </div>
          </form>
        </div>
      </div>
      <Confirmar abierto={!!eliminar} titulo="Eliminar dirección" mensaje="¿Seguro que deseas eliminar esta dirección?" textoConfirmar="Eliminar"
        onCancelar={() => setEliminar(null)} onConfirmar={() => { const d = eliminar!; setEliminar(null); accion(() => usuariosApi.eliminarDireccion(d.id), 'Dirección eliminada') }} />
    </>
  )
}
