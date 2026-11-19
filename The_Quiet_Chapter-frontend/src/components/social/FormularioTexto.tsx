import { useState, type FormEvent } from 'react'
import { CampoTextoLargo } from '../comunes/Campo'

/**
 * Formulario de un solo texto largo con su validacion: se usa para publicar y editar una resena (HU-083/086)
 * y para reportarla (HU-089). `onEnviar` devuelve true si el backend lo acepto; en ese caso se limpia.
 */
export function FormularioTexto({ id, etiqueta, inicial = '', maximo, placeholder, textoBoton, peligro, validar, onEnviar }: {
  id: string
  etiqueta: string
  inicial?: string
  maximo: number
  placeholder?: string
  textoBoton: string
  peligro?: boolean
  validar: (texto: string) => string | undefined
  onEnviar: (texto: string) => Promise<boolean>
}) {
  const [texto, setTexto] = useState(inicial)
  const [error, setError] = useState<string>()
  const [enviando, setEnviando] = useState(false)

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    const invalido = validar(texto)
    setError(invalido)
    if (invalido) return
    setEnviando(true)
    const ok = await onEnviar(texto.trim())
    setEnviando(false)
    if (ok) setTexto('')
  }

  return (
    <form onSubmit={enviar} noValidate className="form-plano">
      <CampoTextoLargo id={id} etiqueta={etiqueta} rows={4} maxLength={maximo} placeholder={placeholder} value={texto} error={error}
        onChange={(e) => { setTexto(e.target.value); setError(undefined) }} />
      <button className={`btn${peligro ? ' btn-peligro' : ''}`} type="submit" disabled={enviando}>{enviando ? 'Enviando...' : textoBoton}</button>
    </form>
  )
}
