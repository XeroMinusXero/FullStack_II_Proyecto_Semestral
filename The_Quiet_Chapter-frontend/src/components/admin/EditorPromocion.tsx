import { useState, type FormEvent } from 'react'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { CamposDescuento } from './CamposDescuento'
import { SelectorAlcances } from './SelectorAlcances'
import { LIMITES, validarPromocion } from '../../utils/validaciones'
import type { Alcance, PromocionSolicitud, TipoAlcance } from '../../interfaces/promo'
import type { Clasificacion } from '../../interfaces/catalogo'

/** HU-111/112: formulario para crear o editar una promocion. */
export function EditorPromocion({ inicial, opciones, nombre, onGuardar }: {
  inicial: PromocionSolicitud
  opciones: Record<TipoAlcance, Clasificacion[]>
  nombre: (a: Alcance) => string
  onGuardar: (datos: PromocionSolicitud) => Promise<boolean>
}) {
  const form = useFormulario(inicial)
  const [enviando, setEnviando] = useState(false)
  const f = form.valores

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarPromocion)) return
    setEnviando(true)
    await onGuardar({ ...f, nombre: f.nombre.trim() })
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <div className="row g-2">
        <div className="col-12">
          <Campo id="promo-nombre" etiqueta="Nombre" maxLength={LIMITES.nombrePromocion} value={f.nombre} error={form.errores.nombre}
            onChange={(e) => form.cambiar('nombre', e.target.value)} />
        </div>
        <CamposDescuento prefijo="promo" tipo={f.tipoDescuento} valor={f.valor} inicio={f.fechaInicio} fin={f.fechaFin} errores={form.errores}
          onCambiar={form.combinar} />
      </div>
      <SelectorAlcances alcances={f.alcances} opciones={opciones} nombre={nombre} error={form.errores.alcances}
        onCambiar={(a) => form.cambiar('alcances', a)} />
      <div className="form-check mb-3">
        <input className="form-check-input" type="checkbox" id="promo-activa" checked={f.activo} onChange={(e) => form.cambiar('activo', e.target.checked)} />
        <label className="form-check-label" htmlFor="promo-activa">Activa</label>
      </div>
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar'}</button></div>
    </form>
  )
}
