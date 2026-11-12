import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useFavoritos } from '../../context/favoritos'
import { useAuth } from '../../context/auth'
import { mensajeDe } from '../../api/http'

/** HU-075/076/078: alterna favorito. Un visitante es enviado a iniciar sesion. */
export function BotonFavorito({ libroId, compacto = false }: { libroId: number; compacto?: boolean }) {
  const { ids, alternar } = useFavoritos()
  const { usuario, esAdmin } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [ocupado, setOcupado] = useState(false)
  if (esAdmin) return null
  const es = ids.has(libroId)

  const alHacerClic = async () => {
    if (!usuario) {
      navegar(`/login?redir=${encodeURIComponent(ubicacion.pathname)}`)
      return
    }
    setOcupado(true)
    try { await alternar(libroId) } catch (e) { alert(mensajeDe(e)) } finally { setOcupado(false) }
  }

  return (
    <button type="button" className="btn btn-secundario" disabled={ocupado} onClick={alHacerClic}
      aria-pressed={es} title={es ? 'Quitar de favoritos' : 'Agregar a favoritos'}>
      {es ? '♥' : '♡'}{!compacto && (es ? ' En favoritos' : ' Favorito')}
    </button>
  )
}
