import type { FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { useAccion } from '../../hooks/useAccion'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { Avisos } from '../comunes/Avisos'
import { validarCambioContrasena } from '../../utils/validaciones'

const VACIO = { actual: '', nueva: '', confirmar: '' }

/** HU-039: cambio de contrasena (el backend verifica la actual contra el hash BCrypt). */
export function FormularioContrasena() {
  const accion = useAccion()
  const form = useFormulario(VACIO)

  const guardar = (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarCambioContrasena)) return
    accion.ejecutar(() => usuariosApi.cambiarContrasena(form.valores.actual, form.valores.nueva),
      'Contraseña actualizada. Úsala en tu próximo inicio de sesión.', () => form.reiniciar())
  }

  return (
    <form onSubmit={guardar} noValidate className="ancho">
      <h2>Cambiar contraseña</h2>
      <Avisos estado={accion} />
      <Campo id="actual" etiqueta="Contraseña actual" type="password" autoComplete="current-password" {...form.campo('actual')} />
      <Campo id="nueva" etiqueta="Nueva contraseña" type="password" autoComplete="new-password" ayuda="Mínimo 8 caracteres y un carácter especial" {...form.campo('nueva')} />
      <Campo id="confirmar" etiqueta="Confirmar nueva contraseña" type="password" autoComplete="new-password" {...form.campo('confirmar')} />
      <button className="btn" type="submit">Cambiar contraseña</button>
    </form>
  )
}
