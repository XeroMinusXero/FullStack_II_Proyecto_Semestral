import { useEffect, useRef } from 'react'
import { NavLink } from 'react-router-dom'
import estilos from './Pestanas.module.css'

const ENLACES: [string, string, string][] = [
  ['/admin', '◈', 'Panel'],
  ['/admin/catalogo', '▤', 'Catálogo'],
  ['/admin/pedidos', '▣', 'Pedidos'],
  ['/admin/dashboard', '▥', 'Reportes'],
  ['/admin/promociones', '%', 'Promociones'],
  ['/admin/resenas', '★', 'Reseñas'],
  ['/admin/boletines', '✉', 'Boletines'],
  ['/admin/opciones', '⇄', 'Envíos y pagos'],
  ['/admin/correos', '✓', 'Correos'],
]

/** Navegacion del panel de administracion (secciones de adminCatalogo, adminPedidos, adminDashboard...). */
export function MenuAdmin() {
  // En pantallas chicas la barra se desplaza: se deja visible la seccion activa.
  const barra = useRef<HTMLElement>(null)
  useEffect(() => {
    barra.current?.querySelector('[aria-current="page"]')?.scrollIntoView({ block: 'nearest', inline: 'center' })
  }, [])
  return (
    <nav ref={barra} className={estilos.barra} aria-label="Secciones de administración">
      {ENLACES.map(([a, icono, texto]) => (
        <NavLink key={a} to={a} end className={({ isActive }) => (isActive ? estilos.activa : estilos.pestana)}>
          <span className={estilos.icono} aria-hidden="true">{icono}</span>{texto}
        </NavLink>
      ))}
    </nav>
  )
}
