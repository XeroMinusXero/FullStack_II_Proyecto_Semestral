/**
 * Reglas base de validacion, portadas desde js/script.js del sitio HTML (mismos mensajes), y los
 * limites que exige el backend (anotaciones @Size/@Min de los DTO de cada microservicio).
 * Validar en el frontend es para la experiencia del usuario: el backend vuelve a validar todo.
 */
export const reglas = {
  requerido: (v: string) => v.trim().length > 0,
  email: (v: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim()),
  telefono: (v: string) => /^[+]?[\d\s\-()]{8,15}$/.test(v.trim()),
  soloNumeros: (v: string) => /^\d+$/.test(v.trim()),
  passwordFuerte: (v: string) => v.length >= 8 && v.length <= 72 && /[^A-Za-z0-9\s]/.test(v),
  soloLetras: (v: string) => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ\s'-]+$/.test(v.trim()),
  direccion: (v: string) => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ0-9\s.,#'\-/]+$/.test(v.trim()),
  isbn: (v: string) => /^[\dXx-]{10,17}$/.test(v.trim()),
  /** Entero (sin decimales) mayor o igual a `minimo`. */
  entero: (v: string | number, minimo = 0) => v !== '' && Number.isInteger(Number(v)) && Number(v) >= minimo,
  /** Numero (admite decimales) mayor o igual a `minimo`. */
  numero: (v: string | number, minimo = 0) => v !== '' && Number.isFinite(Number(v)) && Number(v) >= minimo,
  maximo: (v: string, max: number) => v.trim().length <= max,
}

/** Errores por campo: { nombreDelCampo: 'mensaje' }. Un objeto vacio significa "formulario valido". */
export type Errores<T> = Partial<Record<keyof T, string>>

export const sinErrores = (e: object) => Object.keys(e).length === 0

/** Quita las claves sin mensaje: { a: 'x', b: undefined } -> { a: 'x' }. */
export const soloErrores = <T extends object>(e: T): T =>
  Object.fromEntries(Object.entries(e).filter(([, v]) => v !== undefined)) as T

export const MENSAJES = {
  requerido: 'Este campo es obligatorio',
  email: 'Ingrese un correo válido',
  telefono: 'Ingrese un teléfono válido',
  soloNumeros: 'Solo números',
  passwordFuerte: 'Mínimo 8 caracteres y un carácter especial',
  soloLetras: 'Solo se permiten letras, espacios y guiones',
  direccion: 'Dirección inválida',
  noCoinciden: 'Las contraseñas no coinciden',
  maximo: (n: number) => `Máximo ${n} caracteres`,
  minimo: (n: number) => `Mínimo ${n} caracteres`,
}

/** Limites del backend (DTO y modelos de dominio). Si cambian alla, se cambian aqui. */
export const LIMITES = {
  nombrePersona: 100,       // user-service RegistroSolicitud
  email: 180,
  numeroDireccion: 25,      // checkout-service DireccionEnvioSolicitud (el mas estricto de los dos servicios)
  calle: 160,
  comuna: 100,
  resenaMin: 10,            // social-service Resena.MIN
  resenaMax: 2000,          // social-service Resena.MAX
  motivoReporteMin: 5,
  motivoReporteMax: 500,
  codigoCupon: /^[A-Z0-9_-]{3,40}$/, // promo-service Cupon.actualizarCodigo
  nombrePromocion: 120,
  nombreOpcion: 120,        // checkout-service MetodoEnvio / MedioPago
  asuntoBoletin: 200,
  contenidoBoletinMin: 10,
  seguimientoMin: 4,
  seguimientoMax: 120,      // order-service SeguimientoSolicitud
  motivoAjusteMin: 3,
  motivoAjusteMax: 400,     // catalog-service AjusteStockSolicitud
  biografia: 5000,
}
