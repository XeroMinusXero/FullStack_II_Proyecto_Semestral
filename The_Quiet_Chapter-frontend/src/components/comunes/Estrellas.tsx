/** Calificacion visual de 1 a 5. Con onElegir se vuelve un selector accesible (HU-083). */
export function Estrellas({ valor, onElegir, tamano = '1.1rem' }: { valor: number | null; onElegir?: (v: number) => void; tamano?: string }) {
  const v = Math.round(valor ?? 0)
  if (!onElegir) {
    return <span className="estrellas" aria-label={`${valor ?? 0} de 5 estrellas`} style={{ fontSize: tamano }}>{'★'.repeat(v)}{'☆'.repeat(5 - v)}</span>
  }
  return (
    <span role="radiogroup" aria-label="Calificación" className="estrellas" style={{ fontSize: tamano }}>
      {[1, 2, 3, 4, 5].map((n) => (
        <button key={n} type="button" role="radio" aria-checked={n === v} aria-label={`${n} estrellas`}
          onClick={() => onElegir(n)} className="estrella-boton">
          {n <= v ? '★' : '☆'}
        </button>
      ))}
    </span>
  )
}
