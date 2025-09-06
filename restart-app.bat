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