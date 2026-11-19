# The Quiet Chapter — Frontend React

Vite + React 19 + TypeScript (`@vitejs/plugin-react-swc` + ESLint, igual que los ejemplos del curso),
Bootstrap 5, React Router 7, componentes de función, `.module.css` por componente y `fetch` con `async/await`.
Los colores y estilos del sitio HTML original se conservan en `src/styles/tema.css`.

## Cómo ejecutar

1. **XAMPP**: Start MySQL (las bases ya creadas con `backend/docs/crear_bases_xampp.sql`).
2. **Backend**: levantar los 9 microservicios y luego el **api-gateway (8090)** (ver `backend/README.md`).
3. **Frontend** (Node 20+):
   ```powershell
   npm install
   npm run dev        # http://localhost:5173
   ```
   Vite reenvía todo `/api/**` al gateway (`vite.config.ts` → proxy a `http://localhost:8090`),
   así que React nunca habla directo con un microservicio.

Otros comandos: `npm run build` (tsc + build de producción), `npm run lint` (ESLint), `npm run preview`.

**Cuentas**: administrador `admin@thequietchapter.cl` / `Admin#2026!`. Los clientes se registran en `/registro`.

## Estructura

```
src/
  api/            Un módulo por microservicio (catalogo, carrito, checkout, pedidos, usuarios,
                  social, promo, notif, reportes) + http.ts (fetch, JWT, errores)
  interfaces/     Tipos TypeScript que reflejan los DTO del backend
  context/        Sesión (auth), carrito y favoritos globales (contexto + Provider separados)
  hooks/          useAccion (mensajes de éxito/error de una operación) y useFormulario (valores + errores)
  components/
    comunes/      Piezas genéricas: Campo/CampoSeleccion/CampoTextoLargo, Avisos, Alerta, Modal, Confirmar,
                  Pestanas, OpcionesRadio, TablaDatos, TarjetaKpi, Paginación, Estrellas, Gráfico de barras
    cuenta/       Formularios de Mi cuenta (datos personales, correo, contraseña, campos de dirección)
    shopping/     Tarjeta de libro, grilla, precio, portada, botón agregar, ítem y resumen del carrito, cupón
    social/       Sección de reseñas, tarjeta de reseña y formulario de texto (reseña / reporte)
    pedidos/      Línea de estados y resumen de importes (checkout, confirmación y detalle)
    admin/        Editores del administrador: libro, precio, stock, biografía, cupón, promoción,
                  método de envío, medio de pago, boletín, gestión de pedido y moderación
    layout/       Header, Footer (boletín), Layout, menús de cuenta y admin, RutaProtegida
  pages/          Una página por cada .html original (+ las HU que no tenían página); solo componen
  utils/
    validaciones/ Reglas y límites del backend por dominio (cuenta, catalogo, promo, social, admin)
    formato.ts    CLP, fechas y etiquetas · direccion.ts · fechas.ts · navegacion.ts · regiones
  styles/         tema.css (diseño del sitio HTML) + ajustes.css (correcciones y utilidades)
```

### Cómo está armado un formulario

1. **Validación** en `utils/validaciones/<dominio>.ts`: una función pura que recibe los datos y devuelve
   `{ campo: 'mensaje' }` (objeto vacío = válido). Los límites (`LIMITES`) copian los `@Size` del backend.
2. **Estado** con `useFormulario(inicial)`: `form.campo('email')` entrega `value`, `error` y `onChange`
   listos para `<Campo>`; `form.validar(validarRegistro)` marca los errores y devuelve si se puede enviar.
3. **Envío** con `useAccion()`: `accion.ejecutar(() => api.algo(), 'Mensaje OK', recargar)` limpia los avisos,
   llama al backend y muestra el éxito o el error en `<Avisos estado={accion} />`.

El frontend valida para dar una buena experiencia; **el backend vuelve a validar todo** (DTO con `@Valid`).

## Páginas HTML → rutas React

| HTML original | Ruta | Acceso |
|---|---|---|
| index.html | `/` | público |
| catalogo.html | `/catalogo` (filtros en la URL) | público |
| detalleLibro.html | `/libros/:id` | público |
| — (HU-009) | `/autores/:id` | público |
| ofertas.html | `/ofertas` | público |
| carrito.html | `/carrito` | público (comprar exige sesión) |
| login.html / registro.html | `/login`, `/registro` | público |
| — (HU-036) | `/recuperar` | público |
| checkoutDireccion/Envio/Pago/Resumen.html | `/checkout/direccion` … `/checkout/resumen` | cliente |
| confirmacion.html | `/confirmacion?numero=` | cliente |
| favoritos.html | `/favoritos` | cliente |
| miPerfil.html | `/cuenta/perfil` | con sesión |
| misPedidos.html | `/cuenta/pedidos`, `/cuenta/pedidos/:id` | cliente |
| misDirecciones.html | `/cuenta/direcciones` | cliente |
| — | `/cuenta/resenas`, `/cuenta/notificaciones` | cliente |
| — (enlaces del correo) | `/boletin/confirmar`, `/boletin/cancelar` | público |
| admin.html | `/admin` | administrador |
| adminCatalogo.html | `/admin/catalogo` | administrador |
| adminPedidos.html | `/admin/pedidos` | administrador |
| adminDashboard.html | `/admin/dashboard` | administrador |
| — | `/admin/promociones`, `/admin/resenas`, `/admin/boletines`, `/admin/opciones`, `/admin/correos` | administrador |

## Notas

- La sesión (JWT de user-service) se guarda en `localStorage`; si el token expira, cualquier 401 cierra la sesión.
- El carrito de visitante vive en la cookie `tqc_cart_session` (HttpOnly) y se fusiona con el de la cuenta al iniciar sesión (HU-019).
- Las guardas de ruta son solo de navegación: cada microservicio vuelve a validar el token y el rol.
- Los correos son simulados: se ven en *Mi cuenta → Mis Correos* y, para el admin, en *Correos enviados*.
