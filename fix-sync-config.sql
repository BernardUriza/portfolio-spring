-- Creado por Bernard Orozco
-- Fix para el error 500 en /api/admin/sync-config/status
-- Inserta la configuración inicial del sincronizador si no existe

INSERT INTO sync_config (singleton_key, enabled, interval_hours, updated_by, updated_at)
VALUES ('X', false, 6, 'system', CURRENT_TIMESTAMP)
ON CONFLICT (singleton_key) DO NOTHING;