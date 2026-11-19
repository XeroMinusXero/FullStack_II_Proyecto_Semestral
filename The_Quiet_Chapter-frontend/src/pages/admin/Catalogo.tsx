import { useCallback, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { catalogoApi } from '../../api/catalogo'
import { ErrorApi, mensajeDe } from '../../api/http'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Avisos } from '../../components/comunes/Avisos'
import { Cargando } from '../../components/comunes/Cargando'
import { Paginacion } from '../../components/comunes/Paginacion'
import { Modal } from '../../components/comunes/Modal'
import { Confirmar } from '../../components/comunes/Confirmar'
import { Portada } from '../../components/shopping/Portada'
import { FormularioLibro } from '../../components/admin/FormularioLibro'
import { GestionStock } from '../../components/admin/GestionStock'
import { EditorPrecio } from '../../components/admin/EditorPrecio'
import { EditorBiografia } from '../../components/admin/EditorBiografia'
import { InterruptorEstado } from '../../components/admin/InterruptorEstado'
import { useAccion } from '../../hooks/useAccion'
import estilos from './Catalogo.module.css'
import { clp } from '../../utils/formato'
import type { LibroDetalle, LibroResumen, LibroSolicitud, PaginaLibros } from '../../interfaces/catalogo'

type Estado = 'TODOS' | 'ACTIVOS' | 'INACTIVOS'

/** Color del stock: agotado (rojo), 5 o menos (alerta) o normal. */
const claseStock = (stock: number) => (stock === 0 ? 'badge-agotado' : stock <= 5 ? 'badge-oferta' : 'badge-disponible')

/** adminCatalogo.html -> HU-046..054: mantenedor de libros, precios, estado, stock y biografias de autores. */
export default function AdminCatalogo() {
  const [params, setParams] = useSearchParams()
  const [texto, setTexto] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [estado, setEstado] = useState<Estado>('TODOS')
  const [agotados, setAgotados] = useState(params.get('agotados') === '1')
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaLibros | null>(null)
  const [formulario, setFormulario] = useState<{ libro?: LibroDetalle } | null>(null)
  const [errCampos, setErrCampos] = useState<Record<string, string>>({})
  const [stock, setStock] = useState<LibroResumen | null>(null)
  const [eliminar, setEliminar] = useState<LibroResumen | null>(null)
  const [precio, setPrecio] = useState<LibroResumen | null>(null)
  const accion = useAccion()
  const { setError, setExito } = accion

  const cargar = useCallback(() => {
    catalogoApi.mantenedor({ texto: busqueda || undefined, estado, agotados: agotados || undefined, pagina, tamanio: 15 })
      .then(setDatos).catch((e) => setError(mensajeDe(e)))
  }, [busqueda, estado, agotados, pagina, setError])
  useEffect(() => { cargar() }, [cargar])

  // ?editar=ID abre el formulario directamente (enlace desde la ficha del libro).
  useEffect(() => {
    const id = params.get('editar')
    if (!id) return
    catalogoApi.detalle(Number(id)).then((libro) => setFormulario({ libro })).catch((e) => setError(mensajeDe(e)))
    setParams({}, { replace: true })
  }, [params, setParams, setError])

  const guardarLibro = async (s: LibroSolicitud) => {
    setErrCampos({}); setError('')
    try {
      if (formulario?.libro) await catalogoApi.editar(formulario.libro.id, s)
      else await catalogoApi.crear(s)
      setExito(formulario?.libro ? 'Libro actualizado' : 'Libro registrado')
      setFormulario(null)
      cargar()
    } catch (e) {
      if (e instanceof ErrorApi) setErrCampos(e.campos)
      setError(mensajeDe(e))
    }
  }

  const guardarPrecio = async (nuevo: number) => {
    const libro = precio!
    const ok = await accion.ejecutar(() => catalogoApi.precio(libro.id, nuevo), 'Precio actualizado. Los carritos con este libro verán el aviso de cambio.', cargar)
    if (ok) setPrecio(null)
    return ok
  }

  const editar = async (id: number) => {
    setError('')
    try { setFormulario({ libro: await catalogoApi.detalle(id) }) } catch (e) { setError(mensajeDe(e)) }
  }

  return (
    <>
      <h1>Catálogo e inventario</h1>
      <MenuAdmin />
      <Avisos estado={accion} />

      <div className="d-flex flex-wrap gap-2 align-items-end mb-3">
        <form className={`barra-busqueda form-plano ${estilos.buscador}`}
          onSubmit={(e) => { e.preventDefault(); setPagina(0); setBusqueda(texto.trim()) }}>
          <input type="search" placeholder="Buscar por título, autor o ISBN" value={texto} onChange={(e) => setTexto(e.target.value)} aria-label="Buscar" />
          <button className="btn" type="submit">Buscar</button>
        </form>
        <select className="form-select selector-corto" value={estado} aria-label="Estado" onChange={(e) => { setEstado(e.target.value as Estado); setPagina(0) }}>
          <option value="TODOS">Todos</option><option value="ACTIVOS">Activos</option><option value="INACTIVOS">Inactivos</option>
        </select>
        <div className="form-check mb-2">
          <input className="form-check-input" type="checkbox" id="agotados" checked={agotados} onChange={(e) => { setAgotados(e.target.checked); setPagina(0) }} />
          <label className="form-check-label" htmlFor="agotados">Solo agotados</label>
        </div>
        <button type="button" className="btn" onClick={() => { setErrCampos({}); setFormulario({}) }}>+ Registrar libro</button>
      </div>

      {!datos ? <Cargando /> : (
        <>
          <p className="text-muted">{datos.totalElementos} libros</p>
          <div className="tabla-scroll">
            <table>
              <thead><tr><th></th><th>Título</th><th>ISBN</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr></thead>
              <tbody>
                {datos.libros.length === 0 && <tr><td colSpan={7}>No hay libros que coincidan.</td></tr>}
                {datos.libros.map((l) => (
                  <tr key={l.id}>
                    <td><Portada url={l.portadaUrl} titulo={l.titulo} variante="mini" id={l.id} /></td>
                    <td><Link to={`/libros/${l.id}`}>{l.titulo}</Link><br /><small className="text-muted">{l.autores.join(', ')}</small></td>
                    <td>{l.isbn}</td>
                    <td>
                      {clp(l.precio)}{' '}
                      <button type="button" className="btn btn-secundario py-0 px-2" title="Cambiar precio" aria-label={`Cambiar precio de ${l.titulo}`} onClick={() => setPrecio(l)}>✎</button>
                    </td>
                    <td><span className={`badge ${claseStock(l.stock)}`}>{l.stock}</span></td>
                    <td>
                      <InterruptorEstado id={`libro-${l.id}`} activo={l.activo}
                        onCambiar={() => accion.ejecutar(() => catalogoApi.estado(l.id, !l.activo), l.activo ? 'Libro desactivado: ya no aparece en el catálogo' : 'Libro activado', cargar)} />
                    </td>
                    <td>
                      <div className="d-flex flex-wrap gap-1">
                        <button type="button" className="btn btn-secundario" onClick={() => editar(l.id)}>Editar</button>
                        <button type="button" className="btn btn-secundario" onClick={() => setStock(l)}>Stock</button>
                        <button type="button" className="btn btn-peligro" onClick={() => setEliminar(l)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={datos.pagina} totalPaginas={datos.totalPaginas} onCambiar={setPagina} />
        </>
      )}

      <EditorBiografia accion={accion} />

      <Modal abierto={!!formulario} titulo={formulario?.libro ? `Editar: ${formulario.libro.titulo}` : 'Registrar libro'} onCerrar={() => setFormulario(null)} ancho="xl">
        {formulario && <FormularioLibro key={formulario.libro?.id ?? 'nuevo'} libro={formulario.libro} errorServidor={errCampos} onGuardar={guardarLibro} onCancelar={() => setFormulario(null)} />}
      </Modal>

      <Modal abierto={!!stock} titulo={`Inventario: ${stock?.titulo ?? ''}`} onCerrar={() => setStock(null)} ancho="lg">
        {stock && <GestionStock libro={stock} onCambio={cargar} />}
      </Modal>

      <Modal abierto={!!precio} titulo={`Precio: ${precio?.titulo ?? ''}`} onCerrar={() => setPrecio(null)}>
        {precio && <EditorPrecio key={precio.id} precioActual={precio.precio} onGuardar={guardarPrecio} />}
      </Modal>

      <Confirmar abierto={!!eliminar} titulo="Eliminar libro" textoConfirmar="Eliminar"
        mensaje={`¿Eliminar «${eliminar?.titulo}»? Si tiene ventas registradas, el sistema lo impedirá: en ese caso desactívalo.`}
        onCancelar={() => setEliminar(null)} onConfirmar={() => { const l = eliminar!; setEliminar(null); accion.ejecutar(() => catalogoApi.eliminar(l.id), 'Libro eliminado', cargar) }} />
    </>
  )
}
