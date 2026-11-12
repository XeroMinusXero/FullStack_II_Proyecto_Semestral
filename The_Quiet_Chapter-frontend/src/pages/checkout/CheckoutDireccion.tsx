import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { usuariosApi } from '../../api/usuarios'
import { checkoutApi } from '../../api/checkout'
import { Alerta } from '../../components/comunes/Alerta'
import { Campo } from '../../components/comunes/Campo'
import { Cargando } from '../../components/comunes/Cargando'
import { PasosCheckout } from './PasosCheckout'
import { useProcesoCompra } from './proceso'
import { REGIONES } from '../../utils/regiones'
import { validarDireccion, type Errores } from '../../utils/validaciones'
import type { Direccion, DireccionSolicitud } from '../../interfaces/usuario'

const VACIA: DireccionSolicitud = { calle: '', numero: '', comuna: '', region: '' }

/** checkoutDireccion.html -> HU-023: elegir una direccion guardada o ingresar una nueva. */
export default function CheckoutDireccion() {
  const navegar = useNavigate()
  const { proceso, error, ejecutar } = useProcesoCompra()
  const [guardadas, setGuardadas] = useState<Direccion[] | null>(null)
  const [elegida, setElegida] = useState<number | 'nueva'>('nueva')
  const [form, setForm] = useState<DireccionSolicitud>(VACIA)
  const [errores, setErrores] = useState<Errores<DireccionSolicitud>>({})
  const [guardarEnCuenta, setGuardarEnCuenta] = useState(true)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    usuariosApi.direcciones().then((d) => {
      setGuardadas(d)
      const pred = d.find((x) => x.predeterminada) ?? d[0]
      if (pred) setElegida(pred.id)
    }).catch(() => setGuardadas([]))
  }, [])

  const continuar = async (e: FormEvent) => {
    e.preventDefault()
    let datos: DireccionSolicitud
    if (elegida === 'nueva') {
      const err = validarDireccion(form)
      setErrores(err)
      if (Object.keys(err).length) return
      datos = { calle: form.calle.trim(), numero: form.numero.trim(), comuna: form.comuna.trim(), region: form.region }
    } else {
      const d = guardadas!.find((x) => x.id === elegida)!
      datos = { calle: d.calle, numero: d.numero, comuna: d.comuna, region: d.region }
    }
    setEnviando(true)
    const p = await ejecutar((id) => checkoutApi.direccion(id, datos))
    if (p && elegida === 'nueva' && guardarEnCuenta) usuariosApi.agregarDireccion(datos).catch(() => undefined)
    setEnviando(false)
    if (p) navegar('/checkout/envio')
  }

  const campo = (k: keyof DireccionSolicitud) => ({
    value: form[k],
    error: errores[k],
    onChange: (e: { target: { value: string } }) => setForm({ ...form, [k]: e.target.value }),
  })

  if (!proceso && !error) return <Cargando />
  return (
    <>
      <h1>Finalizar compra</h1>
      <PasosCheckout actual={1} procesoId={proceso?.id} />
      <Alerta tipo="error">{error}</Alerta>
      {proceso && guardadas && (
        <form onSubmit={continuar} noValidate className="ancho" style={{ maxWidth: 640 }}>
          <h2>Dirección de envío</h2>
          {guardadas.map((d) => (
            <div className="form-check mb-2" key={d.id}>
              <input className="form-check-input" type="radio" name="direccion" id={`dir${d.id}`} checked={elegida === d.id} onChange={() => setElegida(d.id)} />
              <label className="form-check-label" htmlFor={`dir${d.id}`}>
                {d.calle} {d.numero}, {d.comuna}, {d.region} {d.predeterminada && <span className="badge badge-disponible">Predeterminada</span>}
              </label>
            </div>
          ))}
          <div className="form-check mb-3">
            <input className="form-check-input" type="radio" name="direccion" id="dirNueva" checked={elegida === 'nueva'} onChange={() => setElegida('nueva')} />
            <label className="form-check-label" htmlFor="dirNueva">Usar una dirección nueva</label>
          </div>
          {elegida === 'nueva' && (
            <>
              <Campo id="calle" etiqueta="Calle" maxLength={120} {...campo('calle')} />
              <Campo id="numero" etiqueta="Número / Depto" maxLength={30} {...campo('numero')} />
              <Campo id="comuna" etiqueta="Comuna" maxLength={80} {...campo('comuna')} />
              <div className={`campo${errores.region ? ' error' : ''}`}>
                <label htmlFor="region">Región</label>
                <select id="region" value={form.region} onChange={(e) => setForm({ ...form, region: e.target.value })}>
                  <option value="">Seleccione una región</option>
                  {REGIONES.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
                <span className="mensaje-error">{errores.region}</span>
              </div>
              <div className="form-check mb-3">
                <input className="form-check-input" type="checkbox" id="guardarDir" checked={guardarEnCuenta} onChange={(e) => setGuardarEnCuenta(e.target.checked)} />
                <label className="form-check-label" htmlFor="guardarDir">Guardar en mis direcciones</label>
              </div>
            </>
          )}
          <div className="d-flex justify-content-between gap-2">
            <button type="button" className="btn btn-secundario" onClick={() => navegar('/carrito')}>Volver al carrito</button>
            <button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Continuar'}</button>
          </div>
        </form>
      )}
    </>
  )
}
