import type { FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { useAuth } from '../../context/auth'
import { useAccion } from '../../hooks/useAccion'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { Avisos } from '../comunes/Avisos'
import { LIMITES, validarCorreo } from '../../utils/validaciones'

/** HU-038: cambio de correo (el backend rechaza uno ya registrado por otra cuenta). */
export function FormularioCorreo() {
  const { usuario, refrescarPerfil } = useAuth()
  const accion = useAccion()
  const form = useFormulario({ email: usuario?.email ?? '' })
  const sinCambios = form.valores.email.trim().toLowerCase() === usuario?.email.toLowerCase()

  const guardar = (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar((v) => validarCorreo(v.email))) return
    accion.ejecutar(() => usuariosApi.actualizarCorreo(form.valores.email.trim()), 'Correo actualizado', refrescarPerfil)
  }

  return (
    <form onSubmit={guardar} noValidate className="ancho mb-4">
      <h2>Correo electrónico</h2>
      <Avisos estado={accion} />
      <Campo id="email" etiqueta="Correo" type="email" autoComplete="email" maxLength={LIMITES.email} {...form.campo('email')} />
      <button className="btn" type="submit" disabled={sinCambios}>Actualizar correo</button>
    </form>
  )
}
