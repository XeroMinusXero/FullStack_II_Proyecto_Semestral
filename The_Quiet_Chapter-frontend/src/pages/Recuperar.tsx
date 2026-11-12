import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { usuariosApi } from '../api/usuarios'
import { mensajeDe } from '../api/http'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { MENSAJES, reglas } from '../utils/validaciones'

/** HU-036: recuperacion de contrasena con codigo enviado por correo (simulado en notif-service). */
export default function Recuperar() {
  const [paso, setPaso] = useState<1 | 2 | 3>(1)
  const [email, setEmail] = useState('')
  const [codigo, setCodigo] = useState('')
  const [nueva, setNueva] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [enviando, setEnviando] = useState(false)

  const solicitar = async (e: FormEvent) => {
    e.preventDefault()
    if (!reglas.email(email)) { setErrores({ email: MENSAJES.email }); return }
    setErrores({}); setEnviando(true); setError('')
    try { const r = await usuariosApi.solicitarRecuperacion(email.trim()); setInfo(r.mensaje); setPaso(2) } catch (err) { setError(mensajeDe(err)) } finally { setEnviando(false) }
  }

  const restablecer = async (e: FormEvent) => {
    e.preventDefault()
    const err: Record<string, string> = {}
    if (!reglas.soloNumeros(codigo) || codigo.trim().length !== 6) err.codigo = 'El código tiene 6 dígitos'
    if (!reglas.passwordFuerte(nueva)) err.nueva = MENSAJES.passwordFuerte
    if (confirmar !== nueva) err.confirmar = 'Las contraseñas no coinciden'
    setErrores(err)
    if (Object.keys(err).length) return
    setEnviando(true); setError('')
    try { await usuariosApi.restablecer(email.trim(), codigo.trim(), nueva); setPaso(3) } catch (er) { setError(mensajeDe(er)) } finally { setEnviando(false) }
  }

  return (
    <div className="pagina-auth">
      <h1>Recuperar contraseña</h1>
      <p className="bajada">Te enviaremos un código a tu correo.</p>
      {paso === 1 && (
        <form onSubmit={solicitar} noValidate>
          <Alerta tipo="error">{error}</Alerta>
          <Campo id="email" etiqueta="Correo electrónico" type="email" value={email} error={errores.email} onChange={(e) => setEmail(e.target.value)} />
          <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Enviar código'}</button>
          <p className="text-center mt-3 mb-0"><Link to="/login">Volver a iniciar sesión</Link></p>
        </form>
      )}
      {paso === 2 && (
        <form onSubmit={restablecer} noValidate>
          <Alerta tipo="info">{info || 'Si el correo está registrado, recibirás un código.'}</Alerta>
          <Alerta tipo="error">{error}</Alerta>
          <Campo id="codigo" etiqueta="Código de 6 dígitos" inputMode="numeric" maxLength={6} value={codigo} error={errores.codigo} onChange={(e) => setCodigo(e.target.value)} />
          <Campo id="nueva" etiqueta="Nueva contraseña" type="password" autoComplete="new-password" value={nueva} error={errores.nueva} onChange={(e) => setNueva(e.target.value)} />
          <Campo id="confirmar" etiqueta="Confirmar contraseña" type="password" autoComplete="new-password" value={confirmar} error={errores.confirmar} onChange={(e) => setConfirmar(e.target.value)} />
          <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Restablecer contraseña'}</button>
        </form>
      )}
      {paso === 3 && (
        <div className="vacio">
          <p>Tu contraseña fue actualizada.</p>
          <Link to="/login" className="btn">Iniciar sesión</Link>
        </div>
      )}
    </div>
  )
}
