import { Modal } from './Modal'

/** Confirmacion antes de acciones destructivas (HU-017 vaciar carrito, eliminar, cancelar). */
export function Confirmar({ abierto, titulo, mensaje, textoConfirmar = 'Confirmar', peligro = true, onConfirmar, onCancelar }: {
  abierto: boolean
  titulo: string
  mensaje: string
  textoConfirmar?: string
  peligro?: boolean
  onConfirmar: () => void
  onCancelar: () => void
}) {
  return (
    <Modal abierto={abierto} titulo={titulo} onCerrar={onCancelar} ancho="sm"
      pie={<>
        <button type="button" className="btn btn-secundario" onClick={onCancelar}>Volver</button>
        <button type="button" className={`btn${peligro ? ' btn-peligro' : ''}`} onClick={onConfirmar}>{textoConfirmar}</button>
      </>}>
      <p className="mb-0">{mensaje}</p>
    </Modal>
  )
}
