@echo off
REM Script batch para crear la base de datos portfolio_db en PostgreSQL
REM Creado por Bernard Orozco
REM Versión de PostgreSQL: 17.6

echo ========================================
echo  Creando base de datos portfolio_db
echo ========================================
echo.

REM Configuración
set PGUSER=postgres
set PGPASSWORD=ADMIN
set PGHOST=localhost
set PGPORT=5432
set PSQL_PATH="C:\Program Files\PostgreSQL\17\bin\psql"

echo Verificando conexión a PostgreSQL...
%PSQL_PATH% -U %PGUSER% -h %PGHOST% -p %PGPORT% -c "SELECT version();" > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se pudo conectar a PostgreSQL.
    echo Verifica que PostgreSQL esté ejecutándose y que las credenciales sean correctas.
    pause
    exit /b 1
)

echo Conexión exitosa a PostgreSQL.
echo.

echo Creando base de datos portfolio_db...
%PSQL_PATH% -U %PGUSER% -h %PGHOST% -p %PGPORT% -c "CREATE DATABASE portfolio_db WITH OWNER = postgres ENCODING = 'UTF8';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  Base de datos creada exitosamente!
    echo ========================================
    echo.
    echo Configuración completada:
    echo - Database: portfolio_db
    echo - Host: localhost
    echo - Port: 5432
    echo - User: postgres
    echo.
    echo Spring Boot está configurado para conectarse automáticamente
    echo y crear las tablas necesarias con ddl-auto=update
) else (
    echo.
    echo NOTA: Si la base de datos ya existe, este mensaje es normal.
    echo Para recrearla, primero elimínala con:
    echo %PSQL_PATH% -U postgres -c "DROP DATABASE IF EXISTS portfolio_db;"
)

echo.
pause