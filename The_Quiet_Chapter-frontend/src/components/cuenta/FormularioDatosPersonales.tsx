import type { FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { useAuth } from '../../context/auth'
import { useAccion } from '../../hooks/useAccion'
import { useFormulario } from '../../hooks/useFormulario'
import { Campo } from '../comunes/Campo'
import { Avisos } from '../comunes/Avisos'
import { LIMITES, validarPerfil } from '../../utils/validaciones'

/** HU-037: nombre, apellido y telefono del usuario autenticado. */
export function FormularioDatosPersonales() {
  const { usuario, refrescarPerfil } = useAuth()
  const accion = useAccion()
  const form = useFormulario({ nombre: usuario?.nombre ?? '', apellido: usuario?.apellido ?? '', telefono: usuario?.telefono ?? '' })

  const guardar = (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarPerfil)) return
    const { nombre, apellido, telefono } = form.valores
    accion.ejecutar(() => usuariosApi.actualizarPerfil(nombre.trim(), apellido.trim(), telefono.trim()), 'Datos actualizados', refrescarPerfil)
  }

  return (
    <form onSubmit={guardar} noValidate className="ancho">
      <h2>Datos personales</h2>
      <Avisos estado={accion} />
      <Campo id="nombre" etiqueta="Nombre" autoComplete="given-name" maxLength={LIMITES.nombrePersona} {...form.campo('nombre')} />
      <Campo id="apellido" etiqueta="Apellido" autoComplete="family-name" maxLength={LIMITES.nombrePersona} {...form.campo('apellido')} />
      <Campo id="telefono" etiqueta="Teléfono" type="tel" autoComplete="tel" {...form.campo('telefono')} />
      <button className="btn" type="submit">Guardar cambios</button>
    </form>
  )
}
