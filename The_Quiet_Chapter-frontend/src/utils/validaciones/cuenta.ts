import { LIMITES, MENSAJES, reglas, type Errores, soloErrores } from './reglas'

/** Valida un nombre o apellido: obligatorio, solo letras y largo maximo del backend. */
function nombre(v: string): string | undefined {
  if (!reglas.requerido(v)) return MENSAJES.requerido
  if (!reglas.soloLetras(v)) return MENSAJES.soloLetras
  if (!reglas.maximo(v, LIMITES.nombrePersona)) return MENSAJES.maximo(LIMITES.nombrePersona)
}

function correo(v: string): string | undefined {
  if (!reglas.requerido(v)) return MENSAJES.requerido
  if (!reglas.email(v) || !reglas.maximo(v, LIMITES.email)) return MENSAJES.email
}

function telefono(v: string): string | undefined {
  if (!reglas.requerido(v)) return MENSAJES.requerido
  if (!reglas.telefono(v)) return MENSAJES.telefono
}

export interface DatosRegistro { nombre: string; apellido: string; email: string; telefono: string; contrasena: string; confirmar: string }
/** HU-032/033 (registro.html). */
export function validarRegistro(f: DatosRegistro): Errores<DatosRegistro> {
  return soloErrores({
    nombre: nombre(f.nombre),
    apellido: nombre(f.apellido),
    email: correo(f.email),
    telefono: telefono(f.telefono),
    contrasena: reglas.passwordFuerte(f.contrasena) ? undefined : MENSAJES.passwordFuerte,
    confirmar: f.confirmar === f.contrasena ? undefined : MENSAJES.noCoinciden,
  })
}

export interface DatosLogin { email: string; contrasena: string }
/** HU-034 (login.html). */
export function validarLogin(f: DatosLogin): Errores<DatosLogin> {
  return soloErrores({
    email: correo(f.email),
    contrasena: reglas.requerido(f.contrasena) ? undefined : MENSAJES.requerido,
  })
}

export interface DatosPerfil { nombre: string; apellido: string; telefono: string }
/** HU-037 (miPerfil.html). */
export function validarPerfil(f: DatosPerfil): Errores<DatosPerfil> {
  return soloErrores({ nombre: nombre(f.nombre), apellido: nombre(f.apellido), telefono: telefono(f.telefono) })
}

/** HU-038 y suscripcion al boletin: un solo correo. */
export function validarCorreo(email: string): Errores<{ email: string }> {
  return soloErrores({ email: correo(email) })
}

export interface DatosCambioContrasena { actual: string; nueva: string; confirmar: string }
/** HU-039. */
export function validarCambioContrasena(f: DatosCambioContrasena): Errores<DatosCambioContrasena> {
  return soloErrores({
    actual: reglas.requerido(f.actual) ? undefined : MENSAJES.requerido,
    nueva: !reglas.passwordFuerte(f.nueva) ? MENSAJES.passwordFuerte
      : f.nueva === f.actual ? 'La nueva contraseña debe ser distinta de la actual' : undefined,
    confirmar: f.confirmar === f.nueva ? undefined : MENSAJES.noCoinciden,
  })
}

export interface DatosRestablecer { codigo: string; nueva: string; confirmar: string }
/** HU-036: codigo de 6 digitos enviado por correo (RestablecerSolicitud usa \d{6}). */
export function validarRestablecer(f: DatosRestablecer): Errores<DatosRestablecer> {
  return soloErrores({
    codigo: /^\d{6}$/.test(f.codigo.trim()) ? undefined : 'El código tiene 6 dígitos',
    nueva: reglas.passwordFuerte(f.nueva) ? undefined : MENSAJES.passwordFuerte,
    confirmar: f.confirmar === f.nueva ? undefined : MENSAJES.noCoinciden,
  })
}

export interface DatosDireccion { calle: string; numero: string; comuna: string; region: string }
/** HU-023 y HU-044: misma regla en "Mis direcciones" y en el paso 1 del checkout. */
export function validarDireccion(d: DatosDireccion): Errores<DatosDireccion> {
  return soloErrores({
    calle: !reglas.requerido(d.calle) ? MENSAJES.requerido
      : !reglas.direccion(d.calle) || d.calle.trim().length < 3 ? MENSAJES.direccion
        : !reglas.maximo(d.calle, LIMITES.calle) ? MENSAJES.maximo(LIMITES.calle) : undefined,
    numero: !reglas.requerido(d.numero) ? MENSAJES.requerido
      : !reglas.maximo(d.numero, LIMITES.numeroDireccion) ? MENSAJES.maximo(LIMITES.numeroDireccion) : undefined,
    comuna: !reglas.requerido(d.comuna) ? MENSAJES.requerido
      : !reglas.soloLetras(d.comuna) ? MENSAJES.soloLetras
        : !reglas.maximo(d.comuna, LIMITES.comuna) ? MENSAJES.maximo(LIMITES.comuna) : undefined,
    region: reglas.requerido(d.region) ? undefined : MENSAJES.requerido,
  })
}
