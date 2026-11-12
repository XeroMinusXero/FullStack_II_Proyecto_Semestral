import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { promoApi } from '../api/promo'
import { catalogoApi } from '../api/catalogo'
import { useAuth } from '../context/auth'
import { ListaSugeridos } from '../components/shopping/ListaSugeridos'
import { Cargando } from '../components/comunes/Cargando'
import { Carrusel } from '../components/comunes/Carrusel'
import type { LibroSugerido } from '../interfaces/promo'
import type { Clasificacion } from '../interfaces/catalogo'

/** index.html -> HU-114 recomendaciones (generales para visitante, personalizadas para cliente). */
export default function Inicio() {
  const { usuario } = useAuth()
  const [recomendados, setRecomendados] = useState<LibroSugerido[] | null>(null)
  const [categorias, setCategorias] = useState<Clasificacion[]>([])

  useEffect(() => {
    promoApi.inicio(10).then(setRecomendados).catch(() => setRecomendados([]))
    catalogoApi.categorias().then(setCategorias).catch(() => setCategorias([]))
  }, [usuario?.id])

  return (
    <>
      <section className="hero">
        <span className="hero-emblema" aria-hidden="true">❦</span>
        <h1>Bienvenido a Librería The Quiet Chapter</h1>
        <p>Descubre historias que perduran. {usuario ? `Qué bueno verte, ${usuario.nombre}.` : 'Miles de títulos a tu alcance.'}</p>
        <div className="hero-acciones">
          <Link to="/catalogo" className="btn btn-dorado">Explorar catálogo</Link>
          <Link to="/ofertas" className="btn btn-contorno">Ver ofertas</Link>
        </div>
      </section>

      {recomendados === null ? <Cargando /> : (
        <ListaSugeridos titulo={usuario ? 'Recomendados para ti' : 'Los más elegidos'} libros={recomendados} />
      )}

      {categorias.length > 0 && (
        <section className="seccion">
          <h2>Explora por categoría</h2>
          <Carrusel etiqueta="Categorías">
            {categorias.map((c) => (
              <Link key={c.id} to={`/catalogo?categorias=${c.id}`} className="tarjeta-categoria" role="listitem">
                <span className="inicial" aria-hidden="true">{c.nombre.charAt(0)}</span>
                <strong>{c.nombre}</strong>
                <small>Ver libros →</small>
              </Link>
            ))}
          </Carrusel>
        </section>
      )}
    </>
  )
}
