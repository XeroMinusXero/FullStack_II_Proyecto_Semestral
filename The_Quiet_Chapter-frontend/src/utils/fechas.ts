/**
 * Fechas para <input type="datetime-local"> (formato 'aaaa-mm-ddThh:mm', hora local).
 * El backend recibe y devuelve LocalDateTime sin zona, asi que se trabaja siempre en hora local.
 */
const desfaseMs = () => new Date().getTimezoneOffset() * 60000

/** Fecha y hora local de hoy (o dentro de `dias` dias) lista para un datetime-local. */
export function fechaHoraLocal(dias = 0): string {
  return new Date(Date.now() + dias * 86400000 - desfaseMs()).toISOString().slice(0, 16)
}

/** Recorta un LocalDateTime del backend ('2026-11-01T00:00:00') al formato del input ('2026-11-01T00:00'). */
export function aFechaHoraLocal(valor?: string | null): string {
  return valor ? valor.slice(0, 16) : ''
}
