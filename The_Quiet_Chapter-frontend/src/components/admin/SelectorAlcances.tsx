import { useState } from 'react'
import type { Alcance, TipoAlcance } from '../../interfaces/promo'
import type { Clasificacion } from '../../interfaces/catalogo'
import estilos from './SelectorAlcances.module.css'

const TIPOS: { valor: TipoAlcance; texto: string }[] = [
  { valor: 'LIBRO', texto: 'Libro' }, { valor: 'CATEGORIA', texto: 'Categoría' },
  { valor: 'GENERO', texto: 'Género' }, { valor: 'EDITORIAL', texto: 'Editorial' },
]

/**
 * HU-111: a que se aplica una promocion. Se elige tipo (libro, categoria, genero, editorial) y elemento,
 * y cada alcance agregado queda como una etiqueta que se puede quitar.
 */
export function SelectorAlcances({ alcances, opciones, nombre, error, onCambiar }: {
  alcances: Alcance[]
  opciones: Record<TipoAlcance, Clasificacion[]>
  nombre: (a: Alcance) => string
  error?: string
  onCambiar: (alcances: Alcance[]) => void
}) {
  const [tipo, setTipo] = useState<TipoAlcance>('CATEGORIA')
  const [elemento, setElemento] = useState('')

  const agregar = () => {
    const nuevo: Alcance = { tipo, referenciaId: Number(elemento) }
    if (!alcances.some((a) => a.tipo === nuevo.tipo && a.referenciaId === nuevo.referenciaId)) onCambiar([...alcances, nuevo])
    setElemento('')
  }

  return (
    <div className={`campo${error ? ' error' : ''}`}>
      <label htmlFor="alcance-elemento">Alcance</label>
      <div className={estilos.fila}>
        <select className={`form-select ${estilos.tipo}`} aria-label="Tipo de alcance" value={tipo}
          onChange={(e) => { setTipo(e.target.value as TipoAlcance); setElemento('') }}>
          {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.texto}</option>)}
        </select>
        <select id="alcance-elemento" className={`form-select ${estilos.elemento}`} value={elemento} onChange={(e) => setElemento(e.target.value)}>
          <option value="">Selecciona…</option>
          {opciones[tipo].map((o) => <option key={o.id} value={o.id}>{o.nombre}</option>)}
        </select>
        <button type="button" className="btn btn-secundario" disabled={!elemento} onClick={agregar}>Agregar</button>
      </div>
      <div className={estilos.etiquetas}>
        {alcances.length === 0 && <small className="text-muted">Sin alcances.</small>}
        {alcances.map((a) => (
          <span key={`${a.tipo}${a.referenciaId}`} className={estilos.etiqueta}>
            {TIPOS.find((t) => t.valor === a.tipo)?.texto}: {nombre(a)}
            <button type="button" aria-label={`Quitar ${nombre(a)}`} onClick={() => onCambiar(alcances.filter((x) => x !== a))}>×</button>
          </span>
        ))}
      </div>
      <span className="mensaje-error">{error}</span>
    </div>
  )
}
