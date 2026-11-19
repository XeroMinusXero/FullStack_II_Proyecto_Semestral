import { LIMITES, MENSAJES, reglas, type Errores, soloErrores } from './reglas'
import type { Alcance, TipoDescuento } from '../../interfaces/promo'

/** HU-100: codigo que escribe el cliente en el carrito (mismo formato que crea el admin). */
export function validarCodigoCupon(codigo: string): string | undefined {
  const c = codigo.trim().toUpperCase()
  if (!c) return 'Ingresa un código'
  if (!LIMITES.codigoCupon.test(c)) return 'El código tiene de 3 a 40 caracteres: letras, números, - o _'
}

/** promo-service Descuentos: porcentaje entre 1 y 100; monto fijo mayor a 0. */
function descuento(tipo: TipoDescuento, valor: number): string | undefined {
  if (tipo === 'PORCENTAJE') return valor >= 1 && valor <= 100 ? undefined : 'El porcentaje debe estar entre 1 y 100'
  return valor > 0 ? undefined : 'El monto debe ser mayor a 0'
}

function vigencia(inicio: string, fin: string): { fechaInicio?: string; fechaFin?: string } {
  if (!inicio) return { fechaInicio: MENSAJES.requerido }
  if (!fin) return { fechaFin: MENSAJES.requerido }
  return fin > inicio ? {} : { fechaFin: 'La fecha de término debe ser posterior al inicio' }
}

export interface DatosCupon { codigo: string; tipoDescuento: TipoDescuento; valor: number; montoMinimo: number; fechaInicio: string; fechaFin: string }
/** HU-092..099: cupon creado por el administrador. */
export function validarCupon(f: DatosCupon): Errores<DatosCupon> {
  return soloErrores({
    codigo: validarCodigoCupon(f.codigo),
    valor: descuento(f.tipoDescuento, f.valor),
    montoMinimo: reglas.numero(f.montoMinimo) ? undefined : 'El monto mínimo no puede ser negativo',
    ...vigencia(f.fechaInicio, f.fechaFin),
  })
}

export interface DatosPromocion { nombre: string; tipoDescuento: TipoDescuento; valor: number; fechaInicio: string; fechaFin: string; alcances: Alcance[] }
/** HU-111..113: promocion con al menos un alcance (libro, categoria, genero o editorial). */
export function validarPromocion(f: DatosPromocion): Errores<DatosPromocion> {
  return soloErrores({
    nombre: !reglas.requerido(f.nombre) ? MENSAJES.requerido
      : !reglas.maximo(f.nombre, LIMITES.nombrePromocion) ? MENSAJES.maximo(LIMITES.nombrePromocion) : undefined,
    valor: descuento(f.tipoDescuento, f.valor),
    alcances: f.alcances.length ? undefined : 'Agrega al menos un alcance (libro, categoría, género o editorial)',
    ...vigencia(f.fechaInicio, f.fechaFin),
  })
}
