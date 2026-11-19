import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { usuariosApi } from '../../api/usuarios'
import { useAccion } from '../../hooks/useAccion'
import { useFormulario } from '../../hooks/useFormulario'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { Avisos } from '../../components/comunes/Avisos'
import { Cargando } from '../../components/comunes/Cargando'
import { Confirmar } from '../../components/comunes/Confirmar'
import { CamposDireccion } from '../../components/cuenta/CamposDireccion'
import { DIRECCION_VACIA, camposDe, limpiarDireccion } from '../../utils/direccion'
import { validarDireccion } from '../../utils/validaciones'
import type { Direccion } from '../../interfaces/usuario'

/** misDirecciones.html -> HU-044: CRUD de direcciones y predeterminada. */
export default function Direcciones() {
  const [lista, setLista] = useState<Direccion[] | null>(null)
  const [editando, setEditando] = useState<number | null>(null)
  const [eliminar, setEliminar] = useState<Direccion | null>(null)
  const form = useFormulario(DIRECCION_VACIA)
  const accion = useAccion()
  const { setError } = accion

  const cargar = useCallback(() => usuariosApi.direcciones().then(setLista).catch(() => setError('No se pudieron cargar tus direcciones')), [setError])
  useEffect(() => { cargar() }, [cargar])

  const cancelarEdicion = () => { setEditando(null); form.reiniciar() }

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.validar(validarDireccion)) return
    const datos = limpiarDireccion(form.valores)
    const ok = await accion.ejecutar(
      () => (editando ? usuariosApi.editarDireccion(editando, datos) : usuariosApi.agregarDireccion(datos)),
      editando ? 'Dirección actualizada' : 'Dirección agregada', cargar)
    if (ok) cancelarEdicion()
  }

  return (
    <>
      <h1>Mi cuenta</h1>
      <MenuCuenta />
      <Avisos estado={accion} />
      <div className="row g-4">
        <div className="col-lg-7">
          <h2>Mis direcciones</h2>
          {!lista ? <Cargando /> : lista.length === 0 ? <p className="text-muted">No tienes direcciones guardadas.</p> : lista.map((d) => (
            <div key={d.id} className="resumen-caja mb-3">
              <div className="d-flex justify-content-between flex-wrap gap-2">
                <div>
                  <strong>{d.calle} {d.numero}</strong><br />{d.comuna}, {d.region}
                  {d.predeterminada && <div><span className="badge badge-disponible mt-1">Predeterminada</span></div>}
                </div>
                <div className="d-flex flex-wrap gap-2 align-items-start">
                  {!d.predeterminada && (
                    <button type="button" className="btn btn-secundario"
                      onClick={() => accion.ejecutar(() => usuariosApi.predeterminada(d.id), 'Dirección predeterminada actualizada', cargar)}>Predeterminar</button>
                  )}
                  <button type="button" className="btn btn-secundario" onClick={() => { setEditando(d.id); form.reiniciar(camposDe(d)) }}>Editar</button>
                  <button type="button" className="btn btn-peligro" onClick={() => setEliminar(d)}>Eliminar</button>
                </div>
              </div>
            </div>
          ))}
        </div>
        <div className="col-lg-5">
          <form onSubmit={guardar} noValidate className="ancho">
            <h2>{editando ? 'Editar dirección' : 'Nueva dirección'}</h2>
            <CamposDireccion form={form} />
            <div className="d-flex gap-2">
              <button className="btn" type="submit">{editando ? 'Guardar cambios' : 'Agregar dirección'}</button>
              {editando && <button type="button" className="btn btn-secundario" onClick={cancelarEdicion}>Cancelar</button>}
            </div>
          </form>
        </div>
      </div>
      <Confirmar abierto={!!eliminar} titulo="Eliminar dirección" mensaje="¿Seguro que deseas eliminar esta dirección?" textoConfirmar="Eliminar"
        onCancelar={() => setEliminar(null)}
        onConfirmar={() => { const d = eliminar!; setEliminar(null); accion.ejecutar(() => usuariosApi.eliminarDireccion(d.id), 'Dirección eliminada', cargar) }} />
    </>
  )
}
