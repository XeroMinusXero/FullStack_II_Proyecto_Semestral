import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogoApi } from '../api/catalogo'
import { promoApi } from '../api/promo'
import { notifApi } from '../api/notif'
import { ErrorApi, mensajeDe } from '../api/http'
import { useAuth } from '../context/auth'
import { MigasDePan } from '../components/comunes/MigasDePan'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import { Estrellas } from '../components/comunes/Estrellas'
import { Modal } from '../components/comunes/Modal'
import { Portada } from '../components/shopping/Portada'
import { Precio } from '../components/shopping/Precio'
import { BotonAgregar } from '../components/shopping/BotonAgregar'
import { BotonFavorito } from '../components/shopping/BotonFavorito'
import { ControlCantidad } from '../components/shopping/ControlCantidad'
import { ListaSugeridos } from '../components/shopping/ListaSugeridos'
import { SeccionResenas } from '../components/social/SeccionResenas'
import type { LibroDetalle } from '../interfaces/catalogo'
import type { LibroSugerido, PrecioPromo } from '../interfaces/promo'
import type { OpinionesLibro } from '../interfaces/social'

/** detalleLibro.html -> HU-011/012 (ficha + migas), HU-117 relacionados, HU-104 aviso de reabastecimiento. */
export default function DetalleLibro() {
  const { id } = useParams()
  const libroId = Number(id)
  const { usuario, esAdmin } = useAuth()
  const [libro, setLibro] = useState<LibroDetalle | null>(null)
  const [error, setError] = useState('')
  const [noExiste, setNoExiste] = useState(false)
  const [promo, setPromo] = useState<PrecioPromo | undefined>()
  const [relacionados, setRelacionados] = useState<LibroSugerido[]>([])
  const [cantidad, setCantidad] = useState(1)
  const [opiniones, setOpiniones] = useState<OpinionesLibro | null>(null)
  const [muestra, setMuestra] = useState(false)
  const [aviso, setAviso] = useState<'ninguno' | 'activo' | 'cargando'>('ninguno')
  const [mensajeAviso, setMensajeAviso] = useState('')

  useEffect(() => {
    let vigente = true
    catalogoApi.detalle(libroId)
      .then((l) => { if (vigente) setLibro(l) })
      .catch((e) => { if (!vigente) return; if (e instanceof ErrorApi && e.estado === 404) setNoExiste(true); else setError(mensajeDe(e)) })
    promoApi.precios([libroId]).then((p) => vigente && setPromo(p[0])).catch(() => undefined)
    promoApi.relacionados(libroId, 5).then((r) => vigente && setRelacionados(r)).catch(() => undefined)
    promoApi.visita(libroId).catch(() => undefined) // alimenta las recomendaciones por perfil (HU-115)
    return () => { vigente = false }
  }, [libroId])

  useEffect(() => {
    if (!usuario || esAdmin) return
    notifApi.misAvisos().then((a) => setAviso(a.some((x) => x.libroId === libroId && x.activa) ? 'activo' : 'ninguno')).catch(() => undefined)
  }, [usuario, esAdmin, libroId])

  const alCambiarOpiniones = useCallback((o: OpinionesLibro) => setOpiniones(o), [])

  const alternarAviso = async () => {
    const previo = aviso
    setAviso('cargando'); setMensajeAviso('')
    try {
      if (previo === 'activo') { await notifApi.cancelarAviso(libroId); setAviso('ninguno'); setMensajeAviso('Aviso cancelado') }
      else { await notifApi.pedirAviso(libroId); setAviso('activo'); setMensajeAviso('Te avisaremos por correo cuando vuelva a estar disponible') }
    } catch (e) { setAviso(previo); setMensajeAviso(mensajeDe(e)) }
  }

  if (noExiste) return <div className="vacio"><h2>Libro no encontrado</h2><p>El libro no existe o ya no está disponible.</p><Link to="/catalogo" className="btn">Volver al catálogo</Link></div>
  if (error) return <Alerta tipo="error">{error}</Alerta>
  if (!libro) return <Cargando />

  const ficha: [string, string | number | undefined][] = [
    ['ISBN', libro.isbn],
    ['Editorial', libro.editorial?.nombre],
    ['Año de publicación', libro.anioPublicacion],
    ['Páginas', libro.paginas],
    ['Idioma', libro.idioma],
    ['Formato', libro.formato],
    ['Encuadernación', libro.encuadernacion],
    ['Géneros', libro.generos.map((g) => g.nombre).join(', ')],
  ]

  return (
    <>
      <MigasDePan niveles={[
        { texto: 'Inicio', a: '/' },
        { texto: 'Catálogo', a: '/catalogo' },
        { texto: libro.categoria.nombre, a: `/catalogo?categorias=${libro.categoria.id}` },
        { texto: libro.titulo },
      ]} />

      <div className="detalle-libro">
        <Portada url={libro.portadaUrl} titulo={libro.titulo} variante="grande" id={libro.id} />
        <div className="detalle-info">
          <h1 className="mb-0">{libro.titulo}</h1>
          <p className="mb-0">
            por {libro.autores.map((a, i) => (
              <span key={a.id}>{i > 0 && ', '}<Link to={`/autores/${a.id}`}>{a.nombre}</Link></span>
            ))}
          </p>
          {opiniones && (
            <a href="#resenas" className="text-decoration-none">
              <Estrellas valor={opiniones.promedio} />{' '}
              <small>{opiniones.promedio !== null ? `${opiniones.promedio.toFixed(1)} (${opiniones.cantidadCalificaciones})` : 'Sin calificaciones'}</small>
            </a>
          )}
          <Precio precio={libro.precio} promo={promo} grande />
          {promo && <small className="text-muted">Oferta «{promo.promocion}» válida hasta el {new Date(promo.vigenteHasta).toLocaleDateString('es-CL')}</small>}
          <span className={`badge ${libro.disponible ? 'badge-disponible' : 'badge-agotado'}`} style={{ alignSelf: 'flex-start' }}>
            {libro.disponible ? (libro.stock <= 5 ? `Últimas ${libro.stock} unidades` : 'Disponible') : 'Agotado'}
          </span>

          {!esAdmin && libro.disponible && (
            <div className="d-flex flex-wrap gap-3 align-items-center">
              <ControlCantidad valor={cantidad} maximo={libro.stock} onCambiar={(v) => setCantidad(Math.max(1, v))} />
              <BotonAgregar libroId={libro.id} disponible cantidad={cantidad} texto="Agregar al carrito" />
              <BotonFavorito libroId={libro.id} />
            </div>
          )}
          {!esAdmin && !libro.disponible && (
            <div className="d-flex flex-wrap gap-2 align-items-center">
              {usuario
                ? <button type="button" className="btn" disabled={aviso === 'cargando'} onClick={alternarAviso}>
                    {aviso === 'activo' ? 'Cancelar aviso de disponibilidad' : '🔔 Avísame cuando vuelva'}
                  </button>
                : <Link className="btn" to={`/login?redir=/libros/${libro.id}`}>Inicia sesión para recibir aviso</Link>}
              <BotonFavorito libroId={libro.id} />
            </div>
          )}
          {mensajeAviso && <small className="text-muted" role="status">{mensajeAviso}</small>}
          {esAdmin && <Link to={`/admin/catalogo?editar=${libro.id}`} className="btn btn-secundario" style={{ alignSelf: 'flex-start' }}>Editar en el mantenedor</Link>}

          <h3 className="mt-3">Sinopsis</h3>
          <p>{libro.sinopsis || 'Sin sinopsis disponible.'}</p>
          {libro.tieneExtracto && <button type="button" className="btn btn-secundario" style={{ alignSelf: 'flex-start' }} onClick={() => setMuestra(true)}>📖 Leer muestra</button>}

          <h3 className="mt-3">Ficha técnica</h3>
          <div className="tabla-scroll">
            <table>
              <tbody>
                {ficha.filter(([, v]) => v !== undefined && v !== null && v !== '').map(([k, v]) => <tr key={k}><th scope="row">{k}</th><td>{v}</td></tr>)}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <SeccionResenas libroId={libro.id} onCambio={alCambiarOpiniones} />
      <ListaSugeridos titulo="También te puede interesar" libros={relacionados} />

      <Modal abierto={muestra} titulo={`Muestra: ${libro.titulo}`} onCerrar={() => setMuestra(false)} ancho="xl">
        <iframe src={libro.extractoUrl} title={`Muestra de ${libro.titulo}`} style={{ width: '100%', height: '70vh', border: 0 }} />
      </Modal>
    </>
  )
}
