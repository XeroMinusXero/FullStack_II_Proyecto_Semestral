import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { pedidosApi } from '../../api/pedidos'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { Confirmar } from '../../components/comunes/Confirmar'
import { LineaEstados } from '../../components/pedidos/LineaEstados'
import { ETIQUETA_ESTADO_PEDIDO, claseEstado, clp, fecha } from '../../utils/formato'
import type { EstadoPedido, PaginaPedidos, Pedido } from '../../interfaces/pedido'

/** Transiciones validas (mismas reglas que Pedido.java en order-service). */
const SIGUIENTE: Partial<Record<EstadoPedido, EstadoPedido>> = { PAGADO: 'EN_PREPARACION', EN_PREPARACION: 'ENVIADO', ENVIADO: 'ENTREGADO' }

/** adminPedidos.html -> HU-055..060: listado con filtros, detalle, cambio de estado, seguimiento y cancelacion. */
export default function AdminPedidos() {
  const [filtro, setFiltro] = useState<{ estado: EstadoPedido | ''; fechaDesde: string; fechaHasta: string }>({ estado: '', fechaDesde: '', fechaHasta: '' })
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaPedidos | null>(null)
  const [numero, setNumero] = useState('')
  const [detalle, setDetalle] = useState<Pedido | null>(null)
  const [seguimiento, setSeguimiento] = useState('')
  const [cancelar, setCancelar] = useState(false)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  const rangoInvalido = !!filtro.fechaDesde && !!filtro.fechaHasta && filtro.fechaDesde > filtro.fechaHasta
  const cargar = useCallback(() => {
    if (rangoInvalido) return
    pedidosApi.listar({ estado: filtro.estado, fechaDesde: filtro.fechaDesde || undefined, fechaHasta: filtro.fechaHasta || undefined, pagina, tamano: 15 })
      .then(setDatos).catch((e) => setError(mensajeDe(e)))
  }, [filtro, pagina, rangoInvalido])
  useEffect(() => { cargar() }, [cargar])

  const abrir = (p: Pedido) => { setDetalle(p); setSeguimiento(p.entrega.numeroSeguimiento ?? ''); setError(''); setExito('') }

  const buscarNumero = async (e: FormEvent) => {
    e.preventDefault()
    if (!numero.trim()) return
    setError('')
    try { abrir(await pedidosApi.porNumero(numero.trim())) } catch (er) { setError(mensajeDe(er)) }
  }

  const accion = async (fn: () => Promise<Pedido>, ok: string) => {
    setError(''); setExito('')
    try { const p = await fn(); setDetalle(p); setExito(ok); cargar() } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <h1>Gestión de pedidos</h1>
      <MenuAdmin />
      {rangoInvalido && <Alerta tipo="error">La fecha desde no puede ser posterior a la fecha hasta</Alerta>}
      {!detalle && <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>}
      <div className="filtros">
        <div className="campo">
          <label htmlFor="estado">Estado</label>
          <select id="estado" value={filtro.estado} onChange={(e) => { setFiltro({ ...filtro, estado: e.target.value as EstadoPedido | '' }); setPagina(0) }}>
            <option value="">Todos</option>
            {Object.entries(ETIQUETA_ESTADO_PEDIDO).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </div>
        <div className="campo"><label htmlFor="desde">Desde</label><input id="desde" type="date" value={filtro.fechaDesde} onChange={(e) => { setFiltro({ ...filtro, fechaDesde: e.target.value }); setPagina(0) }} /></div>
        <div className="campo"><label htmlFor="hasta">Hasta</label><input id="hasta" type="date" value={filtro.fechaHasta} onChange={(e) => { setFiltro({ ...filtro, fechaHasta: e.target.value }); setPagina(0) }} /></div>
        <form className="campo" onSubmit={buscarNumero} style={{ background: 'none', boxShadow: 'none', padding: 0, border: 0, margin: 0 }}>
          <label htmlFor="numero">N° de pedido</label>
          <div className="d-flex gap-1"><input id="numero" value={numero} onChange={(e) => setNumero(e.target.value)} placeholder="TQC-..." /><button className="btn" type="submit">Buscar</button></div>
        </form>
      </div>

      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>N° Pedido</th><th>Fecha</th><th>Cliente</th><th>Total</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.pedidos.length === 0 && <tr><td colSpan={6}>No hay pedidos para los filtros seleccionados.</td></tr>}
                {datos.pedidos.map((p) => (
                  <tr key={p.id}>
                    <td>{p.numeroPedido}</td><td>{fecha(p.fechaCreacion, true)}</td>
                    <td>{p.clienteNombre ?? `Cliente #${p.clienteId}`}<br /><small className="text-muted">{p.clienteEmail}</small></td>
                    <td>{clp(p.importes.total)}</td>
                    <td><span className={`badge ${claseEstado(p.estado)}`}>{ETIQUETA_ESTADO_PEDIDO[p.estado]}</span></td>
                    <td><button type="button" className="btn btn-secundario" onClick={() => abrir(p)}>Gestionar</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!detalle} titulo={`Pedido ${detalle?.numeroPedido ?? ''}`} onCerrar={() => setDetalle(null)} ancho="xl">
        {detalle && (
          <>
            <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
            <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
            <LineaEstados estado={detalle.estado} />
            <div className="row g-3">
              <div className="col-md-6">
                <h3>Cliente</h3>
                <p className="mb-1">{detalle.clienteNombre ?? `#${detalle.clienteId}`} · {detalle.clienteEmail}</p>
                <p className="mb-1">{detalle.entrega.direccionFormateada}</p>
                <p>{detalle.entrega.metodoEnvio} · {detalle.medioPago} · Ref. {detalle.referenciaPago}</p>
              </div>
              <div className="col-md-6">
                <h3>Acciones</h3>
                {SIGUIENTE[detalle.estado] && (
                  <button type="button" className="btn mb-2" onClick={() => accion(() => pedidosApi.cambiarEstado(detalle.id, SIGUIENTE[detalle.estado]!), 'Estado actualizado y cliente notificado')}>
                    Marcar como {ETIQUETA_ESTADO_PEDIDO[SIGUIENTE[detalle.estado]!].toLowerCase()}
                  </button>
                )}
                {detalle.estado === 'ENVIADO' && (
                  <div className="d-flex gap-2 mb-2">
                    <input className="form-control" placeholder="N° de seguimiento" aria-label="Número de seguimiento" value={seguimiento} onChange={(e) => setSeguimiento(e.target.value)} />
                    <button type="button" className="btn btn-secundario" disabled={seguimiento.trim().length < 4}
                      onClick={() => accion(() => pedidosApi.seguimiento(detalle.id, seguimiento.trim()), 'Seguimiento registrado')}>Guardar</button>
                  </div>
                )}
                {detalle.puedeCancelarse && <button type="button" className="btn btn-peligro" onClick={() => setCancelar(true)}>Cancelar pedido</button>}
                {!SIGUIENTE[detalle.estado] && !detalle.puedeCancelarse && detalle.estado !== 'ENVIADO' && <p className="text-muted">Pedido cerrado: no admite más cambios.</p>}
              </div>
            </div>
            <div className="tabla-scroll">
              <table>
                <thead><tr><th>Libro</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
                <tbody>{detalle.detalles.map((d) => <tr key={d.id}><td>{d.tituloLibro}</td><td>{d.cantidad}</td><td>{clp(d.precioUnitario)}</td><td>{clp(d.subtotal)}</td></tr>)}</tbody>
              </table>
            </div>
            <p className="text-end mt-2 mb-0">Envío {clp(detalle.importes.costoEnvio)} · Descuento {clp(detalle.importes.descuento)} · <strong>Total {clp(detalle.importes.total)}</strong></p>
          </>
        )}
      </Modal>
      <Confirmar abierto={cancelar} titulo="Cancelar pedido" mensaje="Se repondrá el stock y se notificará al cliente. ¿Continuar?" textoConfirmar="Cancelar pedido"
        onCancelar={() => setCancelar(false)} onConfirmar={() => { setCancelar(false); if (detalle) accion(() => pedidosApi.cancelar(detalle.id), 'Pedido cancelado') }} />
    </>
  )
}
