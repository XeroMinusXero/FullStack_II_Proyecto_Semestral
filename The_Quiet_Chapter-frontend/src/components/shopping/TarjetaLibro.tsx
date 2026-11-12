import { Link } from 'react-router-dom'
import { Portada } from './Portada'
import { Precio } from './Precio'
import { BotonAgregar } from './BotonAgregar'
import { BotonFavorito } from './BotonFavorito'
import { Estrellas } from '../comunes/Estrellas'
import { useAuth } from '../../context/auth'
import type { PrecioPromo } from '../../interfaces/promo'

export interface DatosTarjeta {
  id: number
  titulo: string
  autores: string
  precio: number
  portadaUrl?: string | null
  disponible: boolean
}

/** Tarjeta de libro del catalogo (HU-001): portada, titulo, autor, precio y disponibilidad. */
export function TarjetaLibro({ libro, promo, promedio }: { libro: DatosTarjeta; promo?: PrecioPromo; promedio?: { promedio: number; cantidad: number } }) {
  const { esAdmin } = useAuth()
  return (
    <div className="card-libro">
      <Link to={`/libros/${libro.id}`} aria-label={`Ver ${libro.titulo}`} className="portada-envoltorio">
        {promo && <span className="cinta-oferta">-{promo.porcentaje}%</span>}
        <Portada url={libro.portadaUrl} titulo={libro.titulo} id={libro.id} />
      </Link>
      <div className="favorito-flotante"><BotonFavorito libroId={libro.id} compacto /></div>
      <div className="info">
        <div className="titulo">{libro.titulo}</div>
        <div className="autor">{libro.autores}</div>
        {promedio && <div><Estrellas valor={promedio.promedio} tamano=".9rem" /> <small>({promedio.cantidad})</small></div>}
        <Precio precio={libro.precio} promo={promo} />
        <span className={`badge ${libro.disponible ? 'badge-disponible' : 'badge-agotado'}`}>{libro.disponible ? 'Disponible' : 'Agotado'}</span>
        <div className="acciones">
          <Link to={`/libros/${libro.id}`} className="btn btn-secundario">Ver</Link>
          <BotonAgregar libroId={libro.id} disponible={libro.disponible} />
          {esAdmin && <Link to={`/admin/catalogo?editar=${libro.id}`} className="btn">Editar</Link>}
        </div>
      </div>
    </div>
  )
}
