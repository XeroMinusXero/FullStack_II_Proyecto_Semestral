import { useCallback, useEffect, useState } from 'react'
import { promoApi } from '../../api/promo'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import type { Accion } from '../../hooks/useAccion'
import { Cargando } from '../comunes/Cargando'
import { Paginacion } from '../comunes/Paginacion'
import { Modal } from '../comunes/Modal'
import { Confirmar } from '../comunes/Confirmar'
import { EditorPromocion } from './EditorPromocion'
import { InterruptorEstado } from './InterruptorEstado'
import { fecha, textoDescuento } from '../../utils/formato'
import { aFechaHoraLocal, fechaHoraLocal } from '../../utils/fechas'
import type { Alcance, Pagina, Promocion, PromocionSolicitud, TipoAlcance } from '../../interfaces/promo'
import type { Clasificacion } from '../../interfaces/catalogo'

const NUEVA = (): PromocionSolicitud => ({ nombre: '', tipoDescuento: 'PORCENTAJE', valor: 15, fechaInicio: fechaHoraLocal(), fechaFin: fechaHoraLocal(30), alcances: [], activo: true })
const SIN_OPCIONES: Record<TipoAlcance, Clasificacion[]> = { LIBRO: [], CATEGORIA: [], GENERO: [], EDITORIAL: [] }

/** HU-111..113: promociones por libro, categoria, genero o editorial. */
export function GestionPromociones({ accion }: { accion: Accion }) {
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<Pagina<Promocion> | null>(null)
  const [editor, setEditor] = useState<{ id?: number; inicial: PromocionSolicitud } | null>(null)
  const [eliminar, setEliminar] = useState<Promocion | null>(null)
  const [opciones, setOpciones] = useState(SIN_OPCIONES)
  const { setError } = accion

  const cargar = useCallback(() => { promoApi.promociones(pagina).then(setDatos).catch((e) => setError(mensajeDe(e))) }, [pagina, setError])
  useEffect(() => { cargar() }, [cargar])

  // Nombres para mostrar los alcances (promo-service solo guarda el id de cada libro/categoria/...).
  useEffect(() => {
    Promise.all([
      catalogoApi.mantenedor({ estado: 'ACTIVOS', tamanio: 100 }).then((p) => p.libros.map((l) => ({ id: l.id, nombre: l.titulo }))),
      catalogoApi.categorias(), catalogoApi.generos(), catalogoApi.editoriales(),
    ]).then(([LIBRO, CATEGORIA, GENERO, EDITORIAL]) => setOpciones({ LIBRO, CATEGORIA, GENERO, EDITORIAL })).catch(() => undefined)
  }, [])

  const nombre = useCallback((a: Alcance) => opciones[a.tipo].find((o) => o.id === a.referenciaId)?.nombre ?? `#${a.referenciaId}`, [opciones])

  const guardar = async (s: PromocionSolicitud) => {
    const id = editor?.id
    const ok = await accion.ejecutar(() => (id ? promoApi.editarPromocion(id, s) : promoApi.crearPromocion(s)), id ? 'Promoción actualizada' : 'Promoción creada', cargar)
    if (ok) setEditor(null)
    return ok
  }

  return (
    <>
      <div className="text-end mb-2">
        <button type="button" className="btn" onClick={() => setEditor({ inicial: NUEVA() })}>+ Nueva promoción</button>
      </div>
      {!datos ? <Cargando /> : (
        <>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th>Nombre</th><th>Descuento</th><th>Alcance</th><th>Vigencia</th><th>Aplicaciones</th><th>Estado</th><th></th></tr></thead>
              <tbody>
                {datos.contenido.length === 0 && <tr><td colSpan={7}>No hay promociones.</td></tr>}
                {datos.contenido.map((p) => (
                  <tr key={p.id}>
                    <td><strong>{p.nombre}</strong></td>
                    <td>{textoDescuento(p.tipoDescuento, p.valor)}</td>
                    <td><small>{p.alcances.map((a) => `${a.tipo.toLowerCase()}: ${nombre(a)}`).join(' · ')}</small></td>
                    <td>{fecha(p.fechaInicio)} – {fecha(p.fechaFin)} {p.vigente && <span className="badge badge-disponible">vigente</span>}</td>
                    <td>{p.aplicaciones}</td>
                    <td>
                      <InterruptorEstado id={`promo-${p.id}`} activo={p.activo} textos={['Activa', 'Inactiva']}
                        onCambiar={() => accion.ejecutar(() => promoApi.estadoPromocion(p.id, !p.activo), p.activo ? 'Promoción desactivada' : 'Promoción activada', cargar)} />
                    </td>
                    <td className="d-flex gap-1">
                      <button type="button" className="btn btn-secundario" onClick={() => setEditor({
                        id: p.id,
                        inicial: { nombre: p.nombre, tipoDescuento: p.tipoDescuento, valor: p.valor, fechaInicio: aFechaHoraLocal(p.fechaInicio), fechaFin: aFechaHoraLocal(p.fechaFin), alcances: p.alcances, activo: p.activo },
                      })}>Editar</button>
                      <button type="button" className="btn btn-peligro" onClick={() => setEliminar(p)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <Modal abierto={!!editor} titulo={editor?.id ? 'Editar promoción' : 'Nueva promoción'} onCerrar={() => setEditor(null)} ancho="xl">
        {editor && <EditorPromocion key={editor.id ?? 'nueva'} inicial={editor.inicial} opciones={opciones} nombre={nombre} onGuardar={guardar} />}
      </Modal>
      <Confirmar abierto={!!eliminar} titulo="Eliminar promoción" mensaje={`¿Eliminar «${eliminar?.nombre}»?`} textoConfirmar="Eliminar"
        onCancelar={() => setEliminar(null)}
        onConfirmar={() => { const p = eliminar!; setEliminar(null); accion.ejecutar(() => promoApi.eliminarPromocion(p.id), 'Promoción eliminada', cargar) }} />
    </>
  )
}
