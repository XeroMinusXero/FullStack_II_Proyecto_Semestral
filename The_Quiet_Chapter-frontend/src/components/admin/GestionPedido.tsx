import { useState } from 'react'
import { pedidosApi } from '../../api/pedidos'
import { useAccion } from '../../hooks/useAccion'
import { Avisos } from '../comunes/Avisos'
import { Confirmar } from '../comunes/Confirmar'
import { LineaEstados } from '../pedidos/LineaEstados'
import { ETIQUETA_ESTADO_PEDIDO, clp } from '../../utils/formato'
import { validarSeguimiento } from '../../utils/validaciones'
import type { EstadoPedido, Pedido } from '../../interfaces/pedido'

/** Transiciones validas (mismas reglas que Pedido.java en order-service). */
const SIGUIENTE: Partial<Record<EstadoPedido, EstadoPedido>> = { PAGADO: 'EN_PREPARACION', EN_PREPARACION: 'ENVIADO', ENVIADO: 'ENTREGADO' }

/**
 * HU-056..060: detalle de un pedido para el administrador, con avance de estado, numero de seguimiento y cancelacion.
 * Cada operacion devuelve el pedido actualizado; `onActualizado` lo entrega a la pagina para refrescar el listado.
 */
export function GestionPedido({ pedido, onActualizado }: { pedido: Pedido; onActualizado: (p: Pedido) => void }) {
  const accion = useAccion()
  const [seguimiento, setSeguimiento] = useState(pedido.entrega.numeroSeguimiento ?? '')
  const [errorSeguimiento, setErrorSeguimiento] = useState<string>()
  const [cancelar, setCancelar] = useState(false)
  const siguiente = SIGUIENTE[pedido.estado]

  const ejecutar = (operacion: () => Promise<Pedido>, mensajeOk: string) =>
    accion.ejecutar(async () => onActualizado(await operacion()), mensajeOk)

  const guardarSeguimiento = () => {
    const invalido = validarSeguimiento(seguimiento)
    setErrorSeguimiento(invalido)
    if (!invalido) ejecutar(() => pedidosApi.seguimiento(pedido.id, seguimiento.trim()), 'Seguimiento registrado')
  }

  return (
    <>
      <Avisos estado={accion} />
      <LineaEstados estado={pedido.estado} />
      <div className="row g-3">
        <div className="col-md-6">
          <h3>Cliente</h3>
          <p className="mb-1">{pedido.clienteNombre ?? `#${pedido.clienteId}`} · {pedido.clienteEmail}</p>
          <p className="mb-1">{pedido.entrega.direccionFormateada}</p>
          <p>{pedido.entrega.metodoEnvio} · {pedido.medioPago} · Ref. {pedido.referenciaPago}</p>
        </div>
        <div className="col-md-6">
          <h3>Acciones</h3>
          {siguiente && (
            <button type="button" className="btn mb-2" onClick={() => ejecutar(() => pedidosApi.cambiarEstado(pedido.id, siguiente), 'Estado actualizado y cliente notificado')}>
              Marcar como {ETIQUETA_ESTADO_PEDIDO[siguiente].toLowerCase()}
            </button>
          )}
          {pedido.estado === 'ENVIADO' && (
            <div className={`campo mb-2${errorSeguimiento ? ' error' : ''}`}>
              <div className="d-flex gap-2">
                <input placeholder="N° de seguimiento" aria-label="Número de seguimiento" maxLength={120} value={seguimiento} aria-invalid={!!errorSeguimiento}
                  onChange={(e) => { setSeguimiento(e.target.value); setErrorSeguimiento(undefined) }} />
                <button type="button" className="btn btn-secundario" onClick={guardarSeguimiento}>Guardar</button>
              </div>
              <span className="mensaje-error">{errorSeguimiento}</span>
            </div>
          )}
          {pedido.puedeCancelarse && <button type="button" className="btn btn-peligro" onClick={() => setCancelar(true)}>Cancelar pedido</button>}
          {!siguiente && !pedido.puedeCancelarse && pedido.estado !== 'ENVIADO' && <p className="text-muted">Pedido cerrado: no admite más cambios.</p>}
        </div>
      </div>
      <div className="tabla-scroll">
        <table>
          <thead><tr><th>Libro</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
          <tbody>{pedido.detalles.map((d) => <tr key={d.id}><td>{d.tituloLibro}</td><td>{d.cantidad}</td><td>{clp(d.precioUnitario)}</td><td>{clp(d.subtotal)}</td></tr>)}</tbody>
        </table>
      </div>
      <p className="text-end mt-2 mb-0">Envío {clp(pedido.importes.costoEnvio)} · Descuento {clp(pedido.importes.descuento)} · <strong>Total {clp(pedido.importes.total)}</strong></p>

      <Confirmar abierto={cancelar} titulo="Cancelar pedido" mensaje="Se repondrá el stock y se notificará al cliente. ¿Continuar?" textoConfirmar="Cancelar pedido"
        onCancelar={() => setCancelar(false)} onConfirmar={() => { setCancelar(false); ejecutar(() => pedidosApi.cancelar(pedido.id), 'Pedido cancelado') }} />
    </>
  )
}
