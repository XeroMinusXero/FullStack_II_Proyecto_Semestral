import { useCallback, useEffect, useState } from 'react'
import { notifApi } from '../../api/notif'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { Confirmar } from '../../components/comunes/Confirmar'
import { claseEstado, fecha } from '../../utils/formato'
import type { Boletin, Suscriptor } from '../../interfaces/notif'
import type { Pagina } from '../../interfaces/promo'

/** HU-106..110: redaccion y envio de boletines a suscriptores confirmados. */
export default function AdminBoletines() {
  const [pagina, setPagina] = useState(0)
  const [boletines, setBoletines] = useState<Pagina<Boletin> | null>(null)
  const [estadoSus, setEstadoSus] = useState('CONFIRMADA')
  const [paginaSus, setPaginaSus] = useState(0)
  const [suscriptores, setSuscriptores] = useState<Pagina<Suscriptor> | null>(null)
  const [form, setForm] = useState<{ id?: number; asunto: string; contenido: string } | null>(null)
  const [enviar, setEnviar] = useState<Boletin | null>(null)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  const [resumen, setResumen] = useState<{ enviados: number; borradores: number; confirmados: number; pendientes: number } | null>(null)

  const cargar = useCallback(() => {
    notifApi.boletines(undefined, pagina).then(setBoletines).catch((e) => setError(mensajeDe(e)))
    Promise.all([
      notifApi.boletines('ENVIADO'), notifApi.boletines('BORRADOR'),
      notifApi.suscriptores('CONFIRMADA'), notifApi.suscriptores('PENDIENTE'),
    ]).then(([e, b, c, p]) => setResumen({ enviados: e.totalElementos, borradores: b.totalElementos, confirmados: c.totalElementos, pendientes: p.totalElementos }))
      .catch(() => undefined)
  }, [pagina])
  useEffect(() => { cargar() }, [cargar])
  useEffect(() => { notifApi.suscriptores(estadoSus || undefined, paginaSus).then(setSuscriptores).catch((e) => setError(mensajeDe(e))) }, [estadoSus, paginaSus])

  const accion = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok); cargar(); return true } catch (e) { setError(mensajeDe(e)); return false }
  }

  const guardar = async () => {
    if (!form) return
    if (form.asunto.trim().length < 3 || form.contenido.trim().length < 10) { setError('Asunto (mín. 3) y contenido (mín. 10 caracteres) son obligatorios'); return }
    const ok = await accion(() => (form.id ? notifApi.editarBoletin(form.id, form.asunto.trim(), form.contenido.trim()) : notifApi.crearBoletin(form.asunto.trim(), form.contenido.trim())),
      form.id ? 'Borrador actualizado' : 'Borrador creado')
    if (ok) setForm(null)
  }

  return (
    <>
      <h1>Boletines</h1>
      <MenuAdmin />
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <div className="mini-kpis">
        <div className="mini-kpi"><div className="numero">{resumen?.confirmados ?? '…'}</div><div className="rotulo">Suscriptores confirmados</div></div>
        <div className="mini-kpi"><div className="numero">{resumen?.pendientes ?? '…'}</div><div className="rotulo">Por confirmar</div></div>
        <div className="mini-kpi"><div className="numero">{resumen?.enviados ?? '…'}</div><div className="rotulo">Boletines enviados</div></div>
        <div className="mini-kpi"><div className="numero">{resumen?.borradores ?? '…'}</div><div className="rotulo">Borradores</div></div>
      </div>
      <div className="row g-4 align-items-stretch">
        <div className="col-lg-7">
          <section className="panel-caja">
          <div className="cabecera">
            <h2>Boletines</h2>
            <button type="button" className="btn" onClick={() => setForm({ asunto: '', contenido: '' })}>+ Nuevo boletín</button>
          </div>
          {!boletines ? <Cargando /> : (
            <>
              <div className="tabla-scroll">
                <table>
                  <thead><tr><th>Asunto</th><th>Estado</th><th>Fecha</th><th>Destinatarios</th><th></th></tr></thead>
                  <tbody>
                    {boletines.contenido.length === 0 && <tr><td colSpan={5}>Aún no hay boletines.</td></tr>}
                    {boletines.contenido.map((b) => (
                      <tr key={b.id}>
                        <td>{b.asunto}</td>
                        <td><span className={`badge ${b.estado === 'ENVIADO' ? 'badge-disponible' : 'badge-oferta'}`}>{b.estado.toLowerCase()}</span></td>
                        <td>{fecha(b.fechaEnvio ?? b.fechaCreacion, true)}</td>
                        <td>{b.estado === 'ENVIADO' ? b.cantidadDestinatarios : '—'}</td>
                        <td className="d-flex gap-1">
                          {b.estado === 'BORRADOR' && <>
                            <button type="button" className="btn btn-secundario" onClick={() => setForm({ id: b.id, asunto: b.asunto, contenido: b.contenido })}>Editar</button>
                            <button type="button" className="btn" onClick={() => setEnviar(b)}>Enviar</button>
                          </>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Paginacion pagina={boletines.pagina} totalPaginas={boletines.totalPaginas} onCambiar={setPagina} />
            </>
          )}
          </section>
        </div>
        <div className="col-lg-5">
          <section className="panel-caja">
          <div className="cabecera">
            <h2>Suscriptores</h2>
            <select className="form-select" style={{ maxWidth: 170 }} aria-label="Estado" value={estadoSus} onChange={(e) => { setEstadoSus(e.target.value); setPaginaSus(0) }}>
              <option value="">Todos</option><option value="CONFIRMADA">Confirmados</option><option value="PENDIENTE">Pendientes</option><option value="CANCELADA">Cancelados</option>
            </select>
          </div>
          {!suscriptores ? <Cargando /> : (
            <>

              <div className="tabla-scroll">
                <table>
                  <thead><tr><th>Correo</th><th>Estado</th><th>Desde</th></tr></thead>
                  <tbody>
                    {suscriptores.contenido.length === 0 && <tr><td colSpan={3}>Sin suscriptores.</td></tr>}
                    {suscriptores.contenido.map((s) => (
                      <tr key={s.id}><td>{s.email}</td><td><span className={`badge ${claseEstado(s.estado)}`}>{s.estado.toLowerCase()}</span></td><td>{fecha(s.fechaSuscripcion)}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Paginacion pagina={suscriptores.pagina} totalPaginas={suscriptores.totalPaginas} onCambiar={setPaginaSus} />
            </>
          )}
          </section>
        </div>
      </div>

      <Modal abierto={!!form} titulo={form?.id ? 'Editar boletín' : 'Nuevo boletín'} onCerrar={() => setForm(null)} ancho="lg"
        pie={<button type="button" className="btn" onClick={guardar}>Guardar borrador</button>}>
        {form && (
          <>
            <label htmlFor="asunto" className="form-label">Asunto</label>
            <input id="asunto" className="form-control mb-3" maxLength={200} value={form.asunto} onChange={(e) => setForm({ ...form, asunto: e.target.value })} />
            <label htmlFor="contenido" className="form-label">Contenido</label>
            <textarea id="contenido" className="form-control" rows={10} value={form.contenido} onChange={(e) => setForm({ ...form, contenido: e.target.value })} />
            <small className="text-muted">Cada correo incluye automáticamente el enlace para cancelar la suscripción.</small>
          </>
        )}
      </Modal>
      <Confirmar abierto={!!enviar} titulo="Enviar boletín" peligro={false} textoConfirmar="Enviar ahora"
        mensaje={`Se enviará «${enviar?.asunto}» a todos los suscriptores confirmados. Una vez enviado no se puede editar.`}
        onCancelar={() => setEnviar(null)} onConfirmar={() => { const b = enviar!; setEnviar(null); accion(() => notifApi.enviarBoletin(b.id), 'Boletín enviado') }} />
    </>
  )
}
