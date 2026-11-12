import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { FavoritosContext } from './favoritos'
import { socialApi } from '../api/social'
import { useAuth } from './auth'

/** HU-078: ids de favoritos del cliente para pintar el indicador en todo el catalogo. */
export function FavoritosProvider({ children }: { children: ReactNode }) {
  const { usuario, esAdmin } = useAuth()
  const [ids, setIds] = useState<Set<number>>(new Set())
  const disponible = !!usuario && !esAdmin
  const usuarioId = usuario?.id

  const recargar = useCallback(async () => {
    if (!usuarioId || esAdmin) {
      setIds(new Set())
      return
    }
    try {
      setIds(new Set(await socialApi.idsFavoritos()))
    } catch {
      setIds(new Set())
    }
  }, [usuarioId, esAdmin])

  useEffect(() => {
    let vigente = true
    const tarea = usuarioId && !esAdmin ? socialApi.idsFavoritos().catch(() => [] as number[]) : Promise.resolve([] as number[])
    tarea.then((lista) => { if (vigente) setIds(new Set(lista)) })
    return () => { vigente = false }
  }, [usuarioId, esAdmin])

  const alternar = useCallback(async (libroId: number) => {
    const esta = ids.has(libroId)
    const lista = esta ? await socialApi.quitarFavorito(libroId) : await socialApi.agregarFavorito(libroId)
    setIds(new Set(lista.map((f) => f.libroId)))
    return !esta
  }, [ids])

  const valor = useMemo(() => ({ ids, disponible, alternar, recargar }), [ids, disponible, alternar, recargar])
  return <FavoritosContext.Provider value={valor}>{children}</FavoritosContext.Provider>
}
