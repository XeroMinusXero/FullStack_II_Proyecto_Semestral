import { useEffect, useRef } from 'react'
import { NavLink } from 'react-router-dom'
import estilos from './Pestanas.module.css'

const ENLACES: [string, string][] = [
  ['/cuenta/perfil', 'Datos personales'],
  ['/cuenta/pedidos', 'Mis pedidos'],
  ['/cuenta/direcciones', 'Mis direcciones'],
  ['/favoritos', 'Favoritos'],
  ['/cuenta/resenas', 'Mis reseñas'],
  ['/cuenta/notificaciones', 'Mis correos'],
]

/** Pestanas de "Mi cuenta" (miPerfil.html, misPedidos.html, misDirecciones.html, favoritos.html). */
export function MenuCuenta() {
  // En pantallas chicas la barra se desplaza: se deja visible la seccion activa.
  const barra = useRef<HTMLElement>(null)
  useEffect(() => {
    barra.current?.querySelector('[aria-current="page"]')?.scrollIntoView({ block: 'nearest', inline: 'center' })
  }, [])
  return (
    <nav ref={barra} className={estilos.barra} aria-label="Secciones de mi cuenta">
      {ENLACES.map(([a, texto]) => (
        <NavLink key={a} to={a} className={({ isActive }) => (isActive ? estilos.activa : estilos.pestana)}>{texto}</NavLink>
      ))}
    </nav>
  )
}
