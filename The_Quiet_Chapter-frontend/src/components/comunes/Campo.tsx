import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'

interface Base {
  etiqueta: string
  error?: string
  id: string
  ayuda?: ReactNode
}

/** Envoltorio comun: etiqueta, control, ayuda y mensaje de error con las clases del sitio (.campo / .error). */
function Marco({ etiqueta, error, id, ayuda, children }: Base & { children: ReactNode }) {
  return (
    <div className={`campo${error ? ' error' : ''}`}>
      <label htmlFor={id}>{etiqueta}</label>
      {children}
      {ayuda && !error && <small className="text-muted">{ayuda}</small>}
      <span className="mensaje-error" id={`${id}-error`}>{error}</span>
    </div>
  )
}

/** Campo de texto (input) con su etiqueta y su error. */
export function Campo({ etiqueta, error, id, ayuda, ...input }: Base & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <Marco etiqueta={etiqueta} error={error} id={id} ayuda={ayuda}>
      <input id={id} aria-invalid={!!error} aria-describedby={error ? `${id}-error` : undefined} {...input} />
    </Marco>
  )
}

/** Lista desplegable (select) con su etiqueta y su error. Las opciones van como children. */
export function CampoSeleccion({ etiqueta, error, id, ayuda, children, ...select }: Base & SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <Marco etiqueta={etiqueta} error={error} id={id} ayuda={ayuda}>
      <select id={id} aria-invalid={!!error} aria-describedby={error ? `${id}-error` : undefined} {...select}>{children}</select>
    </Marco>
  )
}

/** Texto largo (textarea) con su etiqueta, su error y, si se indica maxLength, un contador de caracteres. */
export function CampoTextoLargo({ etiqueta, error, id, ayuda, ...area }: Base & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  const largo = String(area.value ?? '').length
  const contador = area.maxLength ? `${largo}/${area.maxLength}` : undefined
  return (
    <Marco etiqueta={etiqueta} error={error} id={id} ayuda={ayuda ?? contador}>
      <textarea id={id} aria-invalid={!!error} aria-describedby={error ? `${id}-error` : undefined} {...area} />
    </Marco>
  )
}
