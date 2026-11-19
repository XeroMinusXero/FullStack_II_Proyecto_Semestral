import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { pedidosApi } from '../../api/pedidos'
import { mensajeDe } from '../../api/http'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Cargando } from '../../components/comunes/Cargando'
import { Alerta } from '../../components/comunes/Alerta'
import { Paginacion } from '../../components/comunes/Paginacion'
import { ETIQUETA_ESTADO_PEDIDO, claseEstado, clp, fecha } from '../../utils/formato'
import type { EstadoPedido, PaginaPedidos } from '../../interfaces/pedido'

/** misPedidos.html -> HU-040/041: historial de pedidos con filtro por estado. */
export default function Pedidos() {
  const [estado, setEstado] = useState<EstadoPedido | ''>('')
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaPedidos | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    pedidosApi.mios({ estado, pagina, tamano: 10 }).then(setDatos).catch((e) => setError(mensajeDe(e)))
  }, [estado, pagina])

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h2 className="mb-0">Mis pedidos</h2>
        <select value={estado} onChange={(e) => { setEstado(e.target.value as EstadoPedido | ''); setPagina(0) }} aria-label="Filtrar por estado" className="form-select selector-corto">
          <option value="">Todos los estados</option>
          {Object.entries(ETIQUETA_ESTADO_PEDIDO).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <Alerta tipo="error">{error}</Alerta>
      {!datos && !error && <Cargando />}
      {datos && datos.pedidos.length === 0 && <div className="vacio"><p>{estado ? 'No tienes pedidos con ese estado.' : 'Aún no has realizado pedidos.'}</p><Link to="/catalogo" className="btn">Ir al catálogo</Link></div>}
      {datos && datos.pedidos.length > 0 && (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>N° Pedido</th><th>Fecha</th><th>Libros</th><th>Total</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.pedidos.map((p) => (
                  <tr key={p.id}>
                    <td>{p.numeroPedido}</td>
                    <td>{fecha(p.fechaCreacion)}</td>
                    <td>{p.detalles.reduce((s, d) => s + d.cantidad, 0)}</td>
                    <td>{clp(p.importes.total)}</td>
                    <td><span className={`badge ${claseEstado(p.estado)}`}>{ETIQUETA_ESTADO_PEDIDO[p.estado]}</span></td>
                    <td><Link to={`/cuenta/pedidos/${p.id}`} className="btn btn-secundario">Ver detalle</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}
    </>
  )
}
