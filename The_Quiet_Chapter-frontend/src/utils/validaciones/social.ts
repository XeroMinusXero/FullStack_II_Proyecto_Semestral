import { LIMITES, MENSAJES } from './reglas'

/** HU-083/086: texto de una resena (social-service Resena: entre 10 y 2000 caracteres). */
export function validarResena(texto: string): string | undefined {
  const largo = texto.trim().length
  if (largo < LIMITES.resenaMin) return `La reseña debe tener al menos ${LIMITES.resenaMin} caracteres`
  if (largo > LIMITES.resenaMax) return `La reseña no puede superar ${LIMITES.resenaMax} caracteres`
}

/** HU-089: motivo de un reporte. */
export function validarReporte(motivo: string): string | undefined {
  const largo = motivo.trim().length
  if (largo < LIMITES.motivoReporteMin) return MENSAJES.minimo(LIMITES.motivoReporteMin)
  if (largo > LIMITES.motivoReporteMax) return MENSAJES.maximo(LIMITES.motivoReporteMax)
}
