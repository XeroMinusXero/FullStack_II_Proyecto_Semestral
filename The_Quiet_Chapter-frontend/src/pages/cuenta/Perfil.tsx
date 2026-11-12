import { useState, type FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { mensajeDe } from '../../api/http'
import { useAuth } from '../../context/auth'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Campo } from '../../components/comunes/Campo'
import { Alerta } from '../../components/comunes/Alerta'
import { fecha } from '../../utils/formato'
import { MENSAJES, reglas } from '../../utils/validaciones'

/** miPerfil.html -> HU-037 datos personales, HU-038 correo, HU-039 contrasena. */
export default function Perfil() {
  const { usuario, refrescarPerfil, cerrarSesion } = useAuth()
  // RutaProtegida garantiza sesion: el formulario parte con los datos actuales del perfil.
  const [datos, setDatos] = useState({ nombre: usuario?.nombre ?? '', apellido: usuario?.apellido ?? '', telefono: usuario?.telefono ?? '' })
  const [email, setEmail] = useState(usuario?.email ?? '')
  const [clave, setClave] = useState({ actual: '', nueva: '', confirmar: '' })
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [exito, setExito] = useState('')
  const [error, setError] = useState('')

  const ejecutar = async (fn: () => Promise<unknown>, ok: string) => {
    setError(''); setExito('')
    try { await fn(); setExito(ok) } catch (e) { setError(mensajeDe(e)) }
  }

  const guardarDatos = (e: FormEvent) => {
    e.preventDefault()
    const err: Record<string, string> = {}
    if (!reglas.soloLetras(datos.nombre)) err.nombre = MENSAJES.soloLetras
    if (!reglas.soloLetras(datos.apellido)) err.apellido = MENSAJES.soloLetras
    if (!reglas.telefono(datos.telefono)) err.telefono = MENSAJES.telefono
    setErrores(err)
    if (Object.keys(err).length) return
    ejecutar(async () => { await usuariosApi.actualizarPerfil(datos.nombre.trim(), datos.apellido.trim(), datos.telefono.trim()); await refrescarPerfil() }, 'Datos actualizados')
  }

  const guardarCorreo = (e: FormEvent) => {
    e.preventDefault()
    if (!reglas.email(email)) { setErrores({ email: MENSAJES.email }); return }
    setErrores({})
    ejecutar(async () => { await usuariosApi.actualizarCorreo(email.trim()); await refrescarPerfil() }, 'Correo actualizado')
  }

  const guardarClave = (e: FormEvent) => {
    e.preventDefault()
    const err: Record<string, string> = {}
    if (!clave.actual) err.actual = MENSAJES.requerido
    if (!reglas.passwordFuerte(clave.nueva)) err.nueva = MENSAJES.passwordFuerte
    if (clave.nueva !== clave.confirmar) err.confirmar = 'Las contraseñas no coinciden'
    setErrores(err)
    if (Object.keys(err).length) return
    ejecutar(async () => { await usuariosApi.cambiarContrasena(clave.actual, clave.nueva); setClave({ actual: '', nueva: '', confirmar: '' }) },
      'Contraseña actualizada. Úsala en tu próximo inicio de sesión.')
  }

  if (!usuario) return null
  return (
    <>
      <h1>Mi cuenta</h1>
      {usuario.rol === 'CLIENTE' && <MenuCuenta />}
      <p className="text-muted">Cliente desde {fecha(usuario.fechaRegistro)}</p>
      <Alerta tipo="exito" onCerrar={() => setExito('')}>{exito}</Alerta>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      <div className="row g-4">
        <div className="col-lg-6">
          <form onSubmit={guardarDatos} noValidate className="ancho">
            <h2>Datos personales</h2>
            <Campo id="nombre" etiqueta="Nombre" value={datos.nombre} error={errores.nombre} onChange={(e) => setDatos({ ...datos, nombre: e.target.value })} />
            <Campo id="apellido" etiqueta="Apellido" value={datos.apellido} error={errores.apellido} onChange={(e) => setDatos({ ...datos, apellido: e.target.value })} />
            <Campo id="telefono" etiqueta="Teléfono" type="tel" value={datos.telefono} error={errores.telefono} onChange={(e) => setDatos({ ...datos, telefono: e.target.value })} />
            <button className="btn" type="submit">Guardar cambios</button>
          </form>
        </div>
        <div className="col-lg-6">
          <form onSubmit={guardarCorreo} noValidate className="ancho mb-4">
            <h2>Correo electrónico</h2>
            <Campo id="email" etiqueta="Correo" type="email" value={email} error={errores.email} onChange={(e) => setEmail(e.target.value)} />
            <button className="btn" type="submit" disabled={email.trim() === usuario.email}>Actualizar correo</button>
          </form>
          <form onSubmit={guardarClave} noValidate className="ancho">
            <h2>Cambiar contraseña</h2>
            <Campo id="actual" etiqueta="Contraseña actual" type="password" autoComplete="current-password" value={clave.actual} error={errores.actual} onChange={(e) => setClave({ ...clave, actual: e.target.value })} />
            <Campo id="nueva" etiqueta="Nueva contraseña" type="password" autoComplete="new-password" value={clave.nueva} error={errores.nueva} onChange={(e) => setClave({ ...clave, nueva: e.target.value })} />
            <Campo id="confirmar" etiqueta="Confirmar nueva contraseña" type="password" autoComplete="new-password" value={clave.confirmar} error={errores.confirmar} onChange={(e) => setClave({ ...clave, confirmar: e.target.value })} />
            <button className="btn" type="submit">Cambiar contraseña</button>
          </form>
        </div>
      </div>
      <button type="button" className="btn btn-peligro mt-4" onClick={cerrarSesion}>Cerrar sesión</button>
    </>
  )
}
