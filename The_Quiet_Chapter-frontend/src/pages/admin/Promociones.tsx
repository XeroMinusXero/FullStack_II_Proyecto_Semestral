import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { promoApi } from '../../api/promo'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { Confirmar } from '../../components/comunes/Confirmar'
import { clp, fecha } from '../../utils/formato'
import type { Alcance, Canje, Cupon, CuponSolicitud, Pagina, Promocion, PromocionSolicitud, TipoAlcance, TipoDescuento } from '../../interfaces/promo'
import type { Clasificacion } from '../../interfaces/catalogo'

const local = (v?: string) => (v ? v.slice(0, 16) : '')
const ahora = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16)
const enUnMes = () => new Date(Date.now() + 30 * 86400000 - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16)
const descuento = (tipo: TipoDescuento, valor: number) => (tipo === 'PORCENTAJE' ? `${valor}%` : clp(valor))

/** HU-092..099 (cupones) y HU-111..113 (promociones por libro, categoria, genero o editorial). */
export default function AdminPromociones() {
  const [pestana, setPestana] = useState<'cupones' | 'promociones'>('cupones')
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  return (
    <>
      <h1>Cupones y promociones</h1>
      <MenuAdmin />
      <div className="tabs" role="tablist">
        <button type="button" role="tab" aria-selected={pestana === 'cupones'} className={pestana === 'cupones' ? 'activo' : undefined} onClick={() => setPestana('cupones')}>Cupones</button>
        <button type="button" role="tab" aria-selected={pestana === 'promociones'} className={pestana === 'promociones' ? 'activo' : undefined} onClick={() => setPestana('promociones')}>Promociones</button>
      </div>
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      {pestana === 'cupones' ? <Cupones setError={setError} setExito={setExito} /> : <Promociones setError={setError} setExito={setExito} />}
    </>
  )
}

interface Avisos { setError: (m: string) => void; setExito: (m: string) => void }

function Cupones({ setError, setExito }: Avisos) {
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Cupon> | null>(null)
  const [form, setForm] = useState<(CuponSolicitud & { id?: number }) | null>(null)
  const [canjes, setCanjes] = useState<{ cupon: Cupon; lista: Canje[] } | null>(null)
  const [eliminar, setEliminar] = useState<Cupon | null>(null)

  const cargar = useCallback(() => { promoApi.cupones(pagina).then(setDatos).catch((e) => setError(mensajeDe(e))) }, [pagina, setError])
  useEffect(() => { cargar() }, [cargar])

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); cargar(); return true } catch (e) { setError(mensajeDe(e)); return false }
  }

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form) return
    if (form.codigo.trim().length < 3) { setError('El código debe tener al menos 3 caracteres'); return }
    if (!(form.valor > 0) || (form.tipoDescuento === 'PORCENTAJE' && form.valor > 100)) { setError('Valor de descuento inválido'); return }
    if (form.fechaFin <= form.fechaInicio) { setError('La fecha de término debe ser posterior al inicio'); return }
    const { id, ...s } = form
    const ok = await accion(() => (id ? promoApi.editarCupon(id, { ...s, codigo: s.codigo.trim().toUpperCase() }) : promoApi.crearCupon({ ...s, codigo: s.codigo.trim().toUpperCase() })), id ? 'Cupón actualizado' : 'Cupón creado')
    if (ok) setForm(null)
  }

  return (
    <>
      <div className="text-end mb-2">
        <button type="button" className="btn" onClick={() => setForm({ codigo: '', tipoDescuento: 'PORCENTAJE', valor: 10, montoMinimo: 0, fechaInicio: ahora(), fechaFin: enUnMes() })}>+ Nuevo cupón</button>
      </div>
      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Código</th><th>Descuento</th><th>Mínimo</th><th>Vigencia</th><th>Canjes</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.contenido.length === 0 && <tr><td colSpan={7}>No hay cupones.</td></tr>}
                {datos.contenido.map((c) => (
                  <tr key={c.id}>
                    <td><strong>{c.codigo}</strong></td><td>{descuento(c.tipoDescuento, c.valor)}</td><td>{clp(c.montoMinimo)}</td>
                    <td>{fecha(c.fechaInicio)} – {fecha(c.fechaFin)} {c.vigente ? <span className="badge badge-disponible">vigente</span> : <span className="badge badge-agotado">no vigente</span>}</td>
                    <td><button type="button" className="btn btn-secundario py-0" onClick={() => promoApi.canjes(c.id).then((lista) => setCanjes({ cupon: c, lista })).catch((e) => setError(mensajeDe(e)))}>{c.canjes}</button></td>
                    <td>
                      <div className="form-check form-switch">
                        <input className="form-check-input" type="checkbox" role="switch" id={`cup${c.id}`} checked={c.activo} onChange={() => accion(() => promoApi.estadoCupon(c.id, !c.activo), c.activo ? 'Cupón desactivado' : 'Cupón activado')} />
                        <label className="form-check-label" htmlFor={`cup${c.id}`}>{c.activo ? 'Activo' : 'Inactivo'}</label>
                      </div>
                    </td>
                    <td className="d-flex gap-1">
                      <button type="button" className="btn btn-secundario" onClick={() => setForm({ id: c.id, codigo: c.codigo, tipoDescuento: c.tipoDescuento, valor: c.valor, montoMinimo: c.montoMinimo, fechaInicio: local(c.fechaInicio), fechaFin: local(c.fechaFin) })}>Editar</button>
                      <button type="button" className="btn btn-peligro" onClick={() => setEliminar(c)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!form} titulo={form?.id ? 'Editar cupón' : 'Nuevo cupón'} onCerrar={() => setForm(null)}>
        {form && (
          <form onSubmit={guardar} noValidate className="ancho" style={{ boxShadow: 'none', padding: 0, border: 0 }}>
            <div className="row g-2">
              <div className="col-md-6 campo"><label htmlFor="codigo">Código</label><input id="codigo" maxLength={40} value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value.toUpperCase() })} /></div>
              <div className="col-md-6 campo"><label htmlFor="tipo">Tipo</label>
                <select id="tipo" value={form.tipoDescuento} onChange={(e) => setForm({ ...form, tipoDescuento: e.target.value as TipoDescuento })}>
                  <option value="PORCENTAJE">Porcentaje</option><option value="MONTO_FIJO">Monto fijo</option>
                </select></div>
              <div className="col-md-6 campo"><label htmlFor="valor">Valor {form.tipoDescuento === 'PORCENTAJE' ? '(%)' : '(CLP)'}</label><input id="valor" type="number" min={1} value={form.valor} onChange={(e) => setForm({ ...form, valor: Number(e.target.value) })} /></div>
              <div className="col-md-6 campo"><label htmlFor="minimo">Monto mínimo de compra</label><input id="minimo" type="number" min={0} value={form.montoMinimo} onChange={(e) => setForm({ ...form, montoMinimo: Number(e.target.value) })} /></div>
              <div className="col-md-6 campo"><label htmlFor="ini">Inicio</label><input id="ini" type="datetime-local" value={form.fechaInicio} onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })} /></div>
              <div className="col-md-6 campo"><label htmlFor="fin">Término</label><input id="fin" type="datetime-local" value={form.fechaFin} onChange={(e) => setForm({ ...form, fechaFin: e.target.value })} /></div>
            </div>
            <div className="text-end"><button className="btn" type="submit">Guardar</button></div>
          </form>
        )}
      </Modal>

      <Modal abierto={!!canjes} titulo={`Canjes de ${canjes?.cupon.codigo ?? ''}`} onCerrar={() => setCanjes(null)}>
        <div className="tabla-scroll">
          <table>
            <thead><tr><th>Fecha</th><th>Cliente</th><th>Pedido</th><th>Descuento</th></tr></thead>
            <tbody>
              {canjes?.lista.length === 0 && <tr><td colSpan={4}>Sin canjes.</td></tr>}
              {canjes?.lista.map((c) => <tr key={c.id}><td>{fecha(c.fechaCanje, true)}</td><td>#{c.clienteId}</td><td>#{c.pedidoId}</td><td>{clp(c.montoDescuento)}</td></tr>)}
            </tbody>
          </table>
        </div>
      </Modal>

      <Confirmar abierto={!!eliminar} titulo="Eliminar cupón" mensaje={`¿Eliminar el cupón ${eliminar?.codigo}? Si ya tiene canjes, desactívalo en su lugar.`} textoConfirmar="Eliminar"
        onCancelar={() => setEliminar(null)} onConfirmar={() => { const c = eliminar!; setEliminar(null); accion(() => promoApi.eliminarCupon(c.id), 'Cupón eliminado') }} />
    </>
  )
}

const ALCANCES: [TipoAlcance, string][] = [['LIBRO', 'Libro'], ['CATEGORIA', 'Categoría'], ['GENERO', 'Género'], ['EDITORIAL', 'Editorial']]

function Promociones({ setError, setExito }: Avisos) {
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Promocion> | null>(null)
  const [form, setForm] = useState<(PromocionSolicitud & { id?: number }) | null>(null)
  const [eliminar, setEliminar] = useState<Promocion | null>(null)
  const [opciones, setOpciones] = useState<Record<TipoAlcance, Clasificacion[]>>({ LIBRO: [], CATEGORIA: [], GENERO: [], EDITORIAL: [] })
  const [nuevoAlcance, setNuevoAlcance] = useState<{ tipo: TipoAlcance; id: string }>({ tipo: 'CATEGORIA', id: '' })

  const cargar = useCallback(() => { promoApi.promociones(pagina).then(setDatos).catch((e) => setError(mensajeDe(e))) }, [pagina, setError])
  useEffect(() => { cargar() }, [cargar])
  useEffect(() => {
    Promise.all([
      catalogoApi.mantenedor({ estado: 'ACTIVOS', tamanio: 100 }).then((p) => p.libros.map((l) => ({ id: l.id, nombre: l.titulo }))),
      catalogoApi.categorias(), catalogoApi.generos(), catalogoApi.editoriales(),
    ]).then(([LIBRO, CATEGORIA, GENERO, EDITORIAL]) => setOpciones({ LIBRO, CATEGORIA, GENERO, EDITORIAL })).catch(() => undefined)
  }, [])

  const nombre = (a: Alcance) => opciones[a.tipo].find((o) => o.id === a.referenciaId)?.nombre ?? `#${a.referenciaId}`

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); cargar(); return true } catch (e) { setError(mensajeDe(e)); return false }
  }

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form) return
    if (!form.nombre.trim()) { setError('El nombre es obligatorio'); return }
    if (!(form.valor > 0) || (form.tipoDescuento === 'PORCENTAJE' && form.valor >= 100)) { setError('Valor de descuento inválido'); return }
    if (form.fechaFin <= form.fechaInicio) { setError('La fecha de término debe ser posterior al inicio'); return }
    if (!form.alcances.length) { setError('Agrega al menos un alcance (libro, categoría, género o editorial)'); return }
    const { id, ...s } = form
    const ok = await accion(() => (id ? promoApi.editarPromocion(id, s) : promoApi.crearPromocion(s)), id ? 'Promoción actualizada' : 'Promoción creada')
    if (ok) setForm(null)
  }

  return (
    <>
      <div className="text-end mb-2">
        <button type="button" className="btn" onClick={() => setForm({ nombre: '', tipoDescuento: 'PORCENTAJE', valor: 15, fechaInicio: ahora(), fechaFin: enUnMes(), alcances: [], activo: true })}>+ Nueva promoción</button>
      </div>
      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Nombre</th><th>Descuento</th><th>Alcance</th><th>Vigencia</th><th>Aplicaciones</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.contenido.length === 0 && <tr><td colSpan={7}>No hay promociones.</td></tr>}
                {datos.contenido.map((p) => (
                  <tr key={p.id}>
                    <td><strong>{p.nombre}</strong></td><td>{descuento(p.tipoDescuento, p.valor)}</td>
                    <td><small>{p.alcances.map((a) => `${a.tipo.toLowerCase()}: ${nombre(a)}`).join(' · ')}</small></td>
                    <td>{fecha(p.fechaInicio)} – {fecha(p.fechaFin)} {p.vigente && <span className="badge badge-disponible">vigente</span>}</td>
                    <td>{p.aplicaciones}</td>
                    <td>
                      <div className="form-check form-switch">
                        <input className="form-check-input" type="checkbox" role="switch" id={`pro${p.id}`} checked={p.activo} onChange={() => accion(() => promoApi.estadoPromocion(p.id, !p.activo), p.activo ? 'Promoción desactivada' : 'Promoción activada')} />
                        <label className="form-check-label" htmlFor={`pro${p.id}`}>{p.activo ? 'Activa' : 'Inactiva'}</label>
                      </div>
                    </td>
                    <td className="d-flex gap-1">
                      <button type="button" className="btn btn-secundario" onClick={() => setForm({ id: p.id, nombre: p.nombre, tipoDescuento: p.tipoDescuento, valor: p.valor, fechaInicio: local(p.fechaInicio), fechaFin: local(p.fechaFin), alcances: p.alcances, activo: p.activo })}>Editar</button>
                      <button type="button" className="btn btn-peligro" onClick={() => setEliminar(p)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!form} titulo={form?.id ? 'Editar promoción' : 'Nueva promoción'} onCerrar={() => setForm(null)} ancho="xl">
        {form && (
          <form onSubmit={guardar} noValidate className="ancho" style={{ boxShadow: 'none', padding: 0, border: 0 }}>
            <div className="row g-2">
              <div className="col-md-6 campo"><label htmlFor="pnombre">Nombre</label><input id="pnombre" maxLength={120} value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} /></div>
              <div className="col-md-3 campo"><label htmlFor="ptipo">Tipo</label>
                <select id="ptipo" value={form.tipoDescuento} onChange={(e) => setForm({ ...form, tipoDescuento: e.target.value as TipoDescuento })}>
                  <option value="PORCENTAJE">Porcentaje</option><option value="MONTO_FIJO">Monto fijo</option>
                </select></div>
              <div className="col-md-3 campo"><label htmlFor="pvalor">Valor</label><input id="pvalor" type="number" min={1} value={form.valor} onChange={(e) => setForm({ ...form, valor: Number(e.target.value) })} /></div>
              <div className="col-md-6 campo"><label htmlFor="pini">Inicio</label><input id="pini" type="datetime-local" value={form.fechaInicio} onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })} /></div>
              <div className="col-md-6 campo"><label htmlFor="pfin">Término</label><input id="pfin" type="datetime-local" value={form.fechaFin} onChange={(e) => setForm({ ...form, fechaFin: e.target.value })} /></div>
            </div>
            <label className="d-block mb-1">Alcance</label>
            <div className="d-flex gap-2 flex-wrap mb-2">
              <select className="form-select" style={{ maxWidth: 150 }} aria-label="Tipo de alcance" value={nuevoAlcance.tipo} onChange={(e) => setNuevoAlcance({ tipo: e.target.value as TipoAlcance, id: '' })}>
                {ALCANCES.map(([v, t]) => <option key={v} value={v}>{t}</option>)}
              </select>
              <select className="form-select" style={{ flex: 1, minWidth: 200 }} aria-label="Elemento" value={nuevoAlcance.id} onChange={(e) => setNuevoAlcance({ ...nuevoAlcance, id: e.target.value })}>
                <option value="">Selecciona…</option>
                {opciones[nuevoAlcance.tipo].map((o) => <option key={o.id} value={o.id}>{o.nombre}</option>)}
              </select>
              <button type="button" className="btn btn-secundario" disabled={!nuevoAlcance.id} onClick={() => {
                const a: Alcance = { tipo: nuevoAlcance.tipo, referenciaId: Number(nuevoAlcance.id) }
                if (!form.alcances.some((x) => x.tipo === a.tipo && x.referenciaId === a.referenciaId)) setForm({ ...form, alcances: [...form.alcances, a] })
                setNuevoAlcance({ ...nuevoAlcance, id: '' })
              }}>Agregar</button>
            </div>
            <div className="d-flex flex-wrap gap-2 mb-3">
              {form.alcances.length === 0 && <small className="text-muted">Sin alcances.</small>}
              {form.alcances.map((a) => (
                <span key={`${a.tipo}${a.referenciaId}`} className="badge badge-oferta">
                  {a.tipo.toLowerCase()}: {nombre(a)}{' '}
                  <button type="button" className="btn-close btn-close-white ms-1" style={{ fontSize: '.6rem' }} aria-label="Quitar"
                    onClick={() => setForm({ ...form, alcances: form.alcances.filter((x) => x !== a) })} />
                </span>
              ))}
            </div>
            <div className="form-check mb-3">
              <input className="form-check-input" type="checkbox" id="pactivo" checked={form.activo} onChange={(e) => setForm({ ...form, activo: e.target.checked })} />
              <label className="form-check-label" htmlFor="pactivo">Activa</label>
            </div>
            <div className="text-end"><button className="btn" type="submit">Guardar</button></div>
          </form>
        )}
      </Modal>
      <Confirmar abierto={!!eliminar} titulo="Eliminar promoción" mensaje={`¿Eliminar «${eliminar?.nombre}»?`} textoConfirmar="Eliminar"
        onCancelar={() => setEliminar(null)} onConfirmar={() => { const p = eliminar!; setEliminar(null); accion(() => promoApi.eliminarPromocion(p.id), 'Promoción eliminada') }} />
    </>
  )
}
