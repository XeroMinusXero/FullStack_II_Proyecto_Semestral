/** Formatos chilenos: pesos sin decimales y fechas dd-mm-aaaa. */
const pesos = new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 })

export const clp = (valor: number | null | undefined) => pesos.format(Number(valor ?? 0))

export function fecha(valor: string | null | undefined, conHora = false) {
  if (!valor) return '—'
  // 'aaaa-mm-dd' se interpreta como fecha local (new Date() la tomaria en UTC y en Chile mostraria el dia anterior).
  const soloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor)
  const d = soloFecha ? new Date(Number(soloFecha[1]), Number(soloFecha[2]) - 1, Number(soloFecha[3])) : new Date(valor)
  if (Number.isNaN(d.getTime())) return valor
  return conHora
    ? d.toLocaleString('es-CL', { dateStyle: 'short', timeStyle: 'short' })
    : d.toLocaleDateString('es-CL', { dateStyle: 'medium' })
}

export const ETIQUETA_ESTADO_PEDIDO: Record<string, string> = {
  PAGADO: 'Pagado',
  EN_PREPARACION: 'En preparación',
  ENVIADO: 'Enviado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
}

export function claseEstado(estado: string): string {
  if (estado === 'CANCELADO' || estado === 'RECHAZADO' || estado === 'RETIRADA' || estado === 'FALLIDA') return 'badge-agotado'
  if (estado === 'ENTREGADO' || estado === 'APROBADO' || estado === 'PUBLICADA' || estado === 'CONFIRMADA' || estado === 'ENVIADA') return 'badge-disponible'
  return 'badge-oferta'
}

export const hoyIso = () => new Date().toISOString().slice(0, 10)

/** "15%" o "$5.000", segun el tipo de descuento de un cupon o promocion. */
export function textoDescuento(tipo: 'PORCENTAJE' | 'MONTO_FIJO', valor: number): string {
  return tipo === 'PORCENTAJE' ? `${valor}%` : clp(valor)
}
