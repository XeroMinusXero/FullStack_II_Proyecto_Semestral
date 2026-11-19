import { useEffect, useState, type FormEvent } from 'react'
import { notifApi } from '../../api/notif'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { claseEstado, fecha } from '../../utils/formato'
import type { Notificacion } from '../../interfaces/notif'
import type { Pagina } from '../../interfaces/promo'

/** Bitacora de correos simulados emitidos por notif-service (evidencia de HU de notificacion). */
export default function AdminCorreos() {
  const [filtro, setFiltro] = useState({ tipo: '', destinatario: '' })
  const [aplicado, setAplicado] = useState(filtro)
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Notificacion> | null>(null)
  const [abierta, setAbierta] = useState<Notificacion | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    notifApi.todas({ tipo: aplicado.tipo || undefined, destinatario: aplicado.destinatario || undefined, pagina }).then(setDatos).catch((e) => setError(mensajeDe(e)))
  }, [aplicado, pagina])

  const filtrar = (e: FormEvent) => { e.preventDefault(); setPagina(0); setAplicado({ tipo: filtro.tipo.trim().toUpperCase(), destinatario: filtro.destinatario.trim() }) }

  return (
    <>
      <h1>Correos enviados</h1>
      <MenuAdmin />
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <form className="filtros ancho" onSubmit={filtrar}>
        <div className="campo"><label htmlFor="tipo">Tipo</label><input id="tipo" placeholder="ej. PEDIDO_CONFIRMADO" value={filtro.tipo} onChange={(e) => setFiltro({ ...filtro, tipo: e.target.value })} /></div>
        <div className="campo"><label htmlFor="dest">Destinatario</label><input id="dest" placeholder="correo o parte de él" value={filtro.destinatario} onChange={(e) => setFiltro({ ...filtro, destinatario: e.target.value })} /></div>
        <div className="campo justify-content-end"><button className="btn" type="submit">Filtrar</button></div>
      </form>
      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Fecha</th><th>Destinatario</th><th>Tipo</th><th>Asunto</th><th>Estado</th></tr></thead>
              <tbody>
                {datos.contenido.length === 0 && <tr><td colSpan={5}>Sin correos.</td></tr>}
                {datos.contenido.map((n) => (
                  <tr key={n.id} onClick={() => setAbierta(n)} className="fila-clicable">
                    <td>{fecha(n.fechaCreacion, true)}</td><td>{n.destinatario}</td><td><small>{n.tipo}</small></td>
                    <td><button type="button" className="btn btn-link p-0 text-start">{n.asunto}</button></td>
                    <td><span className={`badge ${claseEstado(n.estado)}`}>{n.estado.toLowerCase()}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}
      <Modal abierto={!!abierta} titulo={abierta?.asunto ?? ''} onCerrar={() => setAbierta(null)}>
        {abierta && (
          <>
            <p className="text-muted mb-2">Para: {abierta.destinatario} · {fecha(abierta.fechaCreacion, true)}</p>
            <p className="texto-preformado">{abierta.mensaje}</p>
            {abierta.enlaceAccion && <p className="mb-0"><small>Enlace: <code>{abierta.enlaceAccion}</code></small></p>}
          </>
        )}
      </Modal>
    </>
  )
}
