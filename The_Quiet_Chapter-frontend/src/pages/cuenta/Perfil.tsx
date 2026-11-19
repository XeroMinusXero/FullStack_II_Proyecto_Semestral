import { useAuth } from '../../context/auth'
import { MenuCuenta } from '../../components/layout/MenuCuenta'
import { FormularioDatosPersonales } from '../../components/cuenta/FormularioDatosPersonales'
import { FormularioCorreo } from '../../components/cuenta/FormularioCorreo'
import { FormularioContrasena } from '../../components/cuenta/FormularioContrasena'
import { fecha } from '../../utils/formato'

/** miPerfil.html -> HU-037 datos personales, HU-038 correo, HU-039 contrasena. Cada formulario es independiente. */
export default function Perfil() {
  const { usuario, cerrarSesion } = useAuth()
  if (!usuario) return null // RutaProtegida ya garantiza la sesion; esto solo satisface a TypeScript.

  return (
    <>
      <h1>Mi cuenta</h1>
      {usuario.rol === 'CLIENTE' && <MenuCuenta />}
      <p className="text-muted">{usuario.rol === 'CLIENTE' ? 'Cliente' : 'Administrador'} desde {fecha(usuario.fechaRegistro)}</p>
      <div className="row g-4">
        <div className="col-lg-6"><FormularioDatosPersonales /></div>
        <div className="col-lg-6">
          <FormularioCorreo />
          <FormularioContrasena />
        </div>
      </div>
      <button type="button" className="btn btn-peligro mt-4" onClick={cerrarSesion}>Cerrar sesión</button>
    </>
  )
}
