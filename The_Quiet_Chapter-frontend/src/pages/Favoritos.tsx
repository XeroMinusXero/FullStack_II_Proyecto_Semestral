import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { socialApi } from '../api/social'
import { catalogoApi } from '../api/catalogo'
import { mensajeDe } from '../api/http'
import { useFavoritos } from '../context/favoritos'
import { GrillaLibros } from '../components/shopping/GrillaLibros'
import { MenuCuenta } from '../components/layout/MenuCuenta'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import type { DatosTarjeta } from '../components/shopping/TarjetaLibro'

/** favoritos.html -> HU-077: lista de favoritos con disponibilidad actual (consultada a catalog-service). */
export default function Favoritos() {
  const { ids } = useFavoritos()
  const [libros, setLibros] = useState<DatosTarjeta[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let vigente = true
    socialApi.favoritos()
      .then(async (favs) => {
        const condiciones = await catalogoApi.condiciones(favs.map((f) => f.libroId))
        const porId = new Map(condiciones.map((c) => [c.id, c]))
        const lista = favs.map((f) => porId.get(f.libroId)).filter((c) => !!c)
          .map((c) => ({ id: c.id, titulo: c.titulo, autores: c.autores, precio: c.precio, portadaUrl: c.portadaUrl, disponible: c.activo && c.stock > 0 }))
        if (vigente) setLibros(lista)
      })
      .catch((e) => { if (vigente) { setError(mensajeDe(e)); setLibros([]) } })
    return () => { vigente = false }
  }, [ids])

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <h2>Mis favoritos</h2>
      <Alerta tipo="error">{error}</Alerta>
      {libros === null ? <Cargando /> : libros.length === 0 ? (
        <div className="vacio"><p>Aún no tienes libros favoritos.</p><Link to="/catalogo" className="btn">Explorar catálogo</Link></div>
      ) : <GrillaLibros libros={libros} />}
    </>
  )
}
