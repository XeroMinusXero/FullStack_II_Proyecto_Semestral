import { useState, type FormEvent } from 'react'
import { Campo } from '../comunes/Campo'
import { validarPrecio } from '../../utils/validaciones'

/** HU-048: cambio de precio de un libro (CLP, IVA incluido). */
export function EditorPrecio({ precioActual, onGuardar }: { precioActual: number; onGuardar: (precio: number) => Promise<boolean> }) {
  const [valor, setValor] = useState(String(precioActual))
  const [error, setError] = useState<string>()
  const [enviando, setEnviando] = useState(false)

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    const invalido = validarPrecio(valor)
    setError(invalido)
    if (invalido) return
    setEnviando(true)
    await onGuardar(Number(valor))
    setEnviando(false)
  }

  return (
    <form onSubmit={guardar} noValidate className="form-plano">
      <Campo id="nuevo-precio" etiqueta="Nuevo precio (CLP, IVA incluido)" type="number" min={1} value={valor} error={error}
        ayuda="Los carritos que tengan este libro verán el aviso de cambio de precio."
        onChange={(e) => { setValor(e.target.value); setError(undefined) }} />
      <div className="text-end"><button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Guardar precio'}</button></div>
    </form>
  )
}
