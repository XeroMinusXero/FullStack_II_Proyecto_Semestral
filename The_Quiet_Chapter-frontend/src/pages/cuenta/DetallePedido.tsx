import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { pedidosApi } from '../../api/pedidos'
import { mensajeDe } from '../../api/http'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Cargando } from '../../components/comunes/Cargando'
import { Alerta } from '../../components/comunes/Alerta'
import { Confirmar } from '../../components/comunes/Confirmar'
import { LineaEstados } from '../../components/pedidos/LineaEstados'
import { ETIQUETA_ESTADO_PEDIDO, claseEstado, clp, fecha } from '../../utils/formato'
import type { Pedido } from '../../interfaces/pedido'

/** HU-041/042/043: detalle, seguimiento y cancelacion (solo mientras no fue enviado). */
export default function DetallePedido() {
  const id = Number(useParams().id)
  const [pedido, setPedido] = useState<Pedido | null>(null)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [confirmar, setConfirmar] = useState(false)

  useEffect(() => { pedidosApi.miPedido(id).then(setPedido).catch((e) => setError(mensajeDe(e))) }, [id])

  const cancelar = async () => {
    setConfirmar(false); setError('')
    try { setPedido(await pedidosApi.cancelarMio(id)); setExito('Pedido cancelado. El stock fue repuesto y te enviamos un correo.') } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <Link to="/cuenta/pedidos">← Volver a mis pedidos</Link>
      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{exito}</Alerta>
      {!pedido && !error && <Cargando />}
      {pedido && (
        <div className="seccion mt-3">
          <div className="d-flex flex-wrap justify-content-between gap-2">
            <h2 className="mb-0">Pedido {pedido.numeroPedido}</h2>
            <span className={`badge ${claseEstado(pedido.estado)}`}>{ETIQUETA_ESTADO_PEDIDO[pedido.estado]}</span>
          </div>
          <p className="text-muted">Realizado el {fecha(pedido.fechaCreacion, true)}</p>
          <LineaEstados estado={pedido.estado} />
          <div className="row g-4 mt-1">
            <div className="col-md-6">
              <h3>Entrega</h3>
              <p className="mb-1">{pedido.entrega.direccionFormateada}</p>
              <p className="mb-1">{pedido.entrega.metodoEnvio} · {pedido.entrega.plazoEstimadoDias} días hábiles</p>
              <p>N° de seguimiento: <strong>{pedido.entrega.numeroSeguimiento ?? 'Aún no asignado'}</strong></p>
            </div>
            <div className="col-md-6">
              <h3>Pago</h3>
              <p className="mb-1">{pedido.medioPago}</p>
              <p>Referencia: {pedido.referenciaPago}</p>
            </div>
          </div>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Libro</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
              <tbody>{pedido.detalles.map((d) => <tr key={d.id}><td><Link to={`/libros/${d.libroId}`}>{d.tituloLibro}</Link></td><td>{d.cantidad}</td><td>{clp(d.precioUnitario)}</td><td>{clp(d.subtotal)}</td></tr>)}</tbody>
            </table>
          </div>
          <div className="resumen-caja mt-3" style={{ maxWidth: 420, marginLeft: 'auto' }}>
            <div className="fila"><span>Subtotal</span><span>{clp(pedido.importes.subtotal)}</span></div>
            {pedido.importes.descuento > 0 && <div className="fila"><span>Descuentos</span><span>−{clp(pedido.importes.descuento)}</span></div>}
            <div className="fila"><span>Envío</span><span>{clp(pedido.importes.costoEnvio)}</span></div>
            <div className="fila"><span>Total</span><span>{clp(pedido.importes.total)}</span></div>
            <small className="text-muted">IVA incluido: {clp(pedido.importes.ivaIncluido)}</small>
          </div>
          {pedido.puedeCancelarse && <button type="button" className="btn btn-peligro mt-3" onClick={() => setConfirmar(true)}>Cancelar pedido</button>}
        </div>
      )}
      <Confirmar abierto={confirmar} titulo="Cancelar pedido" mensaje="¿Seguro que deseas cancelar este pedido? Se reembolsará el pago simulado."
        textoConfirmar="Cancelar pedido" onCancelar={() => setConfirmar(false)} onConfirmar={cancelar} />
    </>
  )
}
