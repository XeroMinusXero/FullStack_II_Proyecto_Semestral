import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { notifApi } from '../../api/notif'
import { mensajeDe } from '../../api/http'
import { Cargando } from '../../components/comunes/Cargando'

/** HU-108/110: enlaces de los correos del boletin (/boletin/confirmar?token= y /boletin/cancelar?token=). */
export default function Boletin({ accion }: { accion: 'confirmar' | 'cancelar' }) {
  const token = useSearchParams()[0].get('token') ?? ''
  const [resultado, setResultado] = useState<{ ok: boolean; texto: string } | null>(null)
  const enviado = useRef(false)

  useEffect(() => {
    if (enviado.current) return // StrictMode monta dos veces: el token es de un solo uso
    enviado.current = true
    if (!token) return
    const llamada = accion === 'confirmar' ? notifApi.confirmar(token) : notifApi.cancelar(token)
    llamada.then((r) => setResultado({ ok: true, texto: r.mensaje })).catch((e) => setResultado({ ok: false, texto: mensajeDe(e) }))
  }, [accion, token])

  return (
    <div className="vacio">
      <h1>{accion === 'confirmar' ? 'Confirmar suscripción' : 'Cancelar suscripción'}</h1>
      {!token ? <p className="text-danger">El enlace no es válido.</p> : !resultado ? <Cargando /> : <p className={resultado.ok ? 'mensaje-exito' : 'text-danger'}>{resultado.texto}</p>}
      <Link to="/" className="btn">Ir al inicio</Link>
    </div>
  )
}
