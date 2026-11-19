/** Pestanas internas de una pagina con el estilo .tabs del sitio original (Dashboard, Promociones). */
export function Pestanas<T extends string>({ opciones, valor, onCambiar, etiqueta }: {
  opciones: { valor: T; texto: string }[]
  valor: T
  onCambiar: (v: T) => void
  etiqueta: string
}) {
  return (
    <div className="tabs" role="tablist" aria-label={etiqueta}>
      {opciones.map((o) => (
        <button key={o.valor} type="button" role="tab" aria-selected={o.valor === valor}
          className={o.valor === valor ? 'activo' : undefined} onClick={() => onCambiar(o.valor)}>
          {o.texto}
        </button>
      ))}
    </div>
  )
}
