import { CampoSeleccion, Campo } from '../comunes/Campo'
import { REGIONES } from '../../utils/regiones'
import { LIMITES, type DatosDireccion } from '../../utils/validaciones'
import type { useFormulario } from '../../hooks/useFormulario'

/** Estado de un formulario de direccion creado con useFormulario<DatosDireccion>. */
export type FormularioDireccion = ReturnType<typeof useFormulario<DatosDireccion>>

/**
 * Los 4 campos de una direccion chilena. Se usa en "Mis direcciones" (HU-044) y en el paso 1 del
 * checkout (HU-023); ambos validan con validarDireccion, asi que la regla es una sola.
 */
export function CamposDireccion({ form, prefijo = 'dir' }: { form: FormularioDireccion; prefijo?: string }) {
  return (
    <>
      <Campo id={`${prefijo}-calle`} etiqueta="Calle" autoComplete="address-line1" maxLength={LIMITES.calle} {...form.campo('calle')} />
      <Campo id={`${prefijo}-numero`} etiqueta="Número / Depto" maxLength={LIMITES.numeroDireccion} {...form.campo('numero')} />
      <Campo id={`${prefijo}-comuna`} etiqueta="Comuna" autoComplete="address-level2" maxLength={LIMITES.comuna} {...form.campo('comuna')} />
      <CampoSeleccion id={`${prefijo}-region`} etiqueta="Región" {...form.campo('region')}>
        <option value="">Seleccione una región</option>
        {REGIONES.map((r) => <option key={r} value={r}>{r}</option>)}
      </CampoSeleccion>
    </>
  )
}
