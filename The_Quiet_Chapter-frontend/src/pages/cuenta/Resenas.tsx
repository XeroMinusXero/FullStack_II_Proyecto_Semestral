import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { socialApi } from '../../api/social'
import { mensajeDe } from '../../api/http'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Cargando } from '../../components/comunes/Cargando'
import { Alerta } from '../../components/comunes/Alerta'
import { Estrellas } from '../../components/comunes/Estrellas'
import { claseEstado, fecha } from '../../utils/formato'
import type { Resena } from '../../interfaces/social'

/** HU-088: el cliente ve sus resenas y su estado de moderacion. */
export default function Resenas() {
  const [lista, setLista] = useState<Resena[] | null>(null)
  const [error, setError] = useState('')
  useEffect(() => { socialApi.mias().then(setLista).catch((e) => { setError(mensajeDe(e)); setLista([]) }) }, [])

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <h2>Mis reseñas</h2>
      <Alerta tipo="error">{error}</Alerta>
      {!lista ? <Cargando /> : lista.length === 0 ? <p className="text-muted">Aún no has escrito reseñas. Puedes reseñar los libros que compraste desde su ficha.</p> : lista.map((r) => (
        <div key={r.id} className="resumen-caja mb-3">
          <div className="d-flex justify-content-between flex-wrap gap-2">
            <Link to={`/libros/${r.libroId}#resenas`}><strong>Ver libro</strong></Link>
            <span><span className={`badge ${claseEstado(r.estado)}`}>{r.estado.replaceAll('_', ' ').toLowerCase()}</span> <small className="text-muted">{fecha(r.fechaPublicacion)}</small></span>
          </div>
          {r.puntuacion !== null && <Estrellas valor={r.puntuacion} tamano=".9rem" />}
          <p className="mb-1 mt-2">{r.contenido}</p>
          <small className="text-muted">{r.votosUtiles} votos útiles</small>
        </div>
      ))}
    </>
  )
}
