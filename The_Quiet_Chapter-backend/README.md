# The Quiet Chapter — Backend de microservicios

Spring Boot 4.1.1 · Java 17 · Spring Data JPA · MySQL de XAMPP · Spring Cloud Gateway Server WebMVC.
Cada microservicio tiene **su propia base de datos** y se comunica con los demás **solo por HTTP**.

| Servicio | Puerto | Base de datos | Diagrama | Rutas públicas (vía gateway) |
|---|---|---|---|---|
| api-gateway | **8090** | — | — | punto de entrada único para React |
| catalog-service | 8080 | tqc_catalogo_db | DC-01 | `/api/libros/**` |
| cart-service | 8081 | tqc_carrito_db | DC-02 | `/api/carrito/**` |
| checkout-service | 8082 | tqc_checkout_db | DC-03 | `/api/checkout/**` |
| order-service | 8083 | tqc_pedidos_db | DC-04 | `/api/pedidos/**` |
| user-service | 8084 | tqc_usuarios_db | DC-05 | `/api/usuarios/**` |
| social-service | 8085 | tqc_social_db | DC-06 | `/api/favoritos/**`, `/api/resenas/**` |
| promo-service | 8086 | tqc_promo_db | DC-07 | `/api/cupones/**`, `/api/promociones/**`, `/api/recomendaciones/**` |
| notif-service | 8087 | tqc_notif_db | DC-08 | `/api/notificaciones/**`, `/api/boletines/**` |
| report-service | 8088 | — | DC-09 | `/api/reportes/**` |

## Seguridad

- **Login** en user-service → JWT HS256 (`accessToken`, 2 h) con `sub` = id del usuario y claim `rol` (`CLIENTE` | `ADMINISTRADOR`).
- **Cada microservicio valida el mismo JWT** (`security/Autenticacion.java`) con el secreto compartido `TQC_JWT_SECRET`.
  La identidad sale SIEMPRE del token: ya no existe la cabecera `X-TQC-Cliente-Id`.
- Endpoints de administración responden **401** sin sesión y **403** con rol CLIENTE.
- **Administrador inicial**: user-service lo crea al primer arranque si no existe ninguno.
  Por defecto `admin@thequietchapter.cl` / `Admin#2026!` (cámbialo con `TQC_ADMIN_EMAIL` y `TQC_ADMIN_PASSWORD`).
- Llamadas servicio-a-servicio usan rutas `/internal/**` con la cabecera `X-TQC-Internal-Key` (`TQC_INTERNAL_KEY`).
  **El gateway no enruta `/internal/**`**, así que React nunca puede llamarlas.

Los valores por defecto de `TQC_JWT_SECRET` y `TQC_INTERNAL_KEY` ya son iguales en todos los `application.properties`:
en desarrollo no necesitas definir nada.

## Arranque (orden recomendado)

1. XAMPP → **Start MySQL**. En phpMyAdmin ejecuta `docs/crear_bases_xampp.sql`.
2. En una terminal por servicio (PowerShell, dentro de cada carpeta):
   ```powershell
   .\mvnw.cmd clean test        # compila y corre pruebas (H2 en memoria, no toca XAMPP)
   .\mvnw.cmd spring-boot:run
   ```
   Orden: `user-service` → `catalog-service` → `cart-service` → `order-service` → `checkout-service`
   → (`social`, `promo`, `notif`, `report`) → **`api-gateway` al final**.
3. Prueba todo contra `http://localhost:8090`: con Swagger (`http://localhost:8090/swagger-ui.html`) o con la colección `docs/TQC.postman_collection.json`.

## Documentación de la API (Swagger / OpenAPI)

Cada microservicio publica su documentación con **springdoc-openapi 3.2.0** (la línea 3.x es la de Spring Boot 4).

| Qué | Dónde |
|---|---|
| **Swagger centralizado (los 9 servicios en un selector)** | `http://localhost:8090/swagger-ui.html` |
| Swagger de un servicio | `http://127.0.0.1:<puerto>/swagger-ui.html` (por ejemplo `8080` para catálogo) |
| Especificación OpenAPI en JSON | `http://127.0.0.1:<puerto>/v3/api-docs` |

- El gateway pide cada especificación por la ruta `/docs/<servicio>/v3/api-docs` y quita ese prefijo (`StripPrefix=2`).
  Desde la UI del gateway, **"Try it out" pasa por el gateway**, igual que el frontend.
- Las rutas `/internal/**` **no se documentan** (`springdoc.paths-to-exclude`): son solo servicio-a-servicio.
- Cada endpoint indica quién puede usarlo: `[Público]`, `[Visitante o cliente]`, `[Cliente]`, `[Con sesión]` o `[Admin]`.

**Probar una ruta protegida:**
1. En *Usuarios y autenticación*, ejecuta `POST /api/usuarios/login` (por ejemplo con el administrador) y copia `accessToken`.
2. Pulsa **Authorize**, pega solo el token (sin la palabra `Bearer`) y confirma.
3. El token queda guardado al cambiar de servicio en el selector y al recargar la página (`persist-authorization`).

Archivos: `config/OpenApiConfig.java` en cada servicio (título, esquema JWT y un `OperationCustomizer` que reemplaza la
cabecera `Authorization` manual por el botón Authorize), `@Operation` en cada endpoint de los controladores y la sección
"Swagger" de cada `application.properties`.

## Flujo de compra (HU-028)

`POST /api/checkout/{id}/pagar` hace, en orden:
1. registra el intento de pago (PENDIENTE) → 2. descuenta stock en catalog (todas las líneas o ninguna)
→ 3. crea el pedido en order → 4. aprueba el pago y finaliza el checkout → 5. vacía el carrito y registra el canje del cupón.
Si el paso 2 o 3 falla, el pago queda **RECHAZADO** con el motivo, el stock se repone y el carrito queda intacto.
`{"simularRechazo": true}` permite demostrar un pago rechazado.

Cancelar un pedido (cliente antes del envío, o administrador) **repone el stock** en catalog; si catalog no responde, la cancelación se revierte.

## Servicios de dominio

- **social-service (DC-06):** favoritos, reseñas (solo con compra verificada en order-service), calificaciones 1-5, votos de utilidad, reportes y moderación del administrador.
- **promo-service (DC-07):** cupones (validados por cart, canje registrado por checkout), promociones por porcentaje o monto fijo con alcance (libro, categoría, género, editorial) que el carrito aplica al precio, ofertas temporales y recomendaciones (relacionados, por compras, por categoría, complementarios).
- **notif-service (DC-08):** **correo simulado**: cada envío queda en la tabla `notificaciones` (bandeja admin: `GET /api/notificaciones`). Bienvenida, cambios de cuenta, código de recuperación, compra, pago, estados del pedido, avisos de reabastecimiento y boletín con doble confirmación.
- **report-service (DC-09):** sin base de datos propia; consulta order, catalog y user. Panel de KPIs, ventas, libros, inventario, clientes y exportación CSV (`;`, UTF-8 con BOM para Excel).

## IVA

Los precios del catálogo **incluyen IVA (19%)**. Checkout y pedidos informan `ivaIncluido` = total × 19/119; `impuestos` = impuestos adicionales (0).
