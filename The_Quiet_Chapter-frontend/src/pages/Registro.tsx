import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { usuariosApi } from '../api/usuarios'
import { ErrorApi, mensajeDe } from '../api/http'
import { useAuth } from '../context/auth'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { MENSAJES, reglas, type Errores } from '../utils/validaciones'

interface Formulario { nombre: string; apellido: string; email: string; telefono: string; contrasena: string; confirmar: string; terminos: boolean }
const VACIO: Formulario = { nombre: '', apellido: '', email: '', telefono: '', contrasena: '', confirmar: '', terminos: false }

/** registro.html -> HU-032/033: registro de cliente con las mismas validaciones del sitio original. */
export default function Registro() {
  const { usuario, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const redir = useSearchParams()[0].get('redir')
  const [f, setF] = useState<Formulario>(VACIO)
  const [errores, setErrores] = useState<Errores<Formulario>>({})
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to="/" replace />

  const validar = () => {
    const e: Errores<Formulario> = {}
    if (!reglas.requerido(f.nombre)) e.nombre = MENSAJES.requerido
    else if (!reglas.soloLetras(f.nombre)) e.nombre = MENSAJES.soloLetras
    if (!reglas.requerido(f.apellido)) e.apellido = MENSAJES.requerido
    else if (!reglas.soloLetras(f.apellido)) e.apellido = MENSAJES.soloLetras
    if (!reglas.requerido(f.email)) e.email = MENSAJES.requerido
    else if (!reglas.email(f.email)) e.email = MENSAJES.email
    if (!reglas.requerido(f.telefono)) e.telefono = MENSAJES.requerido
    else if (!reglas.telefono(f.telefono)) e.telefono = MENSAJES.telefono
    if (!reglas.passwordFuerte(f.contrasena)) e.contrasena = MENSAJES.passwordFuerte
    if (f.confirmar !== f.contrasena) e.confirmar = 'Las contraseñas no coinciden'
    if (!f.terminos) e.terminos = 'Debes aceptar los términos y condiciones'
    return e
  }

  const enviar = async (ev: FormEvent) => {
    ev.preventDefault()
    const e = validar()
    setErrores(e)
    if (Object.keys(e).length) return
    setEnviando(true); setError('')
    try {
      await usuariosApi.registrar({ nombre: f.nombre.trim(), apellido: f.apellido.trim(), email: f.email.trim(), telefono: f.telefono.trim(), contrasena: f.contrasena })
      await iniciarSesion(f.email.trim(), f.contrasena)
      navegar(redir && redir.startsWith('/') ? redir : '/', { replace: true })
    } catch (err) {
      if (err instanceof ErrorApi && Object.keys(err.campos).length) setErrores(err.campos as Errores<Formulario>)
      setError(mensajeDe(err))
    } finally { setEnviando(false) }
  }

  const campo = (k: keyof Formulario) => ({
    value: f[k] as string,
    error: errores[k],
    onChange: (e: { target: { value: string } }) => setF({ ...f, [k]: e.target.value }),
  })

  return (
    <div className="pagina-auth ancha">
      <h1>Crear cuenta</h1>
      <p className="bajada">Guarda tus favoritos, sigue tus pedidos y compra más rápido.</p>
      <form onSubmit={enviar} noValidate>
        <Alerta tipo="error">{error}</Alerta>
        <div className="row g-2">
          <div className="col-sm-6"><Campo id="nombre" etiqueta="Nombre" autoComplete="given-name" maxLength={60} {...campo('nombre')} /></div>
          <div className="col-sm-6"><Campo id="apellido" etiqueta="Apellido" autoComplete="family-name" maxLength={60} {...campo('apellido')} /></div>
        </div>
        <Campo id="email" etiqueta="Correo electrónico" type="email" autoComplete="email" {...campo('email')} />
        <Campo id="telefono" etiqueta="Teléfono" type="tel" autoComplete="tel" placeholder="+56 9 1234 5678" {...campo('telefono')} />
        <Campo id="contrasena" etiqueta="Contraseña" type="password" autoComplete="new-password" ayuda="Mínimo 8 caracteres y un carácter especial" {...campo('contrasena')} />
        <Campo id="confirmar" etiqueta="Confirmar contraseña" type="password" autoComplete="new-password" {...campo('confirmar')} />
        <div className={`campo${errores.terminos ? ' error' : ''}`}>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" id="terminos" checked={f.terminos} onChange={(e) => setF({ ...f, terminos: e.target.checked })} />
            <label className="form-check-label" htmlFor="terminos">Acepto los términos y condiciones</label>
          </div>
          <span className="mensaje-error">{errores.terminos}</span>
        </div>
        <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Creando cuenta...' : 'Registrarme'}</button>
        <p className="text-center mt-3 mb-0">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </div>
  )
}
