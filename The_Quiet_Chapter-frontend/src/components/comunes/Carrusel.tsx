import { useEffect, useRef, useState, type ReactNode } from 'react'
import estilos from './Carrusel.module.css'

/**
 * Cinta horizontal desplazable con flechas (se usa para las categorias del inicio): a medida que se
 * agregan elementos la fila crece hacia la derecha en vez de apilar filas nuevas.
 */
export function Carrusel({ children, etiqueta }: { children: ReactNode; etiqueta: string }) {
  const pista = useRef<HTMLDivElement>(null)
  const [bordes, setBordes] = useState({ inicio: true, fin: true })

  useEffect(() => {
    const el = pista.current
    if (!el) return
    const medir = () => setBordes({ inicio: el.scrollLeft <= 4, fin: el.scrollLeft + el.clientWidth >= el.scrollWidth - 4 })
    const observador = new ResizeObserver(medir)
    observador.observe(el)
    el.addEventListener('scroll', medir, { passive: true })
    return () => { observador.disconnect(); el.removeEventListener('scroll', medir) }
  }, [])

  const mover = (dir: 1 | -1) => pista.current?.scrollBy({ left: dir * pista.current.clientWidth * 0.8, behavior: 'smooth' })

  return (
    <div className={estilos.carrusel}>
      {!bordes.inicio && <button type="button" className={`${estilos.flecha} ${estilos.izquierda}`} aria-label="Anterior" onClick={() => mover(-1)}>‹</button>}
      <div ref={pista} className={estilos.pista} role="list" aria-label={etiqueta}>{children}</div>
      {!bordes.fin && <button type="button" className={`${estilos.flecha} ${estilos.derecha}`} aria-label="Siguiente" onClick={() => mover(1)}>›</button>}
    </div>
  )
}
