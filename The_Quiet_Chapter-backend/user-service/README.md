# The Quiet Chapter — User Service (DC-05)

Servicio de Usuarios independiente (Spring Boot 4.1.1, Java 17, Spring Data JPA, XAMPP MySQL/MariaDB). **Puerto 8084**. Mantiene clases de negocio de DC-05: `Usuario`, `RolUsuario`, `LibretaDirecciones` y `Direccion`. `controller/`, `dto/`, `model/`, `repository/` y `service/` separados.

## Instalación

1. Colocar la carpeta `user-service` en `The_Quiet_Chapter-backend/` junto a los demás proyectos Maven.
2. En phpMyAdmin ejecutar:

```sql
CREATE DATABASE IF NOT EXISTS tqc_usuarios_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Iniciar MySQL en XAMPP. Comprobar `src/main/resources/application.properties` (usuario `root`, contraseña vacía y puerto 3306 son valores de desarrollo).
4. Terminal PowerShell dentro de `user-service`:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

5. Probar `http://localhost:8084/api/usuarios/registro` con POST. `GET` no es la ruta para crear cuentas.

**Importante:** configura una clave JWT segura en variable `TQC_JWT_SECRET` (mínimo 32 bytes UTF-8) al integrar/deplegar el sistema. El valor de respaldo en `application.properties` es **solo para pruebas locales**. El servidor está restringido a `127.0.0.1`; no exponerlo a Internet con esa configuración.

## Endpoints y ejemplos Postman

### Crear cuenta CLIENTE

**POST** `http://localhost:8084/api/usuarios/registro` — `Content-Type: application/json`

```json
{"nombre":"Ana","apellido":"Rojas","email":"ana.rojas@example.cl","telefono":"912345678","contrasena":"ClaveSegura123"}
```

Devuelve **201 Created** con `id`, `rol`, `nombre`, `apellido`, `email`, `telefono`, `fechaRegistro` y `direcciones`, **nunca** el hash de contraseña. No existe un parámetro `rol` en el registro público: todas las cuentas creadas así tienen rol CLIENTE.

### Iniciar sesión

**POST** `http://localhost:8084/api/usuarios/login`:

```json
{"email":"ana.rojas@example.cl","contrasena":"ClaveSegura123"}
```

Devuelve `accessToken`, `tokenType: Bearer`, `expiresIn` (segundos) y `usuario`. En las operaciones protegidas de Postman, pestaña **Authorization → Bearer Token**, pega **solo el valor de `accessToken`**. También puedes enviar el header `Authorization: Bearer <token>`.

### Perfil

- **GET** `/api/usuarios/me` (requiere token)
- **PUT** `/api/usuarios/me` (requiere token): `{"nombre":"Ana María","apellido":"Rojas","telefono":"912345678"}`
- **PUT** `/api/usuarios/me/email` (requiere token): `{"email":"ana.rojasm@example.cl"}`
- **PUT** `/api/usuarios/me/contrasena` (requiere token): `{"actual":"ClaveSegura123","nueva":"OtraClave123"}`. Respuesta 204.

### Libreta de direcciones

- **GET** `/api/usuarios/me/direcciones` (requiere token)
- **POST** `/api/usuarios/me/direcciones` (requiere token):

```json
{"calle":"Los Alerces","numero":"123","comuna":"Puerto Montt","region":"Los Lagos"}
```

- **PUT** `/api/usuarios/me/direcciones/{id}` (requiere token): mismo JSON.
- **PUT** `/api/usuarios/me/direcciones/{id}/predeterminada` (requiere token, sin cuerpo).
- **DELETE** `/api/usuarios/me/direcciones/{id}` (requiere token).

La primera dirección agregada será predeterminada. Al eliminarla, otra dirección existente pasa a ser predeterminada.

## Contrato para React y próximos microservicios

- `id` del usuario corresponde al `clienteId` que Cart, Checkout y Order almacenan como referencia (otros microservicios **no** comparten tablas con esta BD).
- `accessToken` es un JWT HS256 firmado y con expiración; el backend User Service verifica firma y vencimiento en rutas protegidas.
- La respuesta de login ofrece `usuario.id`, para reemplazar los `9001` de ensayos **después** de integrar autenticación en Cart, Checkout, Order y el API Gateway.
- **Todavía no es seguro pasar `usuario.id` desde React mediante `X-TQC-Cliente-Id` a los otros microservicios**: esas cabeceras siguen siendo simulaciones locales vulnerables a suplantación. Se corregirá durante la integración final con Gateway.
- `ADMINISTRADOR` está modelado, pero **no** puede seleccionarse desde el registro público. La provisión segura de administradores se integrará posteriormente.
- El historial de pedidos NO se duplica aquí: pertenece a `order-service`, donde se consulta con la identidad autenticada.
- No hay recuperación de contraseña por correo ni servicios de envío de notificaciones; esos casos se integrarán después con DC-08.
- Al ser tokens sin estado, cambiar contraseña no revoca inmediatamente tokens ya emitidos: requerirá revocación/versión de credenciales o sesiones en auditoría de seguridad.

## Pruebas

- Registro, login, perfil y JWT.
- Gestión de libreta de direcciones (JPA con base H2 aislada durante pruebas Maven).
- Correo único, contraseña BCrypt y protección de datos sensibles.
- `spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect` para evitar `FOR UPDATE OF alias` incompatibles con XAMPP.

**Pendiente de validación en tu PC:** descarga real de dependencias Maven, `clean test`, conexión a MySQL de XAMPP y flujo Postman. Este entorno no dispone del repositorio Maven para comprobarlos integralmente.
