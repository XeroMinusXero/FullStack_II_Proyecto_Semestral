import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { catalogoApi } from '../api/catalogo'
import { mensajeDe } from '../api/http'
import { GrillaLibros } from '../components/shopping/GrillaLibros'
import { Paginacion } from '../components/comunes/Paginacion'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import { ListaSugeridos } from '../components/shopping/ListaSugeridos'
import { promoApi } from '../api/promo'
import type { Clasificacion, LibroResumen, OrdenCatalogo, PaginaLibros } from '../interfaces/catalogo'
import type { LibroSugerido } from '../interfaces/promo'
import estilos from './Catalogo.module.css'
import { SelectorMultiple } from '../components/comunes/SelectorMultiple'
import { clp } from '../utils/formato'
import { validarRangoPrecio } from '../utils/validaciones'

const ORDENES: { valor: OrdenCatalogo | ''; texto: string }[] = [
  { valor: '', texto: 'Título A-Z' },
  { valor: 'PRECIO_ASC', texto: 'Precio: Menor a Mayor' },
  { valor: 'PRECIO_DESC', texto: 'Precio: Mayor a Menor' },
  { valor: 'NOVEDADES', texto: 'Más recientes' },
]

const numeros = (v: string | null) => (v ? v.split(',').map(Number).filter((n) => !Number.isNaN(n)) : [])

/**
 * catalogo.html -> HU-001..010. Los filtros viven en la URL (?texto=&categorias=...): se pueden compartir
 * y el orden mantiene los filtros activos (HU-007). La busqueda/paginacion ocurre en MySQL (catalog-service).
 */
export default function Catalogo() {
  const [params, setParams] = useSearchParams()
  const navegar = useNavigate()
  const [resultado, setResultado] = useState<PaginaLibros | null>(null)
  const [error, setError] = useState('')
  const [categorias, setCategorias] = useState<Clasificacion[]>([])
  const [generos, setGeneros] = useState<Clasificacion[]>([])
  const [editoriales, setEditoriales] = useState<Clasificacion[]>([])
  const [texto, setTexto] = useState(params.get('texto') ?? '')
  const [minimo, setMinimo] = useState(params.get('minimo') ?? '')
  const [maximo, setMaximo] = useState(params.get('maximo') ?? '')
  const [errorPrecio, setErrorPrecio] = useState('')
  const [sugerencias, setSugerencias] = useState<LibroResumen[]>([])
  const [recomendadosCategoria, setRecomendadosCategoria] = useState<LibroSugerido[]>([])
  const temporizador = useRef<number | undefined>(undefined)

  const filtros = useMemo(() => ({
    texto: params.get('texto') ?? undefined,
    categorias: numeros(params.get('categorias')),
    generos: numeros(params.get('generos')),
    minimo: params.get('minimo') ? Number(params.get('minimo')) : undefined,
    maximo: params.get('maximo') ? Number(params.get('maximo')) : undefined,
    autorId: params.get('autorId') ? Number(params.get('autorId')) : undefined,
    editorialId: params.get('editorialId') ? Number(params.get('editorialId')) : undefined,
    orden: (params.get('orden') as OrdenCatalogo) || undefined,
    pagina: Number(params.get('pagina') ?? 0),
    tamanio: 12,
  }), [params])

  useEffect(() => {
    catalogoApi.categorias().then(setCategorias).catch(() => undefined)
    catalogoApi.generos().then(setGeneros).catch(() => undefined)
    catalogoApi.editoriales().then(setEditoriales).catch(() => undefined)
  }, [])

  useEffect(() => {
    let vigente = true
    catalogoApi.buscar(filtros)
      .then((r) => { if (vigente) { setResultado(r); setError('') } })
      .catch((e) => { if (vigente) setError(mensajeDe(e)) })
    return () => { vigente = false }
  }, [filtros])

  // HU-116: bloque de recomendados cuando se filtra por una sola categoria.
  useEffect(() => {
    if (filtros.categorias.length !== 1) return
    promoApi.porCategoria(filtros.categorias[0], 4).then(setRecomendadosCategoria).catch(() => setRecomendadosCategoria([]))
  }, [filtros.categorias])

  const actualizar = (cambios: Record<string, string | null>) => {
    const nuevo = new URLSearchParams(params)
    Object.entries(cambios).forEach(([k, v]) => (v === null || v === '' ? nuevo.delete(k) : nuevo.set(k, v)))
    if (!('pagina' in cambios)) nuevo.delete('pagina')
    setParams(nuevo)
  }

  // HU-004: sugerencias desde 2 caracteres, con espera de 250 ms.
  const alEscribir = (valor: string) => {
    setTexto(valor)
    window.clearTimeout(temporizador.current)
    if (valor.trim().length < 2) { setSugerencias([]); return }
    temporizador.current = window.setTimeout(() => {
      catalogoApi.sugerencias(valor.trim()).then(setSugerencias).catch(() => setSugerencias([]))
    }, 250)
  }

  const buscar = (e: FormEvent) => {
    e.preventDefault()
    setSugerencias([])
    actualizar({ texto: texto.trim() || null })
  }

  const aplicarPrecios = () => {
    const invalido = validarRangoPrecio(minimo, maximo)
    setErrorPrecio(invalido ?? '')
    if (invalido) return
    actualizar({ minimo: minimo || null, maximo: maximo || null })
  }

  const alternarMulti = (clave: 'categorias' | 'generos', id: number) => {
    const actual = numeros(params.get(clave))
    const siguiente = actual.includes(id) ? actual.filter((x) => x !== id) : [...actual, id]
    actualizar({ [clave]: siguiente.length ? siguiente.join(',') : null })
  }

  const hayFiltros = ['texto', 'categorias', 'generos', 'minimo', 'maximo', 'autorId', 'editorialId'].some((k) => params.get(k))

  return (
    <>
      <h1>Catálogo de Libros</h1>
      <form className={estilos.buscador} onSubmit={buscar} role="search">
        <div className="barra-busqueda position-relative">
          <input type="search" placeholder="Buscar por título, autor o ISBN..." value={texto} aria-label="Buscar libros"
            onChange={(e) => alEscribir(e.target.value)} onBlur={() => setTimeout(() => setSugerencias([]), 200)} autoComplete="off" />
          <button className="btn" type="submit">Buscar</button>
          {sugerencias.length > 0 && (
            <ul className={estilos.sugerencias} role="listbox">
              {sugerencias.map((s) => (
                <li key={s.id} role="option" aria-selected={false}>
                  <button type="button" onMouseDown={() => navegar(`/libros/${s.id}`)}>
                    <strong>{s.titulo}</strong> <small>— {s.autores.join(', ')}</small>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </form>

      <div className="barra-filtros">
        <SelectorMultiple etiqueta="Categorías" opciones={categorias} seleccion={filtros.categorias}
          onAlternar={(id) => alternarMulti('categorias', id)} onLimpiar={() => actualizar({ categorias: null })} />
        <SelectorMultiple etiqueta="Géneros" opciones={generos} seleccion={filtros.generos}
          onAlternar={(id) => alternarMulti('generos', id)} onLimpiar={() => actualizar({ generos: null })} />
        <div className="campo">
          <label htmlFor="editorial">Editorial</label>
          <select id="editorial" value={params.get('editorialId') ?? ''} onChange={(e) => actualizar({ editorialId: e.target.value || null })}>
            <option value="">Todas</option>
            {editoriales.map((e) => <option key={e.id} value={e.id}>{e.nombre}</option>)}
          </select>
        </div>
        <form className={`campo ${estilos.precio}${errorPrecio ? ' error' : ''}`} onSubmit={(e) => { e.preventDefault(); aplicarPrecios() }}>
          <label htmlFor="precioMin">Precio (CLP)</label>
          <div className={estilos.rango}>
            <input id="precioMin" type="number" min={0} placeholder="Mín." value={minimo} onChange={(e) => setMinimo(e.target.value)} aria-label="Precio mínimo" />
            <span aria-hidden="true">–</span>
            <input id="precioMax" type="number" min={0} placeholder="Máx." value={maximo} onChange={(e) => setMaximo(e.target.value)} aria-label="Precio máximo" />
            <button type="submit" className="btn">Aplicar</button>
          </div>
          <span className="mensaje-error">{errorPrecio}</span>
        </form>
      </div>

      {hayFiltros && (
        <div className={estilos.activos} aria-label="Filtros aplicados">
          {filtros.texto && <Chip texto={`“${filtros.texto}”`} onQuitar={() => { setTexto(''); actualizar({ texto: null }) }} />}
          {categorias.filter((c) => filtros.categorias.includes(c.id)).map((c) => <Chip key={`c${c.id}`} texto={c.nombre} onQuitar={() => alternarMulti('categorias', c.id)} />)}
          {generos.filter((g) => filtros.generos.includes(g.id)).map((g) => <Chip key={`g${g.id}`} texto={g.nombre} onQuitar={() => alternarMulti('generos', g.id)} />)}
          {filtros.editorialId && <Chip texto={editoriales.find((e) => e.id === filtros.editorialId)?.nombre ?? 'Editorial'} onQuitar={() => actualizar({ editorialId: null })} />}
          {(filtros.minimo !== undefined || filtros.maximo !== undefined) && (
            <Chip texto={`${filtros.minimo !== undefined ? clp(filtros.minimo) : '$0'} – ${filtros.maximo !== undefined ? clp(filtros.maximo) : 'sin tope'}`}
              onQuitar={() => { setMinimo(''); setMaximo(''); actualizar({ minimo: null, maximo: null }) }} />
          )}
          {filtros.autorId && <Chip texto="Autor seleccionado" onQuitar={() => actualizar({ autorId: null })} />}
          <button type="button" className={estilos.limpiar}
            onClick={() => { setTexto(''); setMinimo(''); setMaximo(''); setErrorPrecio(''); setParams(new URLSearchParams()) }}>Limpiar todo</button>
        </div>
      )}

      <Alerta tipo="error">{error}</Alerta>
      {!resultado && !error && <Cargando />}
      {resultado && (
        <>
          <div className={estilos.cabeceraResultados}>
            <p className="text-muted mb-0">{resultado.totalElementos} {resultado.totalElementos === 1 ? 'libro encontrado' : 'libros encontrados'}</p>
            <label className={estilos.orden}>
              <span>Ordenar por</span>
              <select id="orden" value={params.get('orden') ?? ''} onChange={(e) => actualizar({ orden: e.target.value || null })}>
                {ORDENES.map((o) => <option key={o.valor} value={o.valor}>{o.texto}</option>)}
              </select>
            </label>
          </div>
          <GrillaLibros libros={resultado.libros.map((l) => ({ ...l, autores: l.autores.join(', ') }))}
            vacio={filtros.texto ? 'No se encontraron libros que coincidan con su búsqueda' : 'No se encontraron productos para los filtros seleccionados'} />
          <Paginacion pagina={resultado.pagina} totalPaginas={resultado.totalPaginas} onCambiar={(p) => actualizar({ pagina: String(p) })} />
        </>
      )}
      {filtros.categorias.length === 1 && <ListaSugeridos titulo="Recomendados de esta categoría" libros={recomendadosCategoria} />}
    </>
  )
}

function Chip({ texto, onQuitar }: { texto: string; onQuitar: () => void }) {
  return (
    <span className={estilos.chipActivo}>
      {texto}
      <button type="button" onClick={onQuitar} aria-label={`Quitar filtro ${texto}`}>×</button>
    </span>
  )
}
