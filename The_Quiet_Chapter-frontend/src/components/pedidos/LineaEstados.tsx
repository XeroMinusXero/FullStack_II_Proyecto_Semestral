import estilos from './LineaEstados.module.css'
import { ETIQUETA_ESTADO_PEDIDO } from '../../utils/formato'
import type { EstadoPedido } from '../../interfaces/pedido'

const FLUJO: EstadoPedido[] = ['PAGADO', 'EN_PREPARACION', 'ENVIADO', 'ENTREGADO']

/** HU-042: seguimiento visual del estado del pedido. */
export function LineaEstados({ estado }: { estado: EstadoPedido }) {
  if (estado === 'CANCELADO') return <p className="badge badge-agotado">Pedido cancelado</p>
  const actual = FLUJO.indexOf(estado)
  return (
    <ol className={estilos.linea}>
      {FLUJO.map((e, i) => (
        <li key={e} className={i <= actual ? estilos.hecho : undefined} aria-current={i === actual ? 'step' : undefined}>
          <span className={estilos.punto}>{i < actual ? '✓' : i + 1}</span>
          <span>{ETIQUETA_ESTADO_PEDIDO[e]}</span>
        </li>
      ))}
    </ol>
  )
}
