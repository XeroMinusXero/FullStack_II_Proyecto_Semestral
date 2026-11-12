/** Control +/- del carrito (HU-015). Bajar a 0 elimina el libro. */
export function ControlCantidad({ valor, maximo, deshabilitado, onCambiar }: { valor: number; maximo: number; deshabilitado?: boolean; onCambiar: (v: number) => void }) {
  return (
    <div className="control-cantidad">
      <button type="button" className="menos" aria-label="Disminuir" disabled={deshabilitado} onClick={() => onCambiar(valor - 1)}>−</button>
      <input type="number" value={valor} min={0} max={maximo} aria-label="Cantidad" disabled={deshabilitado}
        onChange={(e) => {
          const v = Number(e.target.value)
          if (!Number.isNaN(v)) onCambiar(v)
        }} />
      <button type="button" className="mas" aria-label="Aumentar" disabled={deshabilitado || valor >= maximo} onClick={() => onCambiar(valor + 1)}>+</button>
    </div>
  )
}
