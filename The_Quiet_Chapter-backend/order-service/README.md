# The Quiet Chapter — Order Service (DC-04)

**Spring Boot 4.1.1 · Java 17 · Spring Data JPA · MySQL de XAMPP**. Microservicio independiente para gestionar pedidos y su historial. Adaptado al diagrama **DC-04** (RegistroPedidos, Pedido, DetallePedido, DatosEntregaPedido, ImportesPedido, CriteriosPedido y EstadoPedido).

> **Importante:** este servicio **no procesa pagos**, no descuenta stock y no vacía el carrito automáticamente. El contrato `POST /api/pedidos` acepta una *fotografía de una compra cuyo pago ya fue confirmado*. **Está habilitado solo para pruebas en desarrollo local** hasta integrar la confirmación de pago simulado desde Checkout y la coordinación con Catalog/Cart. No declares compras reales con un estado `PAGADO` sin esa integración.

## 1. Carpeta e instalación

Extrae `order-service` junto a `catalog-service` (8080), `cart-service` (8081), `checkout-service` (8082). Este nuevo servicio escucha en **8083**.

```
The_Quiet_Chapter-backend/
├── catalog-service/
├── cart-service/
├── checkout-service/
└── order-service/
    ├── pom.xml
    ├── mvnw.cmd
    └── src/main/java/com/thequietchapter/
        ├── OrderServiceApplication.java
        ├── model/        # SOLO clases del UML DC-04
        ├── controller/   # REST y errores
        ├── dto/          # contratos JSON
        ├── repository/   # Spring Data JPA
        └── service/      # casos de uso
```

En XAMPP inicia **MySQL** (Apache solo se usa para phpMyAdmin) y crea una base nueva:

```sql
CREATE DATABASE IF NOT EXISTS tqc_pedidos_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configura `src/main/resources/application.properties` si tu servidor no usa `root`, contraseña vacía o puerto 3306. El dialecto `org.hibernate.dialect.MariaDBDialect` **ya está incluido** para evitar el fallo `FOR UPDATE OF` que experimentaste con Cart Service.

Desde `order-service` ejecuta:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Spring Data JPA/Hibernate crea las tablas `pedidos` y `detalles_pedido` automáticamente (`ddl-auto=update`, solo desarrollo). Las direcciones e importes se guardan dentro de la tabla de pedidos mediante `@Embeddable`; **no hace falta crear tablas duplicadas** para esas composiciones.

## 2. Lógica de negocio (DC-04)

- `Pedido` es el agregado principal. Se genera un `numeroPedido` estable a partir de su ID de MySQL: `TQC-00000001`, `TQC-00000002`, etc. Se completa dentro de la misma transacción; nunca reinicia con Spring Boot.
- Un pedido nace en estado `PAGADO` **solo cuando un adaptador de pago confirmado solicita su creación** (por ahora ensayo manual, desarrollo local).
- Flujo válido: `PAGADO` → `EN_PREPARACION` → `ENVIADO` → `ENTREGADO`.
- Cancelación permitida solo en `PAGADO` y `EN_PREPARACION`. No retrocede estados y no permite seguir avanzando uno cancelado.
- Seguimiento se registra cuando el pedido ya está `ENVIADO`.
- `DetallePedido` guarda *snapshot* de libro, título, cantidad y precio pagado. No consulta Catálogo para reescribir el historial si un libro cambia de precio o desaparece.
- `DatosEntregaPedido` guarda una dirección y método de envío históricos, no depende de las futuras direcciones de `user-service`.
- `ImportesPedido.esConsistente` exige que la suma de detalles coincida con el subtotal. El total es `subtotal + costoEnvio + impuestos - descuento`. No recalcula IVA ni aplica descuentos adicionales.
- `CriteriosPedido` comprueba el rango de fechas. El filtrado y paginación operan en MySQL mediante Spring Data JPA.
- `RegistroPedidos` conserva los métodos del UML y ensambla pedidos; el orden global y las consultas persistentes las realiza `PedidoRepository`. Su contador local no se usa como secuencia global para evitar duplicados tras reiniciar.
- Se utiliza un bloqueo pesimista JPA para avanzar estados y `@Version` para proteger cambios concurrentes. La `referenciaPago` es única para evitar crear dos pedidos con el mismo pago; las repeticiones idénticas devuelven el pedido existente.

## 3. Contratos HTTP (futuros componentes React/TypeScript)

| Método | Ruta | Propósito |
|---|---|---|
| `POST` | `/api/pedidos` | **Solo desarrollo:** crear pedido a partir de compra confirmada manualmente |
| `GET` | `/api/pedidos/mis-pedidos?pagina=0&tamano=10` | Historial paginado del cliente |
| `GET` | `/api/pedidos/mis-pedidos/filtrar?estado=ENVIADO&fechaDesde=2026-10-01&fechaHasta=2026-10-31` | Filtrar historial propio |
| `GET` | `/api/pedidos/{id}` | Consultar detalles de un pedido propio |
| `GET` | `/api/pedidos/numero/{numero}` | Consultar pedido propio por número |
| `GET` | `/api/pedidos?estado=PAGADO&pagina=0&tamano=10` | **Solo desarrollo:** listado administrativo |
| `PUT` | `/api/pedidos/{id}/estado` | **Solo desarrollo:** actualizar estado administrativo |
| `PUT` | `/api/pedidos/{id}/seguimiento` | **Solo desarrollo:** registrar seguimiento |
| `POST` | `/api/pedidos/{id}/cancelar` | Cancelar pedido del cliente antes del envío |

**Temporal:** para consultas propias envía la cabecera `X-TQC-Cliente-Id: 9001` y para crear incluye `clienteId` en el cuerpo. Estos identificadores **no son autenticación** y pueden falsificarse. Los endpoints de desarrollo se bloquean al configurar `TQC_ORDERS_DEV_ACTIONS=false`. La integración definitiva debe reemplazarlos por identidad autenticada y autorización interna verificable; no publiques este servicio con las operaciones de desarrollo habilitadas.

### 3.1 Crear pedido de prueba en Postman

**POST `http://localhost:8083/api/pedidos`** con header `Content-Type: application/json` y cuerpo:

```json
{
  "clienteId": 9001,
  "detalles": [
    {"libroId": 1, "tituloLibro": "Cien años de soledad", "cantidad": 5, "precioUnitario": 18900.00}
  ],
  "entrega": {
    "calle": "Los Alerces", "numero": "123", "comuna": "Puerto Montt",
    "region": "Los Lagos", "metodoEnvio": "Despacho a domicilio", "plazoEstimadoDias": 3
  },
  "importes": {"subtotal": 94500.00, "costoEnvio": 0, "impuestos": 0, "descuento": 0},
  "medioPago": "Pago simulado (prueba)",
  "referenciaPago": "DEMO-CHECKOUT-9001-0001"
}
```

Devuelve `201 Created` con `id`, `numeroPedido`, `estado=PAGADO`, `detalles`, `entrega`, `importes.total=94500.00`, `puedeCancelarse=true`. **Este POST no verifica un pago bancario ni el checkout real**. Cambia `referenciaPago` para otro pedido distinto; si la repites con exactamente los mismos datos, recuperarás el original sin duplicarlo. Si se modifica el cuerpo con la misma referencia, devuelve `409 Conflict`.

### 3.2 Consultar historial de cliente

**GET `http://localhost:8083/api/pedidos/mis-pedidos`** y **Headers**:

```
X-TQC-Cliente-Id: 9001
```

Debe devolver JSON con `pedidos`, `pagina`, `tamano`, `totalElementos` y `totalPaginas`. Prueba también `GET /api/pedidos/1` con la misma cabecera (sustituye 1 por el ID real). Si usas otra cabecera de cliente recibirás `404` para no mostrar pedidos ajenos en la prueba local.

### 3.3 Cambiar estados y registrar seguimiento

**PUT** `http://localhost:8083/api/pedidos/1/estado` (ID real), **Body → raw → JSON**:

```json
{"estado":"EN_PREPARACION"}
```

Luego envía `{"estado":"ENVIADO"}`. Una vez `ENVIADO`, **PUT** `/api/pedidos/1/seguimiento`:

```json
{"numeroSeguimiento":"TQC-TRACK-0001"}
```

Finalmente cambia el estado a `ENTREGADO`. Los intentos de saltar directamente de `PAGADO` a `ENTREGADO` retornarán `409 Conflict`.

Para probar cancelación, crea **otro pedido con una referencia diferente** y ejecuta **POST** `/api/pedidos/{id}/cancelar` con `X-TQC-Cliente-Id: 9001` antes de enviarlo. No permite cancelar un pedido enviado o entregado.

### 3.4 Verificar base de datos

En phpMyAdmin:

```sql
SELECT id, numero_pedido, cliente_id, estado, referencia_pago FROM tqc_pedidos_db.pedidos;
SELECT pedido_id, libro_id, titulo_libro, cantidad, precio_unitario FROM tqc_pedidos_db.detalles_pedido;
```

Reinicia Spring Boot y repite los GET. Los pedidos deben persistir y conservar los estados.

## 4. Integración posterior, obligatoria para la compra final

**Estado actual:** DC-01 Catalog y DC-02 Cart funcionan contra MySQL, DC-03 Checkout prepara resumen e intentos de pago. DC-04 Order ya guarda pedidos y estados, pero **aún no se ha conectado automáticamente** a esos servicios.

Para unirlos sin inventar un procesador bancario: implementaremos una **confirmación de pago simulado** dentro del dominio `Pago` de DC-03, autorizada por backend (no una petición arbitraria desde React). Solo después de confirmarse: validar nuevamente stock/precio en Catálogo, registrar transacción de compra con referencia única, crear pedido mediante una llamada interna a Order con el *snapshot* del checkout, descontar existencias y vaciar carrito. Hace falta manejar fallas parciales con idempotencia/compensación; no presentaremos estas operaciones como atómicas entre bases distintas.

**Crítico:** mientras no esté implementada esa coordinación, no podemos asegurar que registrar `PAGADO` equivalga a una compra final ni que el inventario se haya descontado. Cancelar tampoco repone stock automáticamente. La autenticación real llegará con User Service. Notificaciones usará eventos/contratos de pedidos más adelante. El API Gateway se incorporará al final, manteniendo las rutas REST públicas `/api/pedidos`.

## 5. Integración React mediante Vite (temporal)

Mientras no exista API Gateway, en `vite.config.ts` coloca los proxys específicos **antes** del genérico:

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
export default defineConfig({
  plugins: [react()],
  server: { proxy: {
    '/api/pedidos': {target:'http://127.0.0.1:8083', changeOrigin:true},
    '/api/checkout': {target:'http://127.0.0.1:8082', changeOrigin:true},
    '/api/carrito': {target:'http://127.0.0.1:8081', changeOrigin:true},
    '/api': {target:'http://127.0.0.1:8080', changeOrigin:true},
  }},
})
```

Usa `fetch('/api/pedidos/mis-pedidos')` con la identidad temporal en desarrollo; no uses `localStorage` ni consultas SQL desde React. Con User/Gateway se sustituirá la identidad temporal.

## 6. Pruebas

- `PedidoDominioTests`: importes, transición de estados, cancelación, seguimiento, filtros.
- `PedidoJpaIntegracionTests`: persistencia, numeración, creación idempotente, historial y actualización de estado con H2.
- Las pruebas reales de MySQL/XAMPP deben ejecutarse en tu equipo con `mvnw.cmd clean test` y Postman.

Esta base usa versiones Spring compatibles con los servicios que ya funcionan en tu equipo. **No se verificó una compilación Maven completa en el entorno de generación**; aquí se validó la compilación y comportamiento de las clases de dominio en Java. Confirma primero la compilación Maven y el funcionamiento contra XAMPP.
