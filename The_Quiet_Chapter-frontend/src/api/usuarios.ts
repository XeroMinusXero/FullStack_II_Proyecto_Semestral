import { peticion } from './http'
import type { Direccion, DireccionSolicitud, Perfil, RegistroSolicitud, Sesion, UsuarioBasico } from '../interfaces/usuario'

const BASE = '/api/usuarios'

export const usuariosApi = {
  registrar: (s: RegistroSolicitud) => peticion<Perfil>(`${BASE}/registro`, { metodo: 'POST', cuerpo: s }),
  login: (email: string, contrasena: string) => peticion<Sesion>(`${BASE}/login`, { metodo: 'POST', cuerpo: { email, contrasena } }),
  perfil: () => peticion<Perfil>(`${BASE}/me`),
  actualizarPerfil: (nombre: string, apellido: string, telefono: string) =>
    peticion<Perfil>(`${BASE}/me`, { metodo: 'PUT', cuerpo: { nombre, apellido, telefono } }),
  actualizarCorreo: (email: string) => peticion<Perfil>(`${BASE}/me/email`, { metodo: 'PUT', cuerpo: { email } }),
  cambiarContrasena: (actual: string, nueva: string) => peticion<void>(`${BASE}/me/contrasena`, { metodo: 'PUT', cuerpo: { actual, nueva } }),
  direcciones: () => peticion<Direccion[]>(`${BASE}/me/direcciones`),
  agregarDireccion: (d: DireccionSolicitud) => peticion<Direccion>(`${BASE}/me/direcciones`, { metodo: 'POST', cuerpo: d }),
  editarDireccion: (id: number, d: DireccionSolicitud) => peticion<Direccion>(`${BASE}/me/direcciones/${id}`, { metodo: 'PUT', cuerpo: d }),
  eliminarDireccion: (id: number) => peticion<void>(`${BASE}/me/direcciones/${id}`, { metodo: 'DELETE' }),
  predeterminada: (id: number) => peticion<Direccion>(`${BASE}/me/direcciones/${id}/predeterminada`, { metodo: 'PUT' }),
  solicitarRecuperacion: (email: string) => peticion<{ mensaje: string }>(`${BASE}/recuperacion`, { metodo: 'POST', cuerpo: { email } }),
  restablecer: (email: string, codigo: string, nueva: string) =>
    peticion<void>(`${BASE}/recuperacion/restablecer`, { metodo: 'POST', cuerpo: { email, codigo, nueva } }),
  clientes: (pagina = 0, tamano = 20) =>
    peticion<{ usuarios: UsuarioBasico[]; pagina: number; tamano: number; totalElementos: number; totalPaginas: number }>(BASE, { consulta: { pagina, tamano } }),
}
