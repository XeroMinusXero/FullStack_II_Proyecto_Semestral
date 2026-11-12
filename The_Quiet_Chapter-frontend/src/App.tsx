import { lazy, Suspense, type ReactNode } from 'react'
import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { RutaProtegida } from './components/layout/RutaProtegida'
import { Cargando } from './components/comunes/Cargando'
import Inicio from './pages/Inicio'
import Catalogo from './pages/Catalogo'
import DetalleLibro from './pages/DetalleLibro'

// Las secciones menos visitadas se cargan bajo demanda para aligerar la primera carga.
const Autor = lazy(() => import('./pages/Autor'))
const Ofertas = lazy(() => import('./pages/Ofertas'))
const Carrito = lazy(() => import('./pages/Carrito'))
const Favoritos = lazy(() => import('./pages/Favoritos'))
const Login = lazy(() => import('./pages/Login'))
const Registro = lazy(() => import('./pages/Registro'))
const Recuperar = lazy(() => import('./pages/Recuperar'))
const Confirmacion = lazy(() => import('./pages/Confirmacion'))
const NoEncontrado = lazy(() => import('./pages/NoEncontrado'))
const Boletin = lazy(() => import('./pages/boletin/Boletin'))
const CheckoutDireccion = lazy(() => import('./pages/checkout/CheckoutDireccion'))
const CheckoutEnvio = lazy(() => import('./pages/checkout/CheckoutEnvio'))
const CheckoutPago = lazy(() => import('./pages/checkout/CheckoutPago'))
const CheckoutResumen = lazy(() => import('./pages/checkout/CheckoutResumen'))
const Perfil = lazy(() => import('./pages/cuenta/Perfil'))
const Pedidos = lazy(() => import('./pages/cuenta/Pedidos'))
const DetallePedido = lazy(() => import('./pages/cuenta/DetallePedido'))
const Direcciones = lazy(() => import('./pages/cuenta/Direcciones'))
const MisResenas = lazy(() => import('./pages/cuenta/Resenas'))
const Notificaciones = lazy(() => import('./pages/cuenta/Notificaciones'))
const AdminPanel = lazy(() => import('./pages/admin/Panel'))
const AdminCatalogo = lazy(() => import('./pages/admin/Catalogo'))
const AdminPedidos = lazy(() => import('./pages/admin/Pedidos'))
const AdminDashboard = lazy(() => import('./pages/admin/Dashboard'))
const AdminPromociones = lazy(() => import('./pages/admin/Promociones'))
const AdminResenas = lazy(() => import('./pages/admin/Resenas'))
const AdminBoletines = lazy(() => import('./pages/admin/Boletines'))
const AdminOpciones = lazy(() => import('./pages/admin/Opciones'))
const AdminCorreos = lazy(() => import('./pages/admin/Correos'))

const cliente = (el: ReactNode) => <RutaProtegida rol="CLIENTE">{el}</RutaProtegida>
const sesion = (el: ReactNode) => <RutaProtegida>{el}</RutaProtegida>
const admin = (el: ReactNode) => <RutaProtegida rol="ADMINISTRADOR">{el}</RutaProtegida>

/** Mapa de paginas: cada .html del sitio original es una ruta (ver README). */
export default function App() {
  return (
    <Suspense fallback={<Cargando />}>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Inicio />} />
          <Route path="catalogo" element={<Catalogo />} />
          <Route path="libros/:id" element={<DetalleLibro />} />
          <Route path="autores/:id" element={<Autor />} />
          <Route path="ofertas" element={<Ofertas />} />
          <Route path="carrito" element={<Carrito />} />
          <Route path="login" element={<Login />} />
          <Route path="registro" element={<Registro />} />
          <Route path="recuperar" element={<Recuperar />} />
          <Route path="boletin/confirmar" element={<Boletin accion="confirmar" />} />
          <Route path="boletin/cancelar" element={<Boletin accion="cancelar" />} />

          <Route path="favoritos" element={cliente(<Favoritos />)} />
          <Route path="checkout/direccion" element={cliente(<CheckoutDireccion />)} />
          <Route path="checkout/envio" element={cliente(<CheckoutEnvio />)} />
          <Route path="checkout/pago" element={cliente(<CheckoutPago />)} />
          <Route path="checkout/resumen" element={cliente(<CheckoutResumen />)} />
          <Route path="confirmacion" element={cliente(<Confirmacion />)} />
          <Route path="cuenta/perfil" element={sesion(<Perfil />)} />
          <Route path="cuenta/pedidos" element={cliente(<Pedidos />)} />
          <Route path="cuenta/pedidos/:id" element={cliente(<DetallePedido />)} />
          <Route path="cuenta/direcciones" element={cliente(<Direcciones />)} />
          <Route path="cuenta/resenas" element={cliente(<MisResenas />)} />
          <Route path="cuenta/notificaciones" element={cliente(<Notificaciones />)} />

          <Route path="admin" element={admin(<AdminPanel />)} />
          <Route path="admin/catalogo" element={admin(<AdminCatalogo />)} />
          <Route path="admin/pedidos" element={admin(<AdminPedidos />)} />
          <Route path="admin/dashboard" element={admin(<AdminDashboard />)} />
          <Route path="admin/promociones" element={admin(<AdminPromociones />)} />
          <Route path="admin/resenas" element={admin(<AdminResenas />)} />
          <Route path="admin/boletines" element={admin(<AdminBoletines />)} />
          <Route path="admin/opciones" element={admin(<AdminOpciones />)} />
          <Route path="admin/correos" element={admin(<AdminCorreos />)} />

          <Route path="*" element={<NoEncontrado />} />
        </Route>
      </Routes>
    </Suspense>
  )
}
