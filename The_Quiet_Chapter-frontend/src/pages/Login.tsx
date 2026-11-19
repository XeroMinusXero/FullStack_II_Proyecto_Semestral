import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/auth'
import { mensajeDe } from '../api/http'
import { useFormulario } from '../hooks/useFormulario'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { validarLogin } from '../utils/validaciones'
import { destinoSeguro } from '../utils/navegacion'

/** login.html -> HU-034: inicio de sesion. Tras ingresar vuelve a la pagina pedida (?redir=). */
export default function Login() {
  const { usuario, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const [params] = useSearchParams()
  const redir = destinoSeguro(params.get('redir'))
  const form = useFormulario({ email: '', contrasena: '' })
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to={redir ?? (usuario.rol === 'ADMINISTRADOR' ? '/admin' : '/')} replace />

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarLogin)) return
    setEnviando(true); setError('')
    try {
      const perfil = await iniciarSesion(form.valores.email.trim(), form.valores.contrasena)
      navegar(redir ?? (perfil.rol === 'ADMINISTRADOR' ? '/admin' : '/'), { replace: true })
    } catch (err) {
      setError(mensajeDe(err))
    } finally { setEnviando(false) }
  }

  return (
    <div className="pagina-auth">
      <h1>Iniciar sesión</h1>
      <p className="bajada">Bienvenido de vuelta a The Quiet Chapter.</p>
      <form onSubmit={enviar} noValidate>
        {params.get('expirada') && <Alerta tipo="info">Tu sesión expiró. Vuelve a ingresar.</Alerta>}
        <Alerta tipo="error">{error}</Alerta>
        <Campo id="email" etiqueta="Correo electrónico" type="email" autoComplete="email" {...form.campo('email')} />
        <Campo id="contrasena" etiqueta="Contraseña" type="password" autoComplete="current-password" {...form.campo('contrasena')} />
        <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Ingresando...' : 'Ingresar'}</button>
        <p className="text-center mt-3 mb-0"><Link to="/recuperar">¿Olvidaste tu contraseña?</Link></p>
        <div className="separador">o</div>
        <p className="text-center mb-0">¿No tienes cuenta? <Link to={`/registro${redir ? `?redir=${encodeURIComponent(redir)}` : ''}`}>Regístrate</Link></p>
      </form>
    </div>
  )
}
