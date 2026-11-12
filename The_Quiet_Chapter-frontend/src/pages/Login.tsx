import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/auth'
import { mensajeDe } from '../api/http'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { MENSAJES, reglas } from '../utils/validaciones'

/** login.html -> HU-034: inicio de sesion. Tras ingresar vuelve a la pagina pedida (?redir=). */
export default function Login() {
  const { usuario, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const [params] = useSearchParams()
  const redir = params.get('redir')
  const [email, setEmail] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [errores, setErrores] = useState<{ email?: string; contrasena?: string }>({})
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to={redir ?? (usuario.rol === 'ADMINISTRADOR' ? '/admin' : '/')} replace />

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    const err: typeof errores = {}
    if (!reglas.requerido(email)) err.email = MENSAJES.requerido
    else if (!reglas.email(email)) err.email = MENSAJES.email
    if (!reglas.requerido(contrasena)) err.contrasena = MENSAJES.requerido
    setErrores(err)
    if (Object.keys(err).length) return
    setEnviando(true); setError('')
    try {
      const perfil = await iniciarSesion(email.trim(), contrasena)
      navegar(redir && redir.startsWith('/') ? redir : perfil.rol === 'ADMINISTRADOR' ? '/admin' : '/', { replace: true })
    } catch (e) {
      setError(mensajeDe(e))
    } finally { setEnviando(false) }
  }

  return (
    <div className="pagina-auth">
      <h1>Iniciar sesión</h1>
      <p className="bajada">Bienvenido de vuelta a The Quiet Chapter.</p>
      <form onSubmit={enviar} noValidate>
        {params.get('expirada') && <Alerta tipo="info">Tu sesión expiró. Vuelve a ingresar.</Alerta>}
        <Alerta tipo="error">{error}</Alerta>
        <Campo id="email" etiqueta="Correo electrónico" type="email" autoComplete="email" value={email} error={errores.email} onChange={(e) => setEmail(e.target.value)} />
        <Campo id="contrasena" etiqueta="Contraseña" type="password" autoComplete="current-password" value={contrasena} error={errores.contrasena} onChange={(e) => setContrasena(e.target.value)} />
        <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Ingresando...' : 'Ingresar'}</button>
        <p className="text-center mt-3 mb-0"><Link to="/recuperar">¿Olvidaste tu contraseña?</Link></p>
        <div className="separador">o</div>
        <p className="text-center mb-0">¿No tienes cuenta? <Link to={`/registro${redir ? `?redir=${encodeURIComponent(redir)}` : ''}`}>Regístrate</Link></p>
      </form>
    </div>
  )
}
