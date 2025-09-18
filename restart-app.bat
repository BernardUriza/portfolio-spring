@echo off
echo 🔄 Restarting Portfolio Application...

REM Kill processes on port 8080 (Backend)
echo Killing processes on port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080 "') do (
    if not "%%a"=="0" (
        echo Killing process %%a on port 8080
        taskkill /PID %%a /F >nul 2>&1
    )
)

REM Kill processes on port 4200 (Frontend) 
echo Killing processes on port 4200...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":4200 "') do (
    if not "%%a"=="0" (
        echo Killing process %%a on port 4200
        taskkill /PID %%a /F >nul 2>&1
    )
)

echo Ports cleared!
timeout /t 2 /nobreak >nul

REM Initialize sync_config if needed (Creado por Bernard Orozco)
echo Checking sync_config table...
set PGPASSWORD=ADMIN
"C:\Program Files\PostgreSQL\17\bin\psql" -U postgres -d portfolio_db -h localhost -p 5432 -t -c "SELECT COUNT(*) FROM sync_config WHERE singleton_key = 'X';" 2>nul | findstr /r "^[ ]*0[ ]*$" >nul
if %errorlevel%==0 (
    echo Initializing sync_config...
    "C:\Program Files\PostgreSQL\17\bin\psql" -U postgres -d portfolio_db -h localhost -p 5432 -c "INSERT INTO sync_config (singleton_key, enabled, interval_hours, updated_by, updated_at) VALUES ('X', false, 6, 'system', CURRENT_TIMESTAMP);" >nul 2>&1
    echo Sync config initialized!
)

REM Start Backend
echo Starting Backend (Spring Boot)...
cd /d "C:\Users\Bernard\Documents\GitHub\portfolio-backend"
start "Portfolio Backend" cmd /k "mvnw.cmd spring-boot:run"

REM Wait for backend to start
echo Waiting for backend to start...
timeout /t 10 /nobreak >nul

REM Start Frontend
echo Starting Frontend (Angular)...
cd /d "C:\Users\Bernard\Documents\GitHub\portfolio-frontend"

REM Check if node_modules exists
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
)

start "Portfolio Frontend" cmd /k "npm start"

echo.
echo ✅ Application started successfully!
echo Backend: http://localhost:8080
echo Frontend: http://localhost:4200
echo.
echo Press any key to exit...
pause >nul