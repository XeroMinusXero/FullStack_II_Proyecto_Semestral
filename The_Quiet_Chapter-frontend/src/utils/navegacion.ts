/**
 * Destino del parametro ?redir= tras iniciar sesion o registrarse. Solo se aceptan rutas internas
 * ("/carrito"), nunca "//otro-sitio.com" ni URLs completas: evita redirecciones abiertas.
 */
export function destinoSeguro(redir: string | null): string | null {
  return redir && redir.startsWith('/') && !redir.startsWith('//') ? redir : null
}
