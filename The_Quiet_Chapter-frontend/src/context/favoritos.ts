import { createContext, useContext } from 'react'

export interface EstadoFavoritos {
  ids: Set<number>
  disponible: boolean
  alternar: (libroId: number) => Promise<boolean>
  recargar: () => Promise<void>
}

export const FavoritosContext = createContext<EstadoFavoritos | null>(null)

export function useFavoritos(): EstadoFavoritos {
  const ctx = useContext(FavoritosContext)
  if (!ctx) throw new Error('useFavoritos debe usarse dentro de <FavoritosProvider>')
  return ctx
}
