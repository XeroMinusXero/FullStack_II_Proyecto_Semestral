import { useState, type FormEvent } from 'react'
import { Campo } from '../comunes/Campo'
import { reglas } from '../../utils/validaciones'
import type { LibroDetalle, LibroSolicitud } from '../../interfaces/catalogo'

type Form = Record<keyof LibroSolicitud, string>

const desdeLibro = (l?: LibroDetalle): Form => ({
  isbn: l?.isbn ?? '', titulo: l?.titulo ?? '', autores: l?.autores.map((a) => a.nombre).join(', ') ?? '',
  editorial: l?.editorial?.nombre ?? '', categoria: l?.categoria?.nombre ?? '', generos: l?.generos.map((g) => g.nombre).join(', ') ?? '',
  sinopsis: l?.sinopsis ?? '', formato: l?.formato ?? 'Físico', paginas: l?.paginas ? String(l.paginas) : '', idioma: l?.idioma ?? 'Español',
  anioPublicacion: l?.anioPublicacion ? String(l.anioPublicacion) : '', encuadernacion: l?.encuadernacion ?? '',
  portadaUrl: l?.portadaUrl ?? '', extractoUrl: l?.extractoUrl ?? '', precio: l ? String(l.precio) : '', stockInicial: l ? String(l.stock) : '0',
})

const lista = (v: string) => v.split(',').map((x) => x.trim()).filter(Boolean)

/** adminCatalogo.html (formulario) -> HU-046 registrar / HU-047 editar libro. */
export function FormularioLibro({ libro, onGuardar, onCancelar, errorServidor }: {
  libro?: LibroDetalle
  onGuardar: (s: LibroSolicitud) => Promise<void>
  onCancelar: () => void
  errorServidor?: Record<string, string>
}) {
  const [f, setF] = useState<Form>(desdeLibro(libro))
  const [errores, setErrores] = useState<Partial<Form>>({})
  const [enviando, setEnviando] = useState(false)
  const todos = { ...errores, ...(errorServidor ?? {}) } as Partial<Form>

  const validar = () => {
    const e: Partial<Form> = {}
    if (!reglas.isbn(f.isbn)) e.isbn = 'ISBN de 10 a 17 caracteres (dígitos, guiones o X)'
    if (!f.titulo.trim()) e.titulo = 'Este campo es obligatorio'
    if (!lista(f.autores).length) e.autores = 'Indica al menos un autor'
    if (!f.editorial.trim()) e.editorial = 'Este campo es obligatorio'
    if (!f.categoria.trim()) e.categoria = 'Este campo es obligatorio'
    if (!(Number(f.precio) > 0)) e.precio = 'El precio debe ser mayor a 0'
    if (!libro && !(Number(f.stockInicial) >= 0 && Number.isInteger(Number(f.stockInicial)))) e.stockInicial = 'Stock entero ≥ 0'
    if (f.paginas && !(Number(f.paginas) >= 0)) e.paginas = 'Número inválido'
    const anio = Number(f.anioPublicacion)
    if (f.anioPublicacion && (anio < 1000 || anio > new Date().getFullYear() + 1)) e.anioPublicacion = 'Año inválido'
    return e
  }

  const enviar = async (ev: FormEvent) => {
    ev.preventDefault()
    const e = validar()
    setErrores(e)
    if (Object.keys(e).length) return
    setEnviando(true)
    try {
      await onGuardar({
        isbn: f.isbn.trim(), titulo: f.titulo.trim(), autores: lista(f.autores), editorial: f.editorial.trim(), categoria: f.categoria.trim(),
        generos: lista(f.generos), sinopsis: f.sinopsis.trim(), formato: f.formato.trim(), paginas: Number(f.paginas || 0), idioma: f.idioma.trim(),
        anioPublicacion: Number(f.anioPublicacion || 0), encuadernacion: f.encuadernacion.trim(), portadaUrl: f.portadaUrl.trim(),
        extractoUrl: f.extractoUrl.trim(), precio: Number(f.precio), stockInicial: Number(f.stockInicial || 0),
      })
    } finally { setEnviando(false) }
  }

  const c = (k: keyof Form) => ({ value: f[k], error: todos[k], onChange: (e: { target: { value: string } }) => setF({ ...f, [k]: e.target.value }) })

  return (
    <form onSubmit={enviar} noValidate className="ancho" style={{ boxShadow: 'none', padding: 0, border: 0 }}>
      <div className="row g-2">
        <div className="col-md-8"><Campo id="titulo" etiqueta="Título" maxLength={255} {...c('titulo')} /></div>
        <div className="col-md-4"><Campo id="isbn" etiqueta="ISBN" maxLength={17} {...c('isbn')} /></div>
        <div className="col-md-6"><Campo id="autores" etiqueta="Autor(es)" ayuda="Separados por coma" {...c('autores')} /></div>
        <div className="col-md-6"><Campo id="editorial" etiqueta="Editorial" {...c('editorial')} /></div>
        <div className="col-md-6"><Campo id="categoria" etiqueta="Categoría" {...c('categoria')} /></div>
        <div className="col-md-6"><Campo id="generos" etiqueta="Géneros" ayuda="Separados por coma" {...c('generos')} /></div>
        <div className="col-md-4"><Campo id="precio" etiqueta="Precio (CLP, IVA incluido)" type="number" min={1} {...c('precio')} /></div>
        {!libro && <div className="col-md-4"><Campo id="stockInicial" etiqueta="Stock inicial" type="number" min={0} {...c('stockInicial')} /></div>}
        <div className="col-md-4"><Campo id="paginas" etiqueta="Páginas" type="number" min={0} {...c('paginas')} /></div>
        <div className="col-md-4"><Campo id="anio" etiqueta="Año de publicación" type="number" {...c('anioPublicacion')} /></div>
        <div className="col-md-4"><Campo id="idioma" etiqueta="Idioma" {...c('idioma')} /></div>
        <div className="col-md-4"><Campo id="formato" etiqueta="Formato" {...c('formato')} /></div>
        <div className="col-md-4"><Campo id="encuadernacion" etiqueta="Encuadernación" {...c('encuadernacion')} /></div>
        <div className="col-md-6"><Campo id="portada" etiqueta="URL de portada" placeholder="/img/mi-libro.jpg" {...c('portadaUrl')} /></div>
        <div className="col-md-6"><Campo id="extracto" etiqueta="URL de muestra (PDF)" {...c('extractoUrl')} /></div>
        <div className="col-12">
          <div className="campo">
            <label htmlFor="sinopsis">Sinopsis</label>
            <textarea id="sinopsis" rows={4} value={f.sinopsis} onChange={(e) => setF({ ...f, sinopsis: e.target.value })} />
          </div>
        </div>
      </div>
      <div className="d-flex gap-2 justify-content-end">
        <button type="button" className="btn btn-secundario" onClick={onCancelar}>Cancelar</button>
        <button type="submit" className="btn" disabled={enviando}>{enviando ? 'Guardando...' : libro ? 'Guardar cambios' : 'Registrar libro'}</button>
      </div>
    </form>
  )
}
