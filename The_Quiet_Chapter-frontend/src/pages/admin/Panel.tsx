import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { reportesApi } from '../../api/reportes'
import { catalogoApi } from '../../api/catalogo'
import { socialApi } from '../../api/social'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { useAuth } from '../../context/auth'
import { clp } from '../../utils/formato'
import type { Panel as TPanel } from '../../interfaces/reportes'

/** admin.html -> HU-045: panel de administracion con accesos y KPIs del mes. */
export default function Panel() {
  const { usuario } = useAuth()
  const [panel, setPanel] = useState<TPanel | null>(null)
  const [agotados, setAgotados] = useState<number | null>(null)
  const [reportadas, setReportadas] = useState<number | null>(null)

  useEffect(() => {
    reportesApi.panel({ periodo: 'MES' }).then(setPanel).catch(() => undefined)
    catalogoApi.mantenedor({ agotados: true, tamanio: 1 }).then((p) => setAgotados(p.totalElementos)).catch(() => undefined)
    socialApi.reportadas().then((r) => setReportadas(r.length)).catch(() => undefined)
  }, [])

  const tarjetas: { a: string; titulo: string; valor: string; detalle: string }[] = [
    { a: '/admin/dashboard', titulo: 'Ventas del mes', valor: panel ? clp(panel.ventasPeriodo) : '…', detalle: panel ? `${panel.cantidadPedidos} pedidos` : '' },
    { a: '/admin/pedidos', titulo: 'Ticket promedio', valor: panel ? clp(panel.ticketPromedio) : '…', detalle: 'Gestionar pedidos' },
    { a: '/admin/catalogo?agotados=1', titulo: 'Libros agotados', valor: agotados === null ? '…' : String(agotados), detalle: 'Reponer inventario' },
    { a: '/admin/resenas', titulo: 'Reseñas reportadas', valor: reportadas === null ? '…' : String(reportadas), detalle: 'Moderar' },
    { a: '/admin/dashboard', titulo: 'Clientes registrados', valor: panel ? String(panel.clientesRegistrados) : '…', detalle: 'Ver reportes' },
    { a: '/admin/promociones', titulo: 'Cupones y promociones', valor: '%', detalle: 'Administrar descuentos' },
  ]

  return (
    <>
      <h1>Panel de Administración</h1>
      <MenuAdmin />
      <p>Hola, {usuario?.nombre}. Resumen del mes en curso.</p>
      <div className="kpi-grid kpi-3">
        {tarjetas.map((t) => (
          <Link key={t.titulo} to={t.a} className="kpi-card text-decoration-none">
            <div className="etiqueta">{t.titulo}</div>
            <div className="valor">{t.valor}</div>
            <small className="kpi-detalle">{t.detalle}</small>
          </Link>
        ))}
      </div>
    </>
  )
}
