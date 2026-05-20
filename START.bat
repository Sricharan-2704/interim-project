@echo off
REM Sponsorship Hub - Quick Start Script
REM This script helps you get the application running quickly

echo.
echo ===============================================
echo    Sponsorship Hub - Quick Start
echo ===============================================
echo.

REM Check MySQL
echo Checking MySQL Service...
for /f "tokens=3" %%A in ('sc query MySQL80 ^| find "STATE"') do set "MySQL_State=%%A"
if "%MySQL_State%"=="RUNNING" (
    echo [OK] MySQL Service is running
) else (
    echo [WARNING] MySQL Service is not running
    echo Please ensure MySQL service is started before proceeding
    pause
)

REM Build Backend
echo.
echo ===============================================
echo [1/4] Building Backend...
echo ===============================================
cd /d "%~dp0"
call mvnw.cmd clean install -DskipTests
if errorlevel 1 (
    echo [ERROR] Backend build failed
    pause
    exit /b 1
)
echo [OK] Backend build completed

REM Start Backend
echo.
echo ===============================================
echo [2/4] Starting Backend Server...
echo ===============================================
start "Backend - Sponsorship App" cmd /k call mvnw.cmd spring-boot:run
echo [OK] Backend starting on http://localhost:7070
timeout /t 5

REM Install Frontend Dependencies
echo.
echo ===============================================
echo [3/4] Installing Frontend Dependencies...
echo ===============================================
cd frontend
call npm install
if errorlevel 1 (
    echo [ERROR] npm install failed
    pause
    exit /b 1
)
echo [OK] Dependencies installed

REM Start Frontend
echo.
echo ===============================================
echo [4/4] Starting Frontend Server...
echo ===============================================
start "Frontend - Sponsorship App" cmd /k call npm start
echo [OK] Frontend starting on http://localhost:4200
timeout /t 3

echo.
echo ===============================================
echo    STARTUP COMPLETE!
echo ===============================================
echo.
echo Backend:  http://localhost:7070
echo Frontend: http://localhost:4200
echo.
echo Both servers are starting in new windows.
echo Press ENTER to close this window...
echo.
pause

