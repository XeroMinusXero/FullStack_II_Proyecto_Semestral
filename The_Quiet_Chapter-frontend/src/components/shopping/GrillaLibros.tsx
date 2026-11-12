import { useEffect, useState } from 'react'
import { TarjetaLibro, type DatosTarjeta } from './TarjetaLibro'
import { promoApi } from '../../api/promo'
import { socialApi } from '../../api/social'
import type { PrecioPromo } from '../../interfaces/promo'

/** Grilla que enriquece las tarjetas con promociones vigentes y promedio de calificaciones (una llamada por servicio). */
export function GrillaLibros({ libros, vacio = 'No se encontraron libros.' }: { libros: DatosTarjeta[]; vacio?: string }) {
  const [promos, setPromos] = useState<Record<number, PrecioPromo>>({})
  const [promedios, setPromedios] = useState<Record<number, { promedio: number; cantidad: number }>>({})
  const clave = libros.map((l) => l.id).join(',')

  useEffect(() => {
    const ids = clave ? clave.split(',').map(Number) : []
    let vigente = true
    promoApi.precios(ids).then((r) => { if (vigente) setPromos(Object.fromEntries(r.map((p) => [p.libroId, p]))) }).catch(() => setPromos({}))
    socialApi.promedios(ids).then((r) => { if (vigente) setPromedios(Object.fromEntries(r.map((p) => [p.libroId, p]))) }).catch(() => setPromedios({}))
    return () => { vigente = false }
  }, [clave])

  if (!libros.length) return <p className="vacio">{vacio}</p>
  return (
    <div className="grid-libros">
      {libros.map((l) => <TarjetaLibro key={l.id} libro={l} promo={promos[l.id]} promedio={promedios[l.id]} />)}
    </div>
  )
}
