-- Script SQL para crear la base de datos portfolio_db en PostgreSQL
-- Creado por Bernard Orozco
-- Versión de PostgreSQL: 17.6

-- Ejecutar este script con el usuario postgres:
-- "C:\Program Files\PostgreSQL\17\bin\psql" -U postgres -f create-database.sql

-- Eliminar la base de datos si existe (opcional, usar con precaución)
-- DROP DATABASE IF EXISTS portfolio_db;

-- Crear la base de datos portfolio_db
CREATE DATABASE portfolio_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    CONNECTION LIMIT = -1
    TEMPLATE = template0;

-- Conectar a la base de datos recién creada
\c portfolio_db;

-- Mostrar información de la conexión
SELECT current_database(), current_user, version();

-- Mensaje de confirmación
\echo 'Base de datos portfolio_db creada exitosamente!'
\echo 'Spring Boot creará las tablas automáticamente con spring.jpa.hibernate.ddl-auto=update'