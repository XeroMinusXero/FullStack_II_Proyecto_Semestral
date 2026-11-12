import { useEffect, useState } from 'react'
import { reportesApi, type FiltroPeriodo } from '../../api/reportes'
import { mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { GraficoBarras } from '../../components/comunes/GraficoBarras'
import { ETIQUETA_ESTADO_PEDIDO, clp, fecha } from '../../utils/formato'
import type { Panel, PeriodoRapido, ReporteClientes, ReporteInventario, ReporteLibros, ReporteVentas } from '../../interfaces/reportes'

type Pestana = 'ventas' | 'libros' | 'inventario' | 'clientes'
const PERIODOS: [PeriodoRapido, string][] = [['HOY', 'Hoy'], ['SEMANA', 'Semana'], ['MES', 'Mes'], ['ANIO', 'Año']]

/** adminDashboard.html -> HU-140..150: KPIs, evolucion, mas/menos vendidos, inventario, clientes y exportacion CSV. */
export default function Dashboard() {
  const [filtro, setFiltro] = useState<FiltroPeriodo>({ periodo: 'MES' })
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [pestana, setPestana] = useState<Pestana>('ventas')
  const [panel, setPanel] = useState<Panel | null>(null)
  const [ventas, setVentas] = useState<ReporteVentas | null>(null)
  const [libros, setLibros] = useState<ReporteLibros | null>(null)
  const [inventario, setInventario] = useState<ReporteInventario | null>(null)
  const [clientes, setClientes] = useState<ReporteClientes | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    reportesApi.panel(filtro).then(setPanel).catch((e) => setError(mensajeDe(e)))
  }, [filtro])

  useEffect(() => {
    const fallo = (e: unknown) => setError(mensajeDe(e))
    if (pestana === 'ventas') reportesApi.ventas(filtro).then(setVentas).catch(fallo)
    if (pestana === 'libros') reportesApi.libros(filtro).then(setLibros).catch(fallo)
    if (pestana === 'inventario') reportesApi.inventario(filtro).then(setInventario).catch(fallo)
    if (pestana === 'clientes') reportesApi.clientes(filtro).then(setClientes).catch(fallo)
  }, [filtro, pestana])

  const aplicarRango = () => {
    if (!desde || !hasta) { setError('Indica fecha desde y hasta'); return }
    if (desde > hasta) { setError('La fecha desde no puede ser posterior a la fecha hasta'); return }
    setFiltro({ desde, hasta })
  }

  const exportar = async () => {
    const tipo = pestana === 'libros' ? 'libros-mas-vendidos' : pestana
    try { await reportesApi.csv(tipo, filtro) } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <h1>Dashboard y reportes</h1>
      <MenuAdmin />
      <div className="barra-filtros">
        <div className="campo">
          <label id="lblPeriodo">Período rápido</label>
          <div className="segmentado" role="group" aria-labelledby="lblPeriodo">
            {PERIODOS.map(([v, t]) => (
              <button key={v} type="button" className={filtro.periodo === v ? 'activo' : undefined} aria-pressed={filtro.periodo === v} onClick={() => setFiltro({ periodo: v })}>{t}</button>
            ))}
          </div>
        </div>
        <div className="campo"><label htmlFor="desde">Desde</label><input id="desde" type="date" value={desde} onChange={(e) => setDesde(e.target.value)} /></div>
        <div className="campo"><label htmlFor="hasta">Hasta</label><input id="hasta" type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} /></div>
        <button type="button" className="btn" onClick={aplicarRango}>Aplicar rango</button>
        <button type="button" className="btn btn-secundario empuje" onClick={exportar}>⬇ Exportar CSV</button>
      </div>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>

      {!panel ? <Cargando /> : (
        <>
          <p className="text-muted">Mostrando del <strong>{fecha(panel.periodo.fechaDesde)}</strong> al <strong>{fecha(panel.periodo.fechaHasta)}</strong>{filtro.periodo ? '' : ' (rango personalizado)'}</p>
          <div className="kpi-grid">
            <div className="kpi-card"><div className="etiqueta">Ventas</div><div className="valor">{clp(panel.ventasPeriodo)}</div></div>
            <div className="kpi-card"><div className="etiqueta">Pedidos</div><div className="valor">{panel.cantidadPedidos}</div></div>
            <div className="kpi-card"><div className="etiqueta">Ticket promedio</div><div className="valor">{clp(panel.ticketPromedio)}</div></div>
            <div className="kpi-card"><div className="etiqueta">Clientes registrados</div><div className="valor">{panel.clientesRegistrados}</div></div>
          </div>
          <section className="seccion">
            <h2>Evolución de ventas <small className="text-muted fs-6">(por {{ DIA: 'día', SEMANA: 'semana', MES: 'mes' }[panel.agrupacion]})</small></h2>
            <GraficoBarras datos={panel.evolucion} />
            {Object.keys(panel.pedidosPorEstado).length > 0 && (
              <p className="mb-0">{Object.entries(panel.pedidosPorEstado).map(([k, v]) => `${ETIQUETA_ESTADO_PEDIDO[k] ?? k}: ${v}`).join(' · ')}</p>
            )}
          </section>
        </>
      )}

      <div className="tabs" role="tablist">
        {(['ventas', 'libros', 'inventario', 'clientes'] as Pestana[]).map((p) => (
          <button key={p} type="button" role="tab" aria-selected={pestana === p} className={pestana === p ? 'activo' : undefined} onClick={() => setPestana(p)}>
            {{ ventas: 'Ventas', libros: 'Libros más/menos vendidos', inventario: 'Inventario', clientes: 'Clientes' }[p]}
          </button>
        ))}
      </div>

      {pestana === 'ventas' && (!ventas ? <Cargando /> : (
        <div className="row g-4">
          <div className="col-lg-5">
            <h3>Por categoría</h3>
            <Tabla cabeceras={['Categoría', 'Unidades', 'Monto']} filas={ventas.porCategoria.map((c) => [c.categoriaNombre, c.unidadesVendidas, clp(c.montoTotal)])} />
          </div>
          <div className="col-lg-7">
            <h3>Pedidos del período ({ventas.cantidadPedidos})</h3>
            <Tabla cabeceras={['N°', 'Fecha', 'Cliente', 'Estado', 'Total']}
              filas={ventas.pedidos.map((p) => [p.numeroPedido, fecha(p.fecha), p.clienteNombre ?? `#${p.clienteId}`, ETIQUETA_ESTADO_PEDIDO[p.estado] ?? p.estado, clp(p.total)])} />
          </div>
        </div>
      ))}

      {pestana === 'libros' && (!libros ? <Cargando /> : (
        <div className="row g-4">
          <div className="col-lg-6"><h3>Más vendidos</h3><TablaLibros datos={libros.masVendidos} /></div>
          <div className="col-lg-6"><h3>Menos vendidos</h3><TablaLibros datos={libros.menosVendidos} /></div>
          <div className="col-12"><h3>Sin ventas en el período</h3><TablaLibros datos={libros.sinVentas} /></div>
        </div>
      ))}

      {pestana === 'inventario' && (!inventario ? <Cargando /> : (
        <>
          <div className="reporte-caja mb-3"><strong>Valorización total del inventario:</strong> {clp(inventario.valorTotalInventario)}</div>
          <h3>Rotación por libro</h3>
          <Tabla cabeceras={['Libro', 'Vendidas', 'Stock', 'Stock prom.', 'Rotación', 'Valor']}
            filas={inventario.libros.map((l) => [l.titulo, l.unidadesVendidas, l.stockActual, l.stockPromedio.toFixed(1), l.rotacion === null ? '—' : l.rotacion.toFixed(2), clp(l.valorTotal)])} />
          <h3 className="mt-4">Movimientos de inventario</h3>
          <Tabla cabeceras={['Fecha', 'Libro', 'Tipo', 'Cantidad', 'Responsable']}
            filas={inventario.movimientos.map((m) => [fecha(m.fecha, true), m.tituloLibro, m.tipo, m.cantidad, m.responsableNombre ?? '—'])} />
        </>
      ))}

      {pestana === 'clientes' && (!clientes ? <Cargando /> : (
        <>
          <div className="kpi-grid">
            <div className="kpi-card"><div className="etiqueta">Clientes nuevos</div><div className="valor">{clientes.clientesNuevos}</div></div>
            <div className="kpi-card"><div className="etiqueta">Clientes acumulados</div><div className="valor">{clientes.clientesAcumulados}</div></div>
          </div>
          <div className="row g-4">
            <div className="col-lg-6"><h3>Mayores compradores</h3>
              <Tabla cabeceras={['Cliente', 'Pedidos', 'Monto']} filas={clientes.mayoresCompradores.map((c) => [`${c.nombre} (${c.email})`, c.cantidadPedidosPeriodo, clp(c.montoCompradoPeriodo)])} /></div>
            <div className="col-lg-6"><h3>Sin compras en el período</h3>
              <Tabla cabeceras={['Cliente', 'Registro']} filas={clientes.sinCompras.map((c) => [`${c.nombre} (${c.email})`, fecha(c.fechaRegistro)])} /></div>
          </div>
        </>
      ))}
    </>
  )
}

function Tabla({ cabeceras, filas }: { cabeceras: string[]; filas: (string | number)[][] }) {
  return (
    <div className="tabla-scroll">
      <table>
        <thead><tr>{cabeceras.map((c) => <th key={c}>{c}</th>)}</tr></thead>
        <tbody>
          {filas.length === 0 && <tr><td colSpan={cabeceras.length}>Sin datos para el período.</td></tr>}
          {filas.map((f, i) => <tr key={i}>{f.map((v, j) => <td key={j}>{v}</td>)}</tr>)}
        </tbody>
      </table>
    </div>
  )
}

function TablaLibros({ datos }: { datos: ReporteLibros['masVendidos'] }) {
  return <Tabla cabeceras={['Libro', 'Autor', 'Unidades', 'Monto', 'Stock']} filas={datos.map((l) => [l.titulo, l.autor, l.unidadesVendidas, clp(l.montoTotal), l.stockActual])} />
}
