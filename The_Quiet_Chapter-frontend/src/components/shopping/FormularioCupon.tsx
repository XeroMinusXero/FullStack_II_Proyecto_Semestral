import { useState, type FormEvent } from 'react'
import { useCarrito } from '../../context/carrito'
import { mensajeDe } from '../../api/http'
import estilos from './FormularioCupon.module.css'

/** HU-100/101: aplicar y quitar cupon (validado por promo-service). */
export function FormularioCupon({ codigoAplicado }: { codigoAplicado: string | null }) {
  const { aplicarCupon, quitarCupon } = useCarrito()
  const [codigo, setCodigo] = useState('')
  const [error, setError] = useState('')
  const [ocupado, setOcupado] = useState(false)

  const aplicar = async (e: FormEvent) => {
    e.preventDefault()
    if (codigo.trim().length < 3) { setError('Ingresa un código de al menos 3 caracteres'); return }
    setOcupado(true); setError('')
    try { await aplicarCupon(codigo.trim()); setCodigo('') } catch (err) { setError(mensajeDe(err)) } finally { setOcupado(false) }
  }

  if (codigoAplicado) {
    return (
      <div className={estilos.aplicado}>
        <span>Cupón <strong>{codigoAplicado}</strong> aplicado</span>
        <button type="button" className="btn btn-secundario" disabled={ocupado}
          onClick={async () => { setOcupado(true); try { await quitarCupon() } finally { setOcupado(false) } }}>Quitar cupón</button>
      </div>
    )
  }
  return (
    <form className={estilos.formulario} onSubmit={aplicar} noValidate>
      <label htmlFor="cupon" className={estilos.etiqueta}>Cupón de descuento</label>
      <div className={estilos.fila}>
        <input id="cupon" value={codigo} onChange={(e) => setCodigo(e.target.value.toUpperCase())} placeholder="Ingrese código" className={estilos.input} />
        <button className="btn" type="submit" disabled={ocupado}>Aplicar</button>
      </div>
      {error && <small className="text-danger" role="alert">{error}</small>}
    </form>
  )
}
