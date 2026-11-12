import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/auth'
import { Cargando } from '../comunes/Cargando'
import type { Rol } from '../../interfaces/usuario'

/** Guardas de navegacion (HU-022 checkout exige sesion, HU-045 admin exige rol). El backend valida igual. */
export function RutaProtegida({ rol, children }: { rol?: Rol; children: ReactNode }) {
  const { usuario, cargando } = useAuth()
  const ubicacion = useLocation()
  if (cargando) return <Cargando texto="Verificando sesión..." />
  if (!usuario) return <Navigate to={`/login?redir=${encodeURIComponent(ubicacion.pathname + ubicacion.search)}`} replace />
  if (rol === 'ADMINISTRADOR' && usuario.rol !== 'ADMINISTRADOR') {
    return <div className="vacio"><h2>Acceso restringido</h2><p>Esta sección es solo para administradores.</p></div>
  }
  if (rol === 'CLIENTE' && usuario.rol === 'ADMINISTRADOR') {
    return <div className="vacio"><h2>Sección de clientes</h2><p>El administrador no puede realizar compras ni gestionar favoritos.</p></div>
  }
  return <>{children}</>
}
