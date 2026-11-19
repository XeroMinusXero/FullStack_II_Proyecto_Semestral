import { useState, type FormEvent } from 'react'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo, CampoTextoLargo } from '../comunes/Campo'
import { LIMITES, validarBoletin, type DatosBoletin } from '../../utils/validaciones'

/** HU-106/107: borrador de boletin (asunto y contenido). Se envia despues, desde la tabla. */
export function EditorBoletin({ inicial, onGuardar }: { inicial: DatosBoletin; onGuardar: (b: DatosBoletin) => Promise<boolean> }) {
  const form = useFormulario(inicial)
  const [enviando, setEnviando] = useState(false)

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarBoletin)) return
    setEnviando(true)
    await onGuardar({ asunto: form.valores.asunto.trim(), contenido: form.valores.contenido.trim() })
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <Campo id="boletin-asunto" etiqueta="Asunto" maxLength={LIMITES.asuntoBoletin} {...form.campo('asunto')} />
      <CampoTextoLargo id="boletin-contenido" etiqueta="Contenido" rows={10}
        ayuda="Cada correo incluye automáticamente el enlace para cancelar la suscripción." {...form.campo('contenido')} />
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar borrador'}</button></div>
    </form>
  )
}
