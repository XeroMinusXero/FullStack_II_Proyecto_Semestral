import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { usuariosApi } from '../api/usuarios'
import { ErrorApi, mensajeDe } from '../api/http'
import { useAuth } from '../context/auth'
import { useFormulario } from '../hooks/useFormulario'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { LIMITES, validarRegistro, type DatosRegistro, type Errores } from '../utils/validaciones'
import { destinoSeguro } from '../utils/navegacion'

const VACIO: DatosRegistro = { nombre: '', apellido: '', email: '', telefono: '', contrasena: '', confirmar: '' }

/** registro.html -> HU-032/033: registro de cliente con las mismas validaciones del sitio original. */
export default function Registro() {
  const { usuario, iniciarSesion } = useAuth()
  const navegar = useNavigate()
  const redir = destinoSeguro(useSearchParams()[0].get('redir'))
  const form = useFormulario(VACIO)
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to="/" replace />

  const enviar = async (ev: FormEvent) => {
    ev.preventDefault()
    if (!form.validar(validarRegistro)) return
    const f = form.valores
    setEnviando(true); setError('')
    try {
      await usuariosApi.registrar({ nombre: f.nombre.trim(), apellido: f.apellido.trim(), email: f.email.trim(), telefono: f.telefono.trim(), contrasena: f.contrasena })
      await iniciarSesion(f.email.trim(), f.contrasena)
      navegar(redir ?? '/', { replace: true })
    } catch (err) {
      // El backend puede devolver errores por campo (por ejemplo, correo ya registrado).
      if (err instanceof ErrorApi && Object.keys(err.campos).length) form.setErrores(err.campos as Errores<DatosRegistro>)
      setError(mensajeDe(err))
    } finally { setEnviando(false) }
  }

  return (
    <div className="pagina-auth ancha">
      <h1>Crear cuenta</h1>
      <p className="bajada">Guarda tus favoritos, sigue tus pedidos y compra más rápido.</p>
      <form onSubmit={enviar} noValidate>
        <Alerta tipo="error">{error}</Alerta>
        <div className="row g-2">
          <div className="col-sm-6"><Campo id="nombre" etiqueta="Nombre" autoComplete="given-name" maxLength={LIMITES.nombrePersona} {...form.campo('nombre')} /></div>
          <div className="col-sm-6"><Campo id="apellido" etiqueta="Apellido" autoComplete="family-name" maxLength={LIMITES.nombrePersona} {...form.campo('apellido')} /></div>
        </div>
        <Campo id="email" etiqueta="Correo electrónico" type="email" autoComplete="email" maxLength={LIMITES.email} {...form.campo('email')} />
        <Campo id="telefono" etiqueta="Teléfono" type="tel" autoComplete="tel" placeholder="+56 9 1234 5678" {...form.campo('telefono')} />
        <Campo id="contrasena" etiqueta="Contraseña" type="password" autoComplete="new-password" ayuda="Mínimo 8 caracteres y un carácter especial" {...form.campo('contrasena')} />
        <Campo id="confirmar" etiqueta="Confirmar contraseña" type="password" autoComplete="new-password" {...form.campo('confirmar')} />
        <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Creando cuenta...' : 'Registrarme'}</button>
        <p className="text-center mt-3 mb-0">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </div>
  )
}
