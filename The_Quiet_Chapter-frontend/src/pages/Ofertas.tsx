import { useEffect, useState } from 'react'
import { promoApi } from '../api/promo'
import { mensajeDe } from '../api/http'
import { TarjetaLibro } from '../components/shopping/TarjetaLibro'
import { Cargando } from '../components/comunes/Cargando'
import { Alerta } from '../components/comunes/Alerta'
import type { Oferta } from '../interfaces/promo'

/** ofertas.html -> HU-111/113: libros con promocion vigente y precio original tachado. */
export default function Ofertas() {
  const [ofertas, setOfertas] = useState<Oferta[] | null>(null)
  const [error, setError] = useState('')
  useEffect(() => { promoApi.ofertas().then(setOfertas).catch((e) => { setError(mensajeDe(e)); setOfertas([]) }) }, [])

  return (
    <>
      <section className="hero">
        <h1>Ofertas Especiales</h1>
        <p>Descuentos por tiempo limitado en libros seleccionados.</p>
      </section>
      <Alerta tipo="error">{error}</Alerta>
      {ofertas === null ? <Cargando /> : ofertas.length === 0 ? (
        <div className="vacio"><p>No hay ofertas vigentes en este momento. ¡Vuelve pronto!</p></div>
      ) : (
        <div className="grid-libros">
          {ofertas.map((o) => (
            <TarjetaLibro key={o.libroId}
              libro={{ id: o.libroId, titulo: o.titulo, autores: o.autores, precio: o.precioFinal, portadaUrl: o.portadaUrl, disponible: o.disponible }}
              promo={{ libroId: o.libroId, precioOriginal: o.precioOriginal, precioFinal: o.precioFinal, descuento: o.precioOriginal - o.precioFinal, porcentaje: o.porcentaje, promocion: o.promocion, vigenteHasta: o.vigenteHasta }} />
          ))}
        </div>
      )}
    </>
  )
}
