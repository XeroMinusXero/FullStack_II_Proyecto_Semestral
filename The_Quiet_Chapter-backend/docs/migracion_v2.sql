-- Migracion v2 (ejecutar UNA vez en phpMyAdmin si la BD ya existia antes de la v2).
-- Columnas nuevas de user-service para HU-033 (recuperar contrasena). Seguro de repetir (IF NOT EXISTS, MariaDB/XAMPP).
ALTER TABLE tqc_usuarios_db.usuarios
  ADD COLUMN IF NOT EXISTS codigo_recuperacion_hash VARCHAR(100) NULL,
  ADD COLUMN IF NOT EXISTS codigo_recuperacion_expira DATETIME(6) NULL;
