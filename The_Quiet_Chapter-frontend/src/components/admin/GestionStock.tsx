import { useCallback, useEffect, useState } from 'react'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import { useAccion } from '../../hooks/useAccion'
import { Avisos } from '../comunes/Avisos'
import { fecha } from '../../utils/formato'
import { validarAjusteStock, validarIngresoStock, type DatosAjusteStock, type Errores } from '../../utils/validaciones'
import type { LibroResumen, MovimientoStock } from '../../interfaces/catalogo'
import estilos from './GestionStock.module.css'

/** HU-050/051/052: ingreso de unidades, ajuste con motivo e historial de movimientos. */
export function GestionStock({ libro, onCambio }: { libro: LibroResumen; onCambio: () => void }) {
  const accion = useAccion()
  const { setError } = accion
  const [movs, setMovs] = useState<MovimientoStock[] | null>(null)
  const [stock, setStock] = useState(libro.stock)
  const [ingreso, setIngreso] = useState('')
  const [errorIngreso, setErrorIngreso] = useState<string>()
  const [ajuste, setAjuste] = useState<DatosAjusteStock>({ nuevoStock: String(libro.stock), motivo: '' })
  const [erroresAjuste, setErroresAjuste] = useState<Errores<DatosAjusteStock>>({})

  const cargar = useCallback(() => catalogoApi.movimientos(libro.id).then(setMovs).catch((e) => setError(mensajeDe(e))), [libro.id, setError])
  useEffect(() => { cargar() }, [cargar])

  /** Ambas operaciones devuelven el movimiento creado: con el actualizamos el stock mostrado. */
  const registrar = (operacion: () => Promise<MovimientoStock>, mensajeOk: string) =>
    accion.ejecutar(async () => {
      const m = await operacion()
      setStock(m.stockPosterior)
      setAjuste((a) => ({ ...a, nuevoStock: String(m.stockPosterior) }))
    }, mensajeOk, () => { onCambio(); return cargar() })

  const ingresar = async () => {
    const invalido = validarIngresoStock(ingreso)
    setErrorIngreso(invalido)
    if (invalido) return
    if (await registrar(() => catalogoApi.ingreso(libro.id, Number(ingreso)), `Ingresadas ${ingreso} unidades`)) setIngreso('')
  }

  const ajustar = async () => {
    const errores = validarAjusteStock(ajuste)
    setErroresAjuste(errores)
    if (Object.keys(errores).length) return
    if (await registrar(() => catalogoApi.ajuste(libro.id, Number(ajuste.nuevoStock), ajuste.motivo.trim()), 'Stock ajustado')) {
      setAjuste((a) => ({ ...a, motivo: '' }))
    }
  }

  const errorAjuste = erroresAjuste.nuevoStock ?? erroresAjuste.motivo

  return (
    <>
      <p>Stock actual: <strong>{stock}</strong> unidades</p>
      <Avisos estado={accion} />
      <div className="row g-3 mb-3">
        <div className={`col-md-5 campo${errorIngreso ? ' error' : ''}`}>
          <label htmlFor="ingreso">Ingreso de unidades</label>
          <div className="d-flex gap-2">
            <input id="ingreso" type="number" min={1} value={ingreso} aria-invalid={!!errorIngreso}
              onChange={(e) => { setIngreso(e.target.value); setErrorIngreso(undefined) }} />
            <button type="button" className="btn" onClick={ingresar}>Ingresar</button>
          </div>
          <span className="mensaje-error">{errorIngreso}</span>
        </div>
        <div className={`col-md-7 campo${errorAjuste ? ' error' : ''}`}>
          <label htmlFor="ajuste">Ajuste manual (nuevo stock y motivo)</label>
          <div className="d-flex gap-2 flex-wrap">
            <input id="ajuste" type="number" min={0} className={estilos.cantidad} value={ajuste.nuevoStock} aria-invalid={!!erroresAjuste.nuevoStock}
              onChange={(e) => { setAjuste({ ...ajuste, nuevoStock: e.target.value }); setErroresAjuste({}) }} />
            <input aria-label="Motivo del ajuste" className={estilos.motivo} placeholder="Motivo (ej. merma, conteo)" maxLength={400}
              value={ajuste.motivo} aria-invalid={!!erroresAjuste.motivo}
              onChange={(e) => { setAjuste({ ...ajuste, motivo: e.target.value }); setErroresAjuste({}) }} />
            <button type="button" className="btn btn-secundario" onClick={ajustar}>Ajustar</button>
          </div>
          <span className="mensaje-error">{errorAjuste}</span>
        </div>
      </div>
      <h3>Movimientos</h3>
      <div className={`tabla-scroll ${estilos.historial}`}>
        <table>
          <thead><tr><th>Fecha</th><th>Tipo</th><th>Variación</th><th>Stock</th><th>Motivo</th></tr></thead>
          <tbody>
            {movs?.length === 0 && <tr><td colSpan={5}>Sin movimientos.</td></tr>}
            {movs?.map((m) => (
              <tr key={m.id}>
                <td>{fecha(m.fecha, true)}</td><td>{m.tipo}</td>
                <td className={m.variacion < 0 ? 'variacion-negativa' : 'variacion-positiva'}>{m.variacion > 0 ? `+${m.variacion}` : m.variacion}</td>
                <td>{m.stockAnterior} → {m.stockPosterior}</td><td>{m.motivo ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
