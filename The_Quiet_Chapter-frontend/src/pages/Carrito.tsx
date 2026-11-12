import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCarrito } from '../context/carrito'
import { useAuth } from '../context/auth'
import { checkoutApi } from '../api/checkout'
import { promoApi } from '../api/promo'
import { mensajeDe } from '../api/http'
import { ItemCarrito } from '../components/shopping/ItemCarrito'
import { ResumenCarrito } from '../components/shopping/ResumenCarrito'
import { FormularioCupon } from '../components/shopping/FormularioCupon'
import { ListaSugeridos } from '../components/shopping/ListaSugeridos'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import { Confirmar } from '../components/comunes/Confirmar'
import { guardarCheckoutId } from './checkout/proceso'
import type { LibroSugerido } from '../interfaces/promo'

/** carrito.html -> HU-013..021 (gestion del carrito), HU-100..102 (cupon), HU-120 (complementarios), HU-022 (iniciar compra). */
export default function Carrito() {
  const { carrito, cargando, avisos, cambiarCantidad, eliminar, vaciar } = useCarrito()
  const { usuario } = useAuth()
  const navegar = useNavigate()
  const [ocupado, setOcupado] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [confirmarVaciar, setConfirmarVaciar] = useState(false)
  const [iniciando, setIniciando] = useState(false)
  const [complementarios, setComplementarios] = useState<LibroSugerido[]>([])

  const idsCarrito = carrito?.items.map((i) => i.libroId).join(',') ?? ''
  useEffect(() => {
    const ids = idsCarrito ? idsCarrito.split(',').map(Number) : []
    promoApi.complementarios(ids, 4).then(setComplementarios).catch(() => setComplementarios([]))
  }, [idsCarrito])

  const conItem = async (libroId: number, fn: () => Promise<void>) => {
    setOcupado(libroId); setError('')
    try { await fn() } catch (e) { setError(mensajeDe(e)) } finally { setOcupado(null) }
  }

  const iniciarCompra = async () => {
    if (!carrito) return
    if (!usuario) { navegar('/login?redir=/carrito'); return }
    setIniciando(true); setError('')
    try {
      const proceso = await checkoutApi.iniciar(carrito.id)
      guardarCheckoutId(proceso.id)
      navegar('/checkout/direccion')
    } catch (e) { setError(mensajeDe(e)) } finally { setIniciando(false) }
  }

  if (cargando && !carrito) return <Cargando />

  if (!carrito || carrito.items.length === 0) {
    return (
      <>
        <h1>Mi Carrito</h1>
        <div className="vacio">
          <p>Tu carrito está vacío.</p>
          <Link to="/catalogo" className="btn">Ir al catálogo</Link>
        </div>
      </>
    )
  }

  // El backend marca puedeComprar=false para un carrito anonimo (aun no hay cliente): eso NO es un bloqueo,
  // el visitante debe poder pulsar el boton para ir a iniciar sesion. Bloquean solo los productos no disponibles.
  const hayNoDisponibles = carrito.items.some((i) => !i.disponible) || avisos.some((a) => a.bloqueante)
  const bloqueado = hayNoDisponibles || (!!usuario && !carrito.puedeComprar)

  return (
    <>
      <h1>Mi Carrito</h1>
      <Alerta tipo="error" onCerrar={() => setError('')}>{error}</Alerta>
      {avisos.length > 0 && (
        <div className="alert alert-warning" role="status">
          <strong>Revisamos tu carrito:</strong>
          <ul className="mb-0">{avisos.map((a, i) => <li key={i}>{a.mensaje}</li>)}</ul>
        </div>
      )}

      <div className="row g-4">
        <div className="col-lg-8">
          {carrito.items.map((item) => (
            <ItemCarrito key={item.libroId} item={item} ocupado={ocupado === item.libroId}
              onCantidad={(c) => conItem(item.libroId, () => cambiarCantidad(item.libroId, c))}
              onEliminar={() => conItem(item.libroId, () => eliminar(item.libroId))} />
          ))}
          <div className="d-flex flex-wrap gap-2 mt-3">
            <Link to="/catalogo" className="btn btn-secundario">Seguir comprando</Link>
            <button type="button" className="btn btn-peligro" onClick={() => setConfirmarVaciar(true)}>Vaciar carrito</button>
          </div>
        </div>
        <div className="col-lg-4">
          <ResumenCarrito carrito={carrito} />
          <div className="mt-3"><FormularioCupon codigoAplicado={carrito.codigoCuponAplicado} /></div>
          <button type="button" className="btn w-100 mt-3" disabled={bloqueado || iniciando} onClick={iniciarCompra}>
            {iniciando ? 'Preparando compra...' : usuario ? 'Iniciar compra' : 'Inicia sesión para comprar'}
          </button>
          {hayNoDisponibles && <small className="text-danger d-block mt-1">Corrige los productos no disponibles para continuar.</small>}
        </div>
      </div>

      <ListaSugeridos titulo="Complementa tu compra" libros={complementarios} />

      <Confirmar abierto={confirmarVaciar} titulo="Vaciar carrito" mensaje="¿Seguro que deseas eliminar todos los productos del carrito?"
        textoConfirmar="Vaciar" onCancelar={() => setConfirmarVaciar(false)}
        onConfirmar={() => { setConfirmarVaciar(false); conItem(-1, vaciar) }} />
    </>
  )
}
