import { createContext, useContext } from 'react'
import type { Perfil } from '../interfaces/usuario'

export interface EstadoAuth {
  usuario: Perfil | null
  cargando: boolean
  esAdmin: boolean
  iniciarSesion: (email: string, contrasena: string) => Promise<Perfil>
  cerrarSesion: () => void
  refrescarPerfil: () => Promise<void>
}

export const AuthContext = createContext<EstadoAuth | null>(null)

export function useAuth(): EstadoAuth {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
