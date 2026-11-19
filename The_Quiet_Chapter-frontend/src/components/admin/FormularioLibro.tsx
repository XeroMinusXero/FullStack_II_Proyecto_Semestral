import { useState, type FormEvent } from 'react'
import { Campo, CampoTextoLargo } from '../comunes/Campo'
import { useFormulario } from '../../hooks/useFormulario'
import { lista, validarLibro, type FormularioLibroDatos } from '../../utils/validaciones'
import type { LibroDetalle, LibroSolicitud } from '../../interfaces/catalogo'

/** Valores iniciales: vacios al registrar, los del libro al editar. */
const desdeLibro = (l?: LibroDetalle): FormularioLibroDatos => ({
  isbn: l?.isbn ?? '', titulo: l?.titulo ?? '', autores: l?.autores.map((a) => a.nombre).join(', ') ?? '',
  editorial: l?.editorial?.nombre ?? '', categoria: l?.categoria?.nombre ?? '', generos: l?.generos.map((g) => g.nombre).join(', ') ?? '',
  sinopsis: l?.sinopsis ?? '', formato: l?.formato ?? 'Físico', paginas: l?.paginas ? String(l.paginas) : '', idioma: l?.idioma ?? 'Español',
  anioPublicacion: l?.anioPublicacion ? String(l.anioPublicacion) : '', encuadernacion: l?.encuadernacion ?? '',
  portadaUrl: l?.portadaUrl ?? '', extractoUrl: l?.extractoUrl ?? '', precio: l ? String(l.precio) : '', stockInicial: l ? String(l.stock) : '0',
})

/** Convierte el formulario (texto) al cuerpo que espera catalog-service (LibroSolicitud). */
const aSolicitud = (f: FormularioLibroDatos): LibroSolicitud => ({
  isbn: f.isbn.trim(), titulo: f.titulo.trim(), autores: lista(f.autores), editorial: f.editorial.trim(), categoria: f.categoria.trim(),
  generos: lista(f.generos), sinopsis: f.sinopsis.trim(), formato: f.formato.trim(), paginas: Number(f.paginas || 0), idioma: f.idioma.trim(),
  anioPublicacion: Number(f.anioPublicacion || 0), encuadernacion: f.encuadernacion.trim(), portadaUrl: f.portadaUrl.trim(),
  extractoUrl: f.extractoUrl.trim(), precio: Number(f.precio), stockInicial: Number(f.stockInicial || 0),
})

/** adminCatalogo.html (formulario) -> HU-046 registrar / HU-047 editar libro. */
export function FormularioLibro({ libro, onGuardar, onCancelar, errorServidor }: {
  libro?: LibroDetalle
  onGuardar: (s: LibroSolicitud) => Promise<void>
  onCancelar: () => void
  /** Errores por campo que devolvio el backend (por ejemplo, ISBN repetido). */
  errorServidor?: Record<string, string>
}) {
  const form = useFormulario(desdeLibro(libro))
  const [enviando, setEnviando] = useState(false)
  const esNuevo = !libro

  // Los errores del backend se muestran en el mismo campo que los del frontend.
  const c = (k: keyof FormularioLibroDatos) => {
    const props = form.campo(k)
    return { ...props, error: props.error ?? errorServidor?.[k] }
  }

  const enviar = async (ev: FormEvent) => {
    ev.preventDefault()
    if (!form.validar((v) => validarLibro(v, esNuevo))) return
    setEnviando(true)
    try { await onGuardar(aSolicitud(form.valores)) } finally { setEnviando(false) }
  }

  return (
    <form onSubmit={enviar} noValidate className="form-plano">
      <div className="row g-2">
        <div className="col-md-8"><Campo id="titulo" etiqueta="Título" maxLength={255} {...c('titulo')} /></div>
        <div className="col-md-4"><Campo id="isbn" etiqueta="ISBN" maxLength={17} {...c('isbn')} /></div>
        <div className="col-md-6"><Campo id="autores" etiqueta="Autor(es)" ayuda="Separados por coma" {...c('autores')} /></div>
        <div className="col-md-6"><Campo id="editorial" etiqueta="Editorial" maxLength={150} {...c('editorial')} /></div>
        <div className="col-md-6"><Campo id="categoria" etiqueta="Categoría" maxLength={150} {...c('categoria')} /></div>
        <div className="col-md-6"><Campo id="generos" etiqueta="Géneros" ayuda="Separados por coma" {...c('generos')} /></div>
        <div className="col-md-4"><Campo id="precio" etiqueta="Precio (CLP, IVA incluido)" type="number" min={1} {...c('precio')} /></div>
        {esNuevo && <div className="col-md-4"><Campo id="stockInicial" etiqueta="Stock inicial" type="number" min={0} {...c('stockInicial')} /></div>}
        <div className="col-md-4"><Campo id="paginas" etiqueta="Páginas" type="number" min={0} {...c('paginas')} /></div>
        <div className="col-md-4"><Campo id="anio" etiqueta="Año de publicación" type="number" {...c('anioPublicacion')} /></div>
        <div className="col-md-4"><Campo id="idioma" etiqueta="Idioma" maxLength={80} {...c('idioma')} /></div>
        <div className="col-md-4"><Campo id="formato" etiqueta="Formato" maxLength={80} {...c('formato')} /></div>
        <div className="col-md-4"><Campo id="encuadernacion" etiqueta="Encuadernación" maxLength={80} {...c('encuadernacion')} /></div>
        <div className="col-md-6"><Campo id="portada" etiqueta="URL de portada" placeholder="/img/mi-libro.jpg" {...c('portadaUrl')} /></div>
        <div className="col-md-6"><Campo id="extracto" etiqueta="URL de muestra (PDF)" {...c('extractoUrl')} /></div>
        <div className="col-12"><CampoTextoLargo id="sinopsis" etiqueta="Sinopsis" rows={4} {...c('sinopsis')} /></div>
      </div>
      <div className="d-flex gap-2 justify-content-end">
        <button type="button" className="btn btn-secundario" onClick={onCancelar}>Cancelar</button>
        <button type="submit" className="btn" disabled={enviando}>{enviando ? 'Guardando...' : esNuevo ? 'Registrar libro' : 'Guardar cambios'}</button>
      </div>
    </form>
  )
}
