import { GrillaLibros } from './GrillaLibros'
import type { LibroSugerido } from '../../interfaces/promo'

/** Bloque de recomendaciones (HU-114..120). Sin libros no se muestra (HU-116/118/119). */
export function ListaSugeridos({ titulo, libros }: { titulo: string; libros: LibroSugerido[] }) {
  if (!libros.length) return null
  return (
    <section className="seccion">
      <h2>{titulo}</h2>
      <GrillaLibros libros={libros.map((l) => ({ id: l.id, titulo: l.titulo, autores: l.autores, precio: l.precio, portadaUrl: l.portadaUrl, disponible: l.disponible }))} />
    </section>
  )
}
