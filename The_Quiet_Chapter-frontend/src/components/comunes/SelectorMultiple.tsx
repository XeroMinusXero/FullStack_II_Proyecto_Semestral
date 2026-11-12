import { useEffect, useId, useRef, useState } from 'react'
import estilos from './SelectorMultiple.module.css'

interface Opcion { id: number; nombre: string }

/**
 * Desplegable con casillas para elegir varias opciones (categorias, generos). Reemplaza la nube de
 * botones del catalogo, que crecia sin limite a medida que se registraban mas categorias y generos.
 */
export function SelectorMultiple({ etiqueta, opciones, seleccion, onAlternar, onLimpiar }: {
  etiqueta: string
  opciones: Opcion[]
  seleccion: number[]
  onAlternar: (id: number) => void
  onLimpiar: () => void
}) {
  const [abierto, setAbierto] = useState(false)
  const [busqueda, setBusqueda] = useState('')
  const raiz = useRef<HTMLDivElement>(null)
  const idPanel = useId()

  useEffect(() => {
    if (!abierto) return
    const fuera = (e: MouseEvent) => { if (!raiz.current?.contains(e.target as Node)) setAbierto(false) }
    const escape = (e: KeyboardEvent) => { if (e.key === 'Escape') setAbierto(false) }
    document.addEventListener('mousedown', fuera)
    document.addEventListener('keydown', escape)
    return () => { document.removeEventListener('mousedown', fuera); document.removeEventListener('keydown', escape) }
  }, [abierto])

  const normal = (t: string) => t.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
  const visibles = busqueda ? opciones.filter((o) => normal(o.nombre).includes(normal(busqueda))) : opciones
  const nombres = opciones.filter((o) => seleccion.includes(o.id)).map((o) => o.nombre)
  const resumen = nombres.length === 0 ? 'Todas' : nombres.length === 1 ? nombres[0] : `${nombres.length} seleccionadas`

  return (
    <div className={estilos.selector} ref={raiz}>
      <span className={estilos.etiqueta}>{etiqueta}</span>
      <button type="button" className={`${estilos.disparador} ${nombres.length ? estilos.conValor : ''}`}
        aria-expanded={abierto} aria-controls={idPanel} onClick={() => setAbierto(!abierto)}>
        <span className={estilos.resumen}>{resumen}</span>
        <span className={estilos.chevron} aria-hidden="true">▾</span>
      </button>
      {abierto && (
        <div className={estilos.panel} id={idPanel} role="group" aria-label={etiqueta}>
          {opciones.length > 8 && (
            <input type="search" className={estilos.buscar} placeholder={`Buscar ${etiqueta.toLowerCase()}...`} value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)} autoFocus aria-label={`Buscar ${etiqueta}`} />
          )}
          <ul className={estilos.lista}>
            {visibles.length === 0 && <li className={estilos.vacio}>Sin coincidencias</li>}
            {visibles.map((o) => (
              <li key={o.id}>
                <label className={estilos.opcion}>
                  <input type="checkbox" className="form-check-input m-0" checked={seleccion.includes(o.id)} onChange={() => onAlternar(o.id)} />
                  <span>{o.nombre}</span>
                </label>
              </li>
            ))}
          </ul>
          <div className={estilos.pie}>
            <button type="button" className={estilos.enlace} disabled={!seleccion.length} onClick={onLimpiar}>Quitar selección</button>
            <button type="button" className={estilos.listo} onClick={() => setAbierto(false)}>Listo</button>
          </div>
        </div>
      )}
    </div>
  )
}
