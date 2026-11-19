import { useState, type FormEvent } from 'react'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { CamposDescuento } from './CamposDescuento'
import { validarCupon } from '../../utils/validaciones'
import type { CuponSolicitud } from '../../interfaces/promo'

/** HU-092/093: formulario para crear o editar un cupon. Devuelve los datos ya validados a onGuardar. */
export function EditorCupon({ inicial, onGuardar }: {
  inicial: CuponSolicitud
  /** Debe devolver true si se guardo (para cerrar el modal). */
  onGuardar: (datos: CuponSolicitud) => Promise<boolean>
}) {
  const form = useFormulario(inicial)
  const [enviando, setEnviando] = useState(false)
  const f = form.valores

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarCupon)) return
    setEnviando(true)
    await onGuardar({ ...f, codigo: f.codigo.trim().toUpperCase() })
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <div className="row g-2">
        <div className="col-md-6">
          <Campo id="cupon-codigo" etiqueta="Código" maxLength={40} ayuda="Letras, números, - o _ (3 a 40)" value={f.codigo} error={form.errores.codigo}
            onChange={(e) => form.cambiar('codigo', e.target.value.toUpperCase())} />
        </div>
        <div className="col-md-6">
          <Campo id="cupon-minimo" etiqueta="Monto mínimo de compra (CLP)" type="number" min={0} value={f.montoMinimo} error={form.errores.montoMinimo}
            onChange={(e) => form.cambiar('montoMinimo', Number(e.target.value))} />
        </div>
        <CamposDescuento prefijo="cupon" tipo={f.tipoDescuento} valor={f.valor} inicio={f.fechaInicio} fin={f.fechaFin} errores={form.errores}
          onCambiar={form.combinar} />
      </div>
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar'}</button></div>
    </form>
  )
}
