-- The Quiet Chapter: una base de datos POR microservicio (XAMPP MySQL/MariaDB).
-- Ejecutar una vez en phpMyAdmin (pestaña SQL). Las tablas las crea cada servicio al arrancar (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS tqc_catalogo_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_carrito_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_checkout_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_pedidos_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_usuarios_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_social_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_promo_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tqc_notif_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- report-service no tiene base propia: calcula los reportes consultando a order, catalog y user.

-- OPCIONAL (solo si antes ejecutaste OPCIONES_DESARROLLO.sql): desactiva las opciones de prueba antiguas.
-- checkout-service crea opciones reales SOLO cuando las tablas estan vacias; tambien puedes editarlas desde el panel admin.
-- UPDATE tqc_checkout_db.checkout_metodos_envio SET activo = 0 WHERE nombre LIKE 'Despacho de prueba%';
-- UPDATE tqc_checkout_db.checkout_medios_pago  SET activo = 0 WHERE nombre LIKE 'Integración pendiente%';
