import { useState, type FormEvent } from 'react'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { LIMITES, validarMedioPago } from '../../utils/validaciones'

export interface DatosMedioPagoForm { nombre: string; activo: boolean }

/** HU-025 (administrador): medio de pago simulado. */
export function EditorMedioPago({ inicial, onGuardar }: { inicial: DatosMedioPagoForm; onGuardar: (m: DatosMedioPagoForm) => Promise<boolean> }) {
  const form = useFormulario(inicial)
  const [enviando, setEnviando] = useState(false)

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar((v) => { const nombre = validarMedioPago(v.nombre); return nombre ? { nombre } : {} })) return
    setEnviando(true)
    await onGuardar({ ...form.valores, nombre: form.valores.nombre.trim() })
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <Campo id="medio-nombre" etiqueta="Nombre" maxLength={LIMITES.nombreOpcion} {...form.campo('nombre')} />
      <div className="form-check mb-3">
        <input className="form-check-input" type="checkbox" id="medio-activo" checked={form.valores.activo} onChange={(e) => form.cambiar('activo', e.target.checked)} />
        <label className="form-check-label" htmlFor="medio-activo">Activo</label>
      </div>
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar'}</button></div>
    </form>
  )
}
