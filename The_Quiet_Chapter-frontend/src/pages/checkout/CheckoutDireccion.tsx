import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { usuariosApi } from '../../api/usuarios'
import { checkoutApi } from '../../api/checkout'
import { useFormulario } from '../../hooks/useFormulario'
import { Alerta } from '../../components/comunes/Alerta'
import { Cargando } from '../../components/comunes/Cargando'
import { OpcionesRadio } from '../../components/comunes/OpcionesRadio'
import { CamposDireccion } from '../../components/cuenta/CamposDireccion'
import { PasosCheckout } from './PasosCheckout'
import { useProcesoCompra } from './proceso'
import { DIRECCION_VACIA, camposDe, limpiarDireccion } from '../../utils/direccion'
import { validarDireccion, type DatosDireccion } from '../../utils/validaciones'
import type { Direccion } from '../../interfaces/usuario'
import estilos from './Checkout.module.css'

const NUEVA = 'nueva' as const

/** checkoutDireccion.html -> HU-023: elegir una direccion guardada o ingresar una nueva. */
export default function CheckoutDireccion() {
  const navegar = useNavigate()
  const { proceso, error, ejecutar } = useProcesoCompra()
  const [guardadas, setGuardadas] = useState<Direccion[] | null>(null)
  const [elegida, setElegida] = useState<number | typeof NUEVA>(NUEVA)
  const [guardarEnCuenta, setGuardarEnCuenta] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const form = useFormulario(DIRECCION_VACIA)

  useEffect(() => {
    usuariosApi.direcciones().then((d) => {
      setGuardadas(d)
      const predeterminada = d.find((x) => x.predeterminada) ?? d[0]
      if (predeterminada) setElegida(predeterminada.id)
    }).catch(() => setGuardadas([]))
  }, [])

  const continuar = async (e: FormEvent) => {
    e.preventDefault()
    let datos: DatosDireccion
    if (elegida === NUEVA) {
      if (!form.validar(validarDireccion)) return
      datos = limpiarDireccion(form.valores)
    } else {
      datos = camposDe(guardadas!.find((x) => x.id === elegida)!)
    }
    setEnviando(true)
    const p = await ejecutar((id) => checkoutApi.direccion(id, datos))
    // Guardar la direccion en la cuenta es opcional: si falla, la compra sigue igual.
    if (p && elegida === NUEVA && guardarEnCuenta) usuariosApi.agregarDireccion(datos).catch(() => undefined)
    setEnviando(false)
    if (p) navegar('/checkout/envio')
  }

  if (!proceso && !error) return <Cargando />
  return (
    <>
      <h1>Finalizar compra</h1>
      <PasosCheckout actual={1} procesoId={proceso?.id} />
      <Alerta tipo="error">{error}</Alerta>
      {proceso && guardadas && (
        <form onSubmit={continuar} noValidate className={`ancho ${estilos.paso}`}>
          <h2>Dirección de envío</h2>
          <OpcionesRadio<number | typeof NUEVA> nombre="direccion" etiqueta="Dirección de envío" valor={elegida} onCambiar={setElegida}
            opciones={[
              ...guardadas.map((d) => ({
                valor: d.id,
                contenido: <>{d.calle} {d.numero}, {d.comuna}, {d.region} {d.predeterminada && <span className="badge badge-disponible ms-1">Predeterminada</span>}</>,
              })),
              { valor: NUEVA, contenido: 'Usar una dirección nueva' },
            ]} />
          {elegida === NUEVA && (
            <>
              <CamposDireccion form={form} prefijo="envio" />
              <div className="form-check mb-3">
                <input className="form-check-input" type="checkbox" id="guardarDir" checked={guardarEnCuenta} onChange={(e) => setGuardarEnCuenta(e.target.checked)} />
                <label className="form-check-label" htmlFor="guardarDir">Guardar en mis direcciones</label>
              </div>
            </>
          )}
          <div className={estilos.acciones}>
            <button type="button" className="btn btn-secundario" onClick={() => navegar('/carrito')}>Volver al carrito</button>
            <button className="btn" type="submit" disabled={enviando}>{enviando ? 'Guardando...' : 'Continuar'}</button>
          </div>
        </form>
      )}
    </>
  )
}
