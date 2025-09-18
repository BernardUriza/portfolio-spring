@echo off
REM Creado por Bernard Orozco
REM Fix para el error 500 en /api/admin/sync-config/status

echo Aplicando fix para sync-config...
set PGPASSWORD=ADMIN
"C:\Program Files\PostgreSQL\17\bin\psql" -U postgres -d portfolio_db -h localhost -p 5432 -f fix-sync-config.sql
echo Fix aplicado exitosamente!
pause