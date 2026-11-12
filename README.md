# The Quiet Chapter

Librería en línea construida con **arquitectura de microservicios** (Spring Boot) y un **frontend en React**,
desarrollada para la asignatura de Desarrollo Fullstack.

```
React (Vite, 5173) ──/api──► API Gateway (8090) ──► 9 microservicios ──► 1 base MySQL por servicio (XAMPP)
```

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| `The_Quiet_Chapter-backend/` | API Gateway + 9 microservicios Spring Boot 4.1.1 (Java 17), scripts SQL y colección Postman en `docs/` |
| `The_Quiet_Chapter-frontend/` | Vite + React 19 + TypeScript, Bootstrap 5, React Router |
| `Documentos/` | Diagramas UML y plantilla de requerimientos (HU) |

## Microservicios

| Servicio | Puerto | Base de datos |
|---|---|---|
| catalog-service | 8080 | tqc_catalogo_db |
| cart-service | 8081 | tqc_carrito_db |
| checkout-service | 8082 | tqc_checkout_db |
| order-service | 8083 | tqc_pedidos_db |
| user-service | 8084 | tqc_usuarios_db |
| social-service | 8085 | tqc_social_db |
| promo-service | 8086 | tqc_promo_db |
| notif-service | 8087 | tqc_notif_db |
| report-service | 8088 | — (consolida datos de los demás) |
| **api-gateway** | **8090** | — |

El frontend solo habla con el **API Gateway**. Las llamadas entre servicios usan rutas `/internal/**`
protegidas con una clave interna; el gateway no las expone hacia afuera.

## Cómo ejecutar

1. **XAMPP**: iniciar MySQL y ejecutar `The_Quiet_Chapter-backend/docs/crear_bases_xampp.sql`.
2. **Backend**: levantar los 9 servicios y luego el gateway (detalle en `The_Quiet_Chapter-backend/README.md`).
3. **Frontend**:
   ```bash
   cd The_Quiet_Chapter-frontend
   npm install
   npm run dev      # http://localhost:5173
   ```

Cuenta de administrador de prueba: `admin@thequietchapter.cl` / `Admin#2026!`

> Los secretos JWT y la clave interna incluidos en `application.properties` son **valores de desarrollo**
> para uso académico local; en un entorno real se definen por variables de entorno.
