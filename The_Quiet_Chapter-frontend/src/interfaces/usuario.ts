/** Contratos de user-service (DC-05). */
export type Rol = 'CLIENTE' | 'ADMINISTRADOR'

export interface Direccion {
  id: number
  calle: string
  numero: string
  comuna: string
  region: string
  predeterminada: boolean
}

export interface DireccionSolicitud {
  calle: string
  numero: string
  comuna: string
  region: string
}

export interface Perfil {
  id: number
  rol: Rol
  nombre: string
  apellido: string
  email: string
  telefono: string
  fechaRegistro: string
  direcciones: Direccion[]
}

export interface Sesion {
  accessToken: string
  tokenType: string
  expiresIn: number
  usuario: Perfil
}

export interface RegistroSolicitud {
  nombre: string
  apellido: string
  email: string
  telefono: string
  contrasena: string
}

export interface UsuarioBasico {
  id: number
  rol: Rol
  nombre: string
  apellido: string
  nombreCompleto: string
  email: string
  fechaRegistro: string
}
