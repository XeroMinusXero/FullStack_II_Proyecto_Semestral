import { clp } from '../../utils/formato'
import estilos from './GraficoBarras.module.css'

/** Grafico de barras SVG sin dependencias (HU-141). Vacio = mensaje, sin errores. */
export function GraficoBarras({ datos, alto = 220, moneda = true }: { datos: Record<string, number>; alto?: number; moneda?: boolean }) {
  const entradas = Object.entries(datos)
  const max = Math.max(0, ...entradas.map(([, v]) => Number(v)))
  if (!entradas.length || max === 0) return <p className="vacio">No hay ventas en el período seleccionado.</p>
  const ancho = Math.max(entradas.length * 34, 760)
  const barra = ancho / entradas.length
  const cada = Math.ceil(entradas.length / 12)
  return (
    <div className={estilos.contenedor}>
      <svg viewBox={`0 0 ${ancho} ${alto + 40}`} className={estilos.svg} role="img" aria-label="Evolución de ventas">
        {[0.25, 0.5, 0.75, 1].map((f) => (
          <line key={f} x1={0} x2={ancho} y1={alto - alto * f} y2={alto - alto * f} className={estilos.guia} />
        ))}
        {entradas.map(([clave, v], i) => {
          const h = (Number(v) / max) * (alto - 10)
          return (
            <g key={clave}>
              <rect x={i * barra + (barra - Math.min(barra * 0.7, 56)) / 2} y={alto - h} width={Math.min(barra * 0.7, 56)} height={Math.max(h, 0)} rx={3} className={estilos.barra}>
                <title>{`${clave}: ${moneda ? clp(Number(v)) : v}`}</title>
              </rect>
              {i % cada === 0 && (
                <text x={i * barra + barra / 2} y={alto + 16} textAnchor="middle" className={estilos.etiqueta}>{clave.length === 10 ? `${clave.slice(8, 10)}-${clave.slice(5, 7)}` : clave}</text>
              )}
            </g>
          )
        })}
      </svg>
      <div className={estilos.maximo}>Máximo: {moneda ? clp(max) : max}</div>
    </div>
  )
}
