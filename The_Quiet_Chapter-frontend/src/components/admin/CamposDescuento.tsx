import { Campo, CampoSeleccion } from '../comunes/Campo'
import type { TipoDescuento } from '../../interfaces/promo'

/** Campos comunes a cupones y promociones: tipo de descuento, valor y vigencia. */
export function CamposDescuento({ prefijo, tipo, valor, inicio, fin, errores, onCambiar }: {
  prefijo: string
  tipo: TipoDescuento
  valor: number
  inicio: string
  fin: string
  errores: { valor?: string; fechaInicio?: string; fechaFin?: string }
  /** Recibe solo los campos que cambiaron. */
  onCambiar: (cambio: { tipoDescuento?: TipoDescuento; valor?: number; fechaInicio?: string; fechaFin?: string }) => void
}) {
  return (
    <>
      <div className="col-md-6">
        <CampoSeleccion id={`${prefijo}-tipo`} etiqueta="Tipo de descuento" value={tipo}
          onChange={(e) => onCambiar({ tipoDescuento: e.target.value as TipoDescuento })}>
          <option value="PORCENTAJE">Porcentaje</option>
          <option value="MONTO_FIJO">Monto fijo</option>
        </CampoSeleccion>
      </div>
      <div className="col-md-6">
        <Campo id={`${prefijo}-valor`} etiqueta={`Valor ${tipo === 'PORCENTAJE' ? '(1 a 100 %)' : '(CLP)'}`} type="number"
          min={1} max={tipo === 'PORCENTAJE' ? 100 : undefined} value={valor} error={errores.valor}
          onChange={(e) => onCambiar({ valor: Number(e.target.value) })} />
      </div>
      <div className="col-md-6">
        <Campo id={`${prefijo}-inicio`} etiqueta="Inicio" type="datetime-local" value={inicio} error={errores.fechaInicio}
          onChange={(e) => onCambiar({ fechaInicio: e.target.value })} />
      </div>
      <div className="col-md-6">
        <Campo id={`${prefijo}-fin`} etiqueta="Término" type="datetime-local" value={fin} error={errores.fechaFin}
          onChange={(e) => onCambiar({ fechaFin: e.target.value })} />
      </div>
    </>
  )
}
