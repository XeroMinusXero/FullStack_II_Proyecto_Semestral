import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { AuthContext } from './auth'
import { usuariosApi } from '../api/usuarios'
import { borrarSesion, guardarSesion, leerSesion } from '../api/http'
import type { Perfil } from '../interfaces/usuario'

/** Sesion JWT emitida por user-service. Cada microservicio valida el mismo token. */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Perfil | null>(null)
  const [cargando, setCargando] = useState(true)

  const refrescarPerfil = useCallback(async () => {
    if (!leerSesion()) {
      setUsuario(null)
      return
    }
    try {
      setUsuario(await usuariosApi.perfil())
    } catch {
      borrarSesion()
      setUsuario(null)
    }
  }, [])

  useEffect(() => {
    const inicial = leerSesion()
      ? usuariosApi.perfil().then(setUsuario).catch(() => { borrarSesion(); setUsuario(null) })
      : Promise.resolve()
    inicial.finally(() => setCargando(false))
    const alExpirar = () => setUsuario(null)
    window.addEventListener('tqc:sesion-expirada', alExpirar)
    return () => window.removeEventListener('tqc:sesion-expirada', alExpirar)
  }, [])

  const iniciarSesion = useCallback(async (email: string, contrasena: string) => {
    const sesion = await usuariosApi.login(email, contrasena)
    guardarSesion(sesion.accessToken, sesion.expiresIn)
    setUsuario(sesion.usuario)
    window.dispatchEvent(new Event('tqc:sesion-iniciada'))
    return sesion.usuario
  }, [])

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setUsuario(null)
    window.dispatchEvent(new Event('tqc:sesion-cerrada'))
  }, [])

  const valor = useMemo(
    () => ({ usuario, cargando, esAdmin: usuario?.rol === 'ADMINISTRADOR', iniciarSesion, cerrarSesion, refrescarPerfil }),
    [usuario, cargando, iniciarSesion, cerrarSesion, refrescarPerfil],
  )
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
