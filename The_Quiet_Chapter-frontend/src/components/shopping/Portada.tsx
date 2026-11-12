import type { CSSProperties } from 'react'
import estilos from './Portada.module.css'

const EMOJIS = ['📖', '📕', '📗', '📘', '📙', '📔']

/**
 * Portada del libro: imagen si existe, si no el bloque de color con emoji del diseno original.
 * En tarjeta y ficha la imagen se muestra COMPLETA (object-fit: contain) sobre un fondo desenfocado de la
 * misma portada: asi no se recorta aunque la imagen subida no tenga proporcion 2:3 ni cambie el ancho de pantalla.
 */
export function Portada({ url, titulo, variante = 'tarjeta', id = 0 }: {
  url?: string | null
  titulo: string
  variante?: 'tarjeta' | 'grande' | 'mini'
  id?: number
}) {
  const clase = variante === 'grande' ? 'portada-grande' : variante === 'mini' ? 'mini-portada' : 'portada'
  if (!url) {
    return <div className={`${clase} ${estilos.portada}`} role="img" aria-label={`Portada de ${titulo}`}>{EMOJIS[id % EMOJIS.length]}</div>
  }
  if (variante === 'mini') {
    return <div className={`${clase} ${estilos.portada}`} role="img" aria-label={`Portada de ${titulo}`}
      style={{ backgroundImage: `url('${url}')`, color: 'transparent', fontSize: 0 }} />
  }
  return (
    <div className={`${clase} ${estilos.portada} ${estilos.conImagen}`} role="img" aria-label={`Portada de ${titulo}`}
      style={{ '--portada': `url('${url}')` } as CSSProperties}>
      <span className={estilos.fondo} aria-hidden="true" />
      <img className={estilos.imagen} src={url} alt="" loading="lazy" />
    </div>
  )
}
