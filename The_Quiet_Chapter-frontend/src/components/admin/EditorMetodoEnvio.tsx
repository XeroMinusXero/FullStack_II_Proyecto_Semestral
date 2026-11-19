import { useState, type FormEvent } from 'react'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { REGIONES } from '../../utils/regiones'
import { LIMITES, validarMetodoEnvio } from '../../utils/validaciones'
import type { MetodoEnvio } from '../../interfaces/checkout'
import estilos from './EditorMetodoEnvio.module.css'

export type DatosMetodoEnvioForm = Omit<MetodoEnvio, 'id'>

/** HU-024 (administrador): metodo de envio con costo, plazo y regiones donde aplica (ninguna = todas). */
export function EditorMetodoEnvio({ inicial, onGuardar }: { inicial: DatosMetodoEnvioForm; onGuardar: (m: DatosMetodoEnvioForm) => Promise<boolean> }) {
  const form = useFormulario(inicial)
  const [enviando, setEnviando] = useState(false)
  const f = form.valores

  const alternarRegion = (r: string) => form.cambiar('regionesHabilitadas',
    f.regionesHabilitadas.includes(r) ? f.regionesHabilitadas.filter((x) => x !== r) : [...f.regionesHabilitadas, r])

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarMetodoEnvio)) return
    setEnviando(true)
    await onGuardar({ ...f, nombre: f.nombre.trim() })
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <Campo id="envio-nombre" etiqueta="Nombre" maxLength={LIMITES.nombreOpcion} {...form.campo('nombre')} />
      <div className="row g-2">
        <div className="col"><Campo id="envio-costo" etiqueta="Costo (CLP)" type="number" min={0} value={f.costoBase} error={form.errores.costoBase}
          onChange={(e) => form.cambiar('costoBase', Number(e.target.value))} /></div>
        <div className="col"><Campo id="envio-plazo" etiqueta="Plazo (días hábiles)" type="number" min={0} value={f.plazoEstimadoDias} error={form.errores.plazoEstimadoDias}
          onChange={(e) => form.cambiar('plazoEstimadoDias', Number(e.target.value))} /></div>
      </div>
      <fieldset className="mb-3">
        <legend className={estilos.leyenda}>Regiones habilitadas <small className="text-muted">(ninguna = todas)</small></legend>
        <div className={estilos.regiones}>
          {REGIONES.map((r) => (
            <div key={r} className="form-check">
              <input className="form-check-input" type="checkbox" id={`reg-${r}`} checked={f.regionesHabilitadas.includes(r)} onChange={() => alternarRegion(r)} />
              <label className="form-check-label" htmlFor={`reg-${r}`}><small>{r}</small></label>
            </div>
          ))}
        </div>
      </fieldset>
      <div className="form-check mb-3">
        <input className="form-check-input" type="checkbox" id="envio-activo" checked={f.activo} onChange={(e) => form.cambiar('activo', e.target.checked)} />
        <label className="form-check-label" htmlFor="envio-activo">Activo</label>
      </div>
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar'}</button></div>
    </form>
  )
}
