import { useState } from 'react'
import { useAccion } from '../../hooks/useAccion'
import { MenuAdmin } from '../../components/layout/MenuAdmin'
import { Avisos } from '../../components/comunes/Avisos'
import { Pestanas } from '../../components/comunes/Pestanas'
import { GestionCupones } from '../../components/admin/GestionCupones'
import { GestionPromociones } from '../../components/admin/GestionPromociones'

type Pestana = 'cupones' | 'promociones'

/** HU-092..099 (cupones) y HU-111..113 (promociones): cada pestana es un componente independiente. */
export default function AdminPromociones() {
  const [pestana, setPestana] = useState<Pestana>('cupones')
  const accion = useAccion()
  return (
    <>
      <h1>Cupones y promociones</h1>
      <MenuAdmin />
      <Pestanas etiqueta="Tipo de descuento" valor={pestana} onCambiar={setPestana}
        opciones={[{ valor: 'cupones', texto: 'Cupones' }, { valor: 'promociones', texto: 'Promociones' }]} />
      <Avisos estado={accion} />
      {pestana === 'cupones' ? <GestionCupones accion={accion} /> : <GestionPromociones accion={accion} />}
    </>
  )
}
