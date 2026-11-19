import { useEffect, useState } from 'react'
import { catalogoApi } from '../../api/catalogo'
import { mensajeDe } from '../../api/http'
import type { Accion } from '../../hooks/useAccion'
import { CampoTextoLargo } from '../comunes/Campo'
import { LIMITES, validarBiografia } from '../../utils/validaciones'
import type { Autor, Clasificacion } from '../../interfaces/catalogo'

/** HU-009 (administrador): elegir un autor y editar su biografia. */
export function EditorBiografia({ accion }: { accion: Accion }) {
  const [autores, setAutores] = useState<Clasificacion[]>([])
  const [autor, setAutor] = useState<Autor | null>(null)
  const [error, setError] = useState<string>()

  useEffect(() => { catalogoApi.autores().then(setAutores).catch(() => undefined) }, [])

  const elegir = (id: string) => {
    setError(undefined)
    if (!id) { setAutor(null); return }
    catalogoApi.autor(Number(id)).then(setAutor).catch((e) => accion.setError(mensajeDe(e)))
  }

  const guardar = () => {
    if (!autor) return
    const invalido = validarBiografia(autor.biografia ?? '')
    setError(invalido)
    if (!invalido) accion.ejecutar(() => catalogoApi.biografia(autor.id, (autor.biografia ?? '').trim()), 'Biografía actualizada')
  }

  return (
    <section className="seccion mt-4">
      <h2>Biografías de autores</h2>
      <select className="form-select selector-corto" aria-label="Autor" value={autor?.id ?? ''} onChange={(e) => elegir(e.target.value)}>
        <option value="">Selecciona un autor</option>
        {autores.map((a) => <option key={a.id} value={a.id}>{a.nombre}</option>)}
      </select>
      {autor && (
        <div className="mt-3">
          <CampoTextoLargo id="biografia" etiqueta={`Biografía de ${autor.nombre}`} rows={5} maxLength={LIMITES.biografia}
            value={autor.biografia ?? ''} error={error} onChange={(e) => setAutor({ ...autor, biografia: e.target.value })} />
          <button type="button" className="btn" onClick={guardar}>Guardar biografía</button>
        </div>
      )}
    </section>
  )
}
