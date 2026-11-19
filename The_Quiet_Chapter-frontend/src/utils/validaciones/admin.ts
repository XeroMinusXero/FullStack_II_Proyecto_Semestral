import { LIMITES, MENSAJES, reglas, type Errores, soloErrores } from './reglas'

export interface DatosMetodoEnvio { nombre: string; costoBase: number; plazoEstimadoDias: number }
/** Metodo de envio (checkout-service MetodoEnvioSolicitud). */
export function validarMetodoEnvio(f: DatosMetodoEnvio): Errores<DatosMetodoEnvio> {
  return soloErrores({
    nombre: !reglas.requerido(f.nombre) ? MENSAJES.requerido
      : !reglas.maximo(f.nombre, LIMITES.nombreOpcion) ? MENSAJES.maximo(LIMITES.nombreOpcion) : undefined,
    costoBase: reglas.numero(f.costoBase) ? undefined : 'El costo no puede ser negativo',
    plazoEstimadoDias: reglas.entero(f.plazoEstimadoDias) ? undefined : 'Plazo en días enteros (0 o más)',
  })
}

/** Medio de pago (checkout-service MedioPagoSolicitud). */
export function validarMedioPago(nombre: string): string | undefined {
  if (!reglas.requerido(nombre)) return MENSAJES.requerido
  if (!reglas.maximo(nombre, LIMITES.nombreOpcion)) return MENSAJES.maximo(LIMITES.nombreOpcion)
}

export interface DatosBoletin { asunto: string; contenido: string }
/** HU-106/107: borrador de boletin. */
export function validarBoletin(f: DatosBoletin): Errores<DatosBoletin> {
  return soloErrores({
    asunto: f.asunto.trim().length < 3 ? MENSAJES.minimo(3)
      : !reglas.maximo(f.asunto, LIMITES.asuntoBoletin) ? MENSAJES.maximo(LIMITES.asuntoBoletin) : undefined,
    contenido: f.contenido.trim().length < LIMITES.contenidoBoletinMin ? MENSAJES.minimo(LIMITES.contenidoBoletinMin) : undefined,
  })
}

/** HU-058: numero de seguimiento del envio. */
export function validarSeguimiento(numero: string): string | undefined {
  const largo = numero.trim().length
  if (largo < LIMITES.seguimientoMin) return MENSAJES.minimo(LIMITES.seguimientoMin)
  if (largo > LIMITES.seguimientoMax) return MENSAJES.maximo(LIMITES.seguimientoMax)
}

/**
 * Rango de fechas 'aaaa-mm-dd' (reportes y pedidos). Con `obligatorio`, ambas fechas son requeridas.
 * Devuelve el mensaje de error o undefined.
 */
export function validarRangoFechas(desde: string, hasta: string, obligatorio = false): string | undefined {
  if (obligatorio && (!desde || !hasta)) return 'Indica fecha desde y hasta'
  if (desde && hasta && desde > hasta) return 'La fecha desde no puede ser posterior a la fecha hasta'
}
