import { useEffect, useState } from 'react'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import { Alerta } from '../comunes/Alerta'
import { fecha } from '../../utils/formato'
import type { LibroResumen, MovimientoStock } from '../../interfaces/catalogo'

/** HU-050/051/052: ingreso de unidades, ajuste con motivo e historial de movimientos. */
export function GestionStock({ libro, onCambio }: { libro: LibroResumen; onCambio: () => void }) {
  const [movs, setMovs] = useState<MovimientoStock[] | null>(null)
  const [ingreso, setIngreso] = useState('')
  const [nuevo, setNuevo] = useState(String(libro.stock))
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [stock, setStock] = useState(libro.stock)

  const cargar = () => catalogoApi.movimientos(libro.id).then(setMovs).catch((e) => setError(mensajeDe(e)))
  useEffect(() => { cargar() }, [libro.id]) // eslint-disable-line react-hooks/exhaustive-deps

  const ejecutar = async (fn: () => Promise<MovimientoStock>, ok: string) => {
    setError(''); setExito('')
    try { const m = await fn(); setStock(m.stockPosterior); setNuevo(String(m.stockPosterior)); setExito(ok); await cargar(); onCambio() } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <p>Stock actual: <strong>{stock}</strong> unidades</p>
      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{exito}</Alerta>
      <div className="row g-3 mb-3">
        <div className="col-md-5">
          <label className="form-label" htmlFor="ingreso">Ingreso de unidades</label>
          <div className="d-flex gap-2">
            <input id="ingreso" type="number" min={1} className="form-control" value={ingreso} onChange={(e) => setIngreso(e.target.value)} />
            <button type="button" className="btn" disabled={!(Number(ingreso) > 0)}
              onClick={() => ejecutar(() => catalogoApi.ingreso(libro.id, Number(ingreso)), `Ingresadas ${ingreso} unidades`).then(() => setIngreso(''))}>Ingresar</button>
          </div>
        </div>
        <div className="col-md-7">
          <label className="form-label" htmlFor="ajuste">Ajuste manual (nuevo stock y motivo)</label>
          <div className="d-flex gap-2 flex-wrap">
            <input id="ajuste" type="number" min={0} className="form-control" style={{ maxWidth: 110 }} value={nuevo} onChange={(e) => setNuevo(e.target.value)} />
            <input aria-label="Motivo del ajuste" className="form-control" style={{ flex: 1, minWidth: 150 }} placeholder="Motivo (ej. merma, conteo)" value={motivo} onChange={(e) => setMotivo(e.target.value)} />
            <button type="button" className="btn btn-secundario" disabled={!(Number(nuevo) >= 0) || motivo.trim().length < 3}
              onClick={() => ejecutar(() => catalogoApi.ajuste(libro.id, Number(nuevo), motivo.trim()), 'Stock ajustado').then(() => setMotivo(''))}>Ajustar</button>
          </div>
        </div>
      </div>
      <h3>Movimientos</h3>
      <div className="tabla-scroll" style={{ maxHeight: 300 }}>
        <table>
          <thead><tr><th>Fecha</th><th>Tipo</th><th>Variación</th><th>Stock</th><th>Motivo</th></tr></thead>
          <tbody>
            {movs?.length === 0 && <tr><td colSpan={5}>Sin movimientos.</td></tr>}
            {movs?.map((m) => (
              <tr key={m.id}>
                <td>{fecha(m.fecha, true)}</td><td>{m.tipo}</td>
                <td style={{ color: m.variacion < 0 ? 'var(--error)' : 'var(--exito)' }}>{m.variacion > 0 ? `+${m.variacion}` : m.variacion}</td>
                <td>{m.stockAnterior} → {m.stockPosterior}</td><td>{m.motivo ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
