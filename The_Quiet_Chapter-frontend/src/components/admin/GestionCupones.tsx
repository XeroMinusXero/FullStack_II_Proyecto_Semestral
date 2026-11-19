import { useCallback, useEffect, useState } from 'react'
import { promoApi } from '../../api/promo'
import { mensajeDe } from '../../api/http'
import type { Accion } from '../../hooks/useAccion'
import { Cargando } from '../comunes/Cargando'
import { Paginacion } from '../comunes/Paginacion'
import { Modal } from '../comunes/Modal'
import { Confirmar } from '../comunes/Confirmar'
import { EditorCupon } from './EditorCupon'
import { InterruptorEstado } from './InterruptorEstado'
import { clp, fecha, textoDescuento } from '../../utils/formato'
import { aFechaHoraLocal, fechaHoraLocal } from '../../utils/fechas'
import type { Canje, Cupon, CuponSolicitud, Pagina } from '../../interfaces/promo'

const NUEVO = (): CuponSolicitud => ({ codigo: '', tipoDescuento: 'PORCENTAJE', valor: 10, montoMinimo: 0, fechaInicio: fechaHoraLocal(), fechaFin: fechaHoraLocal(30) })

/** HU-092..099: listado, alta, edicion, activacion, eliminacion y canjes de cupones. */
export function GestionCupones({ accion }: { accion: Accion }) {
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Cupon> | null>(null)
  const [editor, setEditor] = useState<{ id?: number; inicial: CuponSolicitud } | null>(null)
  const [canjes, setCanjes] = useState<{ cupon: Cupon; lista: Canje[] } | null>(null)
  const [eliminar, setEliminar] = useState<Cupon | null>(null)
  const { setError } = accion

  const cargar = useCallback(() => { promoApi.cupones(pagina).then(setDatos).catch((e) => setError(mensajeDe(e))) }, [pagina, setError])
  useEffect(() => { cargar() }, [cargar])

  const guardar = async (s: CuponSolicitud) => {
    const id = editor?.id
    const ok = await accion.ejecutar(() => (id ? promoApi.editarCupon(id, s) : promoApi.crearCupon(s)), id ? 'Cupón actualizado' : 'Cupón creado', cargar)
    if (ok) setEditor(null)
    return ok
  }

  const verCanjes = (c: Cupon) => promoApi.canjes(c.id).then((lista) => setCanjes({ cupon: c, lista })).catch((e) => setError(mensajeDe(e)))

  return (
    <>
      <div className="text-end mb-2">
        <button type="button" className="btn" onClick={() => setEditor({ inicial: NUEVO() })}>+ Nuevo cupón</button>
      </div>
      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Código</th><th>Descuento</th><th>Mínimo</th><th>Vigencia</th><th>Canjes</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.contenido.length === 0 && <tr><td colSpan={7}>No hay cupones.</td></tr>}
                {datos.contenido.map((c) => (
                  <tr key={c.id}>
                    <td><strong>{c.codigo}</strong></td>
                    <td>{textoDescuento(c.tipoDescuento, c.valor)}</td>
                    <td>{clp(c.montoMinimo)}</td>
                    <td>{fecha(c.fechaInicio)} – {fecha(c.fechaFin)} <span className={`badge ${c.vigente ? 'badge-disponible' : 'badge-agotado'}`}>{c.vigente ? 'vigente' : 'no vigente'}</span></td>
                    <td><button type="button" className="btn btn-secundario py-0" title="Ver canjes" onClick={() => verCanjes(c)}>{c.canjes}</button></td>
                    <td>
                      <InterruptorEstado id={`cupon-${c.id}`} activo={c.activo}
                        onCambiar={() => accion.ejecutar(() => promoApi.estadoCupon(c.id, !c.activo), c.activo ? 'Cupón desactivado' : 'Cupón activado', cargar)} />
                    </td>
                    <td className="d-flex gap-1">
                      <button type="button" className="btn btn-secundario" onClick={() => setEditor({
                        id: c.id,
                        inicial: { codigo: c.codigo, tipoDescuento: c.tipoDescuento, valor: c.valor, montoMinimo: c.montoMinimo, fechaInicio: aFechaHoraLocal(c.fechaInicio), fechaFin: aFechaHoraLocal(c.fechaFin) },
                      })}>Editar</button>
                      <button type="button" className="btn btn-peligro" onClick={() => setEliminar(c)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!editor} titulo={editor?.id ? 'Editar cupón' : 'Nuevo cupón'} onCerrar={() => setEditor(null)}>
        {editor && <EditorCupon key={editor.id ?? 'nuevo'} inicial={editor.inicial} onGuardar={guardar} />}
      </Modal>

      <Modal abierto={!!canjes} titulo={`Canjes de ${canjes?.cupon.codigo ?? ''}`} onCerrar={() => setCanjes(null)}>
        <div className="tabla-scroll">
          <table>
            <thead><tr><th>Fecha</th><th>Cliente</th><th>Pedido</th><th>Descuento</th></tr></thead>
            <tbody>
              {canjes?.lista.length === 0 && <tr><td colSpan={4}>Sin canjes.</td></tr>}
              {canjes?.lista.map((c) => <tr key={c.id}><td>{fecha(c.fechaCanje, true)}</td><td>#{c.clienteId}</td><td>#{c.pedidoId}</td><td>{clp(c.montoDescuento)}</td></tr>)}
            </tbody>
          </table>
        </div>
      </Modal>

      <Confirmar abierto={!!eliminar} titulo="Eliminar cupón" textoConfirmar="Eliminar"
        mensaje={`¿Eliminar el cupón ${eliminar?.codigo}? Si ya tiene canjes, desactívalo en su lugar.`}
        onCancelar={() => setEliminar(null)}
        onConfirmar={() => { const c = eliminar!; setEliminar(null); accion.ejecutar(() => promoApi.eliminarCupon(c.id), 'Cupón eliminado', cargar) }} />
    </>
  )
}
