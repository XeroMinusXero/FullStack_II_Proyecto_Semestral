import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { pedidosApi } from '../../api/pedidos'
import { mensajeDe } from '../../api/http'
import { useAccion } from '../../hooks/useAccion'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Avisos } from '../../components/comunes/Avisos'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { GestionPedido } from '../../components/admin/GestionPedido'
import { ETIQUETA_ESTADO_PEDIDO, claseEstado, clp, fecha } from '../../utils/formato'
import { validarRangoFechas } from '../../utils/validaciones'
import type { EstadoPedido, PaginaPedidos, Pedido } from '../../interfaces/pedido'

interface Filtro { estado: EstadoPedido | ''; fechaDesde: string; fechaHasta: string }

/** adminPedidos.html -> HU-055..060: listado con filtros y busqueda por numero; el detalle lo gestiona GestionPedido. */
export default function AdminPedidos() {
  const accion = useAccion()
  const { setError } = accion
  const [filtro, setFiltro] = useState<Filtro>({ estado: '', fechaDesde: '', fechaHasta: '' })
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaPedidos | null>(null)
  const [numero, setNumero] = useState('')
  const [detalle, setDetalle] = useState<Pedido | null>(null)

  const errorRango = validarRangoFechas(filtro.fechaDesde, filtro.fechaHasta)
  const cargar = useCallback(() => {
    if (errorRango) return
    pedidosApi.listar({ estado: filtro.estado, fechaDesde: filtro.fechaDesde || undefined, fechaHasta: filtro.fechaHasta || undefined, pagina, tamano: 15 })
      .then(setDatos).catch((e) => setError(mensajeDe(e)))
  }, [filtro, pagina, errorRango, setError])
  useEffect(() => { cargar() }, [cargar])

  /** Cambiar un filtro siempre vuelve a la primera pagina. */
  const filtrar = (cambio: Partial<Filtro>) => { setFiltro({ ...filtro, ...cambio }); setPagina(0) }

  const buscarNumero = (e: FormEvent) => {
    e.preventDefault()
    if (!numero.trim()) return
    accion.ejecutar(async () => setDetalle(await pedidosApi.porNumero(numero.trim())))
  }

  const actualizado = (p: Pedido) => { setDetalle(p); cargar() }

  return (
    <>
      <h1>Gestión de pedidos</h1>
      <MenuAdmin />
      <Alerta tipo="error">{errorRango}</Alerta>
      <Avisos estado={accion} />
      <div className="filtros">
        <div className="campo">
          <label htmlFor="estado">Estado</label>
          <select id="estado" value={filtro.estado} onChange={(e) => filtrar({ estado: e.target.value as EstadoPedido | '' })}>
            <option value="">Todos</option>
            {Object.entries(ETIQUETA_ESTADO_PEDIDO).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </div>
        <div className="campo"><label htmlFor="desde">Desde</label><input id="desde" type="date" value={filtro.fechaDesde} onChange={(e) => filtrar({ fechaDesde: e.target.value })} /></div>
        <div className="campo"><label htmlFor="hasta">Hasta</label><input id="hasta" type="date" value={filtro.fechaHasta} onChange={(e) => filtrar({ fechaHasta: e.target.value })} /></div>
        <form className="campo form-plano" onSubmit={buscarNumero}>
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
                    <td><button type="button" className="btn btn-secundario" onClick={() => setDetalle(p)}>Gestionar</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!detalle} titulo={`Pedido ${detalle?.numeroPedido ?? ''}`} onCerrar={() => setDetalle(null)} ancho="xl">
        {detalle && <GestionPedido key={detalle.id} pedido={detalle} onActualizado={actualizado} />}
      </Modal>
    </>
  )
}
