import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { notifApi } from '../../api/notif'
import { mensajeDe } from '../../api/http'
import { reglas } from '../../utils/validaciones'
import estilos from './Footer.module.css'

/** Pie del sitio + suscripcion al boletin con doble confirmacion (HU-132). */
export function Footer() {
  const [email, setEmail] = useState('')
  const [mensaje, setMensaje] = useState<{ ok: boolean; texto: string } | null>(null)

  const suscribir = async (e: FormEvent) => {
    e.preventDefault()
    if (!reglas.email(email)) { setMensaje({ ok: false, texto: 'Ingresa un correo válido' }); return }
    try {
      const r = await notifApi.suscribir(email.trim())
      setMensaje({ ok: true, texto: r.mensaje })
      setEmail('')
    } catch (err) {
      setMensaje({ ok: false, texto: mensajeDe(err) })
    }
  }

  return (
    <footer>
      <div>© 2026 Librería The Quiet Chapter</div>
      <form className={estilos.boletin} onSubmit={suscribir} noValidate>
        <label htmlFor="boletin-email" className={estilos.etiqueta}>Recibe novedades</label>
        <input id="boletin-email" type="email" placeholder="tu@correo.cl" value={email} onChange={(e) => setEmail(e.target.value)} className={estilos.input} />
        <button className="btn" type="submit">Suscribirme</button>
        {mensaje && <small className={mensaje.ok ? estilos.ok : estilos.error} role="status">{mensaje.texto}</small>}
      </form>
      <div className="footer-links">
        <Link to="/catalogo">Catálogo</Link>
        <Link to="/ofertas">Ofertas</Link>
        <Link to="/cuenta/pedidos">Mis pedidos</Link>
      </div>
    </footer>
  )
}
