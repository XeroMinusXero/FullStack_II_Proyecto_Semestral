import { useCallback, useState, type ChangeEvent } from 'react'
import { sinErrores, type Errores } from '../utils/validaciones'

type Entrada = HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement

/**
 * Estado de un formulario: valores, errores por campo y validacion.
 *
 *   const form = useFormulario({ email: '', contrasena: '' })
 *   <Campo id="email" etiqueta="Correo" {...form.campo('email')} />
 *   if (!form.validar(validarLogin)) return   // muestra los errores bajo cada campo
 *
 * Al escribir en un campo con error, ese error se borra (el usuario ve que lo esta corrigiendo).
 */
export function useFormulario<T extends object>(inicial: T) {
  const [valores, setValores] = useState<T>(inicial)
  const [errores, setErrores] = useState<Errores<T>>({})

  const cambiar = useCallback(<K extends keyof T>(clave: K, valor: T[K]) => {
    setValores((v) => ({ ...v, [clave]: valor }))
    setErrores((e) => {
      if (!e[clave]) return e
      const resto = { ...e }
      delete resto[clave]
      return resto
    })
  }, [])

  /** Cambia varios campos a la vez (por ejemplo desde un subcomponente) y borra sus errores. */
  const combinar = useCallback((parcial: Partial<T>) => {
    setValores((v) => ({ ...v, ...parcial }))
    setErrores((e) => {
      const resto = { ...e }
      for (const clave of Object.keys(parcial) as (keyof T)[]) delete resto[clave]
      return resto
    })
  }, [])

  /** Props para <Campo> o un <select>/<textarea>: value, error y onChange del campo de texto `clave`. */
  const campo = <K extends keyof T>(clave: K) => ({
    value: String(valores[clave] ?? ''),
    error: errores[clave],
    onChange: (e: ChangeEvent<Entrada>) => cambiar(clave, e.target.value as T[K]),
  })

  /** Corre el validador, deja visibles sus errores y devuelve true si el formulario es valido. */
  const validar = (validador: (v: T) => Errores<T>) => {
    const e = validador(valores)
    setErrores(e)
    return sinErrores(e)
  }

  const reiniciar = (nuevo: T = inicial) => {
    setValores(nuevo)
    setErrores({})
  }

  return { valores, setValores, errores, setErrores, cambiar, combinar, campo, validar, reiniciar }
}
