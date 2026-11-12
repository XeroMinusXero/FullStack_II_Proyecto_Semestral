import type { InputHTMLAttributes, ReactNode } from 'react'

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  etiqueta: string
  error?: string
  id: string
  ayuda?: ReactNode
}

/** Campo de formulario con las clases del sitio original (.campo / .error / .mensaje-error). */
export function Campo({ etiqueta, error, id, ayuda, ...input }: Props) {
  return (
    <div className={`campo${error ? ' error' : ''}`}>
      <label htmlFor={id}>{etiqueta}</label>
      <input id={id} aria-invalid={!!error} {...input} />
      {ayuda && !error && <small className="text-muted">{ayuda}</small>}
      <span className="mensaje-error">{error}</span>
    </div>
  )
}
