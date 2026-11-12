import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/auth'
import { useCarrito } from '../../context/carrito'

/** Barra de navegacion del sitio original + sesion real (HU-021 contador del carrito). */
export function Header() {
  const { usuario, esAdmin, cerrarSesion } = useAuth()
  const { unidades } = useCarrito()
  const navegar = useNavigate()

  const salir = () => {
    cerrarSesion()
    navegar('/')
  }

  return (
    <header>
      <Link to="/" className="logo" style={{ color: 'var(--crema)' }}>✒️ The Quiet Chapter</Link>
      <nav aria-label="Principal">
        <NavLink to="/" end>Inicio</NavLink>
        <NavLink to="/catalogo">Catálogo</NavLink>
        <NavLink to="/ofertas">Ofertas</NavLink>
        {!esAdmin && <NavLink to="/favoritos">Favoritos</NavLink>}
        {!esAdmin && (
          <NavLink to="/carrito" className="cart-link" aria-label={`Carrito, ${unidades} artículos`}>
            Carrito <span className="cart-count">{unidades}</span>
          </NavLink>
        )}
        {!usuario && (
          <>
            <NavLink to="/login">Ingresar</NavLink>
            <NavLink to="/registro">Registrarse</NavLink>
          </>
        )}
        {usuario && esAdmin && (
          <NavLink to="/admin" style={{ color: 'var(--dorado)', fontWeight: 700 }}>⚙ Panel Admin</NavLink>
        )}
        {usuario && !esAdmin && <NavLink to="/cuenta/perfil">👤 {usuario.nombre}</NavLink>}
        {usuario && (
          <a href="#salir" onClick={(e) => { e.preventDefault(); salir() }}>Salir</a>
        )}
      </nav>
    </header>
  )
}
