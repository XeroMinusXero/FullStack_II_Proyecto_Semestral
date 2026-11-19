import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogoApi } from '../api/catalogo'
import { mensajeDe } from '../api/http'
import { GrillaLibros } from '../components/shopping/GrillaLibros'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import { MigasDePan } from '../components/comunes/MigasDePan'
import type { Autor as TAutor, LibroResumen } from '../interfaces/catalogo'

/** HU-009: biografia del autor y sus libros. */
export default function Autor() {
  const autorId = Number(useParams().id)
  const [autor, setAutor] = useState<TAutor | null>(null)
  const [libros, setLibros] = useState<LibroResumen[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    catalogoApi.autor(autorId).then(setAutor).catch((e) => setError(mensajeDe(e)))
    catalogoApi.buscar({ autorId, tamanio: 24 }).then((p) => setLibros(p.libros)).catch(() => setLibros([]))
  }, [autorId])

  if (error) return <><Alerta tipo="error">{error}</Alerta><Link to="/catalogo" className="btn">Volver al catálogo</Link></>
  if (!autor) return <Cargando />
  return (
    <>
      <MigasDePan niveles={[{ texto: 'Inicio', a: '/' }, { texto: 'Catálogo', a: '/catalogo' }, { texto: autor.nombre }]} />
      <section className="seccion">
        <h1>{autor.nombre}</h1>
        <p className="texto-preformado">{autor.biografia || 'Biografía no disponible.'}</p>
      </section>
      <h2>Libros de {autor.nombre}</h2>
      <GrillaLibros libros={libros.map((l) => ({ ...l, autores: l.autores.join(', ') }))} vacio="Este autor no tiene libros disponibles." />
    </>
  )
}
