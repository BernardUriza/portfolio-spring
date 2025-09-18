-- Creado por Bernard Orozco
-- Fix para el error 500 en /api/admin/sync-config/status
-- Verifica estructura e inserta configuración inicial

-- Primero verificamos si la tabla existe y su estructura
\d sync_config

-- Insertar registro inicial si no existe
INSERT INTO sync_config (
    singleton_key,
    enabled,
    interval_hours,
    updated_by,
    updated_at
)
SELECT
    'X',
    false,
    6,
    'system',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM sync_config WHERE singleton_key = 'X'
);

-- Verificar que se insertó
SELECT * FROM sync_config;