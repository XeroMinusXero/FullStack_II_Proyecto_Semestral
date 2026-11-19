import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { usuariosApi } from '../api/usuarios'
import { mensajeDe } from '../api/http'
import { useFormulario } from '../hooks/useFormulario'
import { Campo } from '../components/comunes/Campo'
import { Alerta } from '../components/comunes/Alerta'
import { validarCorreo, validarRestablecer } from '../utils/validaciones'

type Paso = 'correo' | 'codigo' | 'listo'

/**
 * HU-036: recuperacion de contrasena en 3 pasos.
 * 1) el usuario pide un codigo (user-service lo genera, guarda su hash y notif-service "envia" el correo);
 * 2) ingresa el codigo de 6 digitos y la nueva contrasena; 3) confirmacion.
 */
export default function Recuperar() {
  const [paso, setPaso] = useState<Paso>('correo')
  const correo = useFormulario({ email: '' })
  const clave = useFormulario({ codigo: '', nueva: '', confirmar: '' })
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [enviando, setEnviando] = useState(false)

  const conEnvio = async (fn: () => Promise<void>) => {
    setEnviando(true); setError('')
    try { await fn() } catch (err) { setError(mensajeDe(err)) } finally { setEnviando(false) }
  }

  const solicitar = (e: FormEvent) => {
    e.preventDefault()
    if (!correo.validar((v) => validarCorreo(v.email))) return
    conEnvio(async () => {
      const r = await usuariosApi.solicitarRecuperacion(correo.valores.email.trim())
      setInfo(r.mensaje)
      setPaso('codigo')
    })
  }

  const restablecer = (e: FormEvent) => {
    e.preventDefault()
    if (!clave.validar(validarRestablecer)) return
    conEnvio(async () => {
      await usuariosApi.restablecer(correo.valores.email.trim(), clave.valores.codigo.trim(), clave.valores.nueva)
      setPaso('listo')
    })
  }

  return (
    <div className="pagina-auth">
      <h1>Recuperar contraseña</h1>
      <p className="bajada">Te enviaremos un código a tu correo.</p>
      {paso === 'correo' && (
        <form onSubmit={solicitar} noValidate>
          <Alerta tipo="error">{error}</Alerta>
          <Campo id="email" etiqueta="Correo electrónico" type="email" autoComplete="email" {...correo.campo('email')} />
          <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Enviar código'}</button>
          <p className="text-center mt-3 mb-0"><Link to="/login">Volver a iniciar sesión</Link></p>
        </form>
      )}
      {paso === 'codigo' && (
        <form onSubmit={restablecer} noValidate>
          <Alerta tipo="info">{info || 'Si el correo está registrado, recibirás un código.'}</Alerta>
          <Alerta tipo="error">{error}</Alerta>
          <Campo id="codigo" etiqueta="Código de 6 dígitos" inputMode="numeric" autoComplete="one-time-code" maxLength={6} {...clave.campo('codigo')} />
          <Campo id="nueva" etiqueta="Nueva contraseña" type="password" autoComplete="new-password" ayuda="Mínimo 8 caracteres y un carácter especial" {...clave.campo('nueva')} />
          <Campo id="confirmar" etiqueta="Confirmar contraseña" type="password" autoComplete="new-password" {...clave.campo('confirmar')} />
          <button className="btn w-100" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Restablecer contraseña'}</button>
          <button type="button" className="btn btn-secundario w-100 mt-2" onClick={() => { setPaso('correo'); setError('') }}>Usar otro correo</button>
        </form>
      )}
      {paso === 'listo' && (
        <div className="vacio">
          <p>Tu contraseña fue actualizada.</p>
          <Link to="/login" className="btn">Iniciar sesión</Link>
        </div>
      )}
    </div>
  )
}
