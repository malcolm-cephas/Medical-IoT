@echo off
title Stop Medical IoT System
cls
echo ===================================================
echo   STOPPING MEDICAL IOT SYSTEM (HYBRID AI EDITION)
echo ===================================================

echo.
echo [1/3] Stopping Java Processes (Auth, Backend, MCP)...
taskkill /F /IM java.exe /T 2>nul
if %errorlevel% equ 0 (echo    - All Java services stopped.) else (echo    - Java services were not running.)

echo.
echo [2/3] Stopping Python Processes (Analytics, Face AI)...
taskkill /F /IM python.exe /T 2>nul
if %errorlevel% equ 0 (echo    - Python AI services stopped.) else (echo    - Python AI was not running.)
taskkill /F /IM uvicorn.exe /T 2>nul

echo.
echo [3/3] Stopping Node.js Processes (React Dashboard)...
taskkill /F /IM node.exe /T 2>nul
if %errorlevel% equ 0 (echo    - React Dashboard stopped.) else (echo    - React Dashboard was not running.)

echo.
echo [CLEANUP] Force Closing Terminal Windows...
taskkill /F /FI "WINDOWTITLE eq Analytics Service (Port 4242)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq Auth Server (Port 9000)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq AI MCP Server (Port 9090)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq AI MCP Client (Port 8083)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq Spring Backend (Port 8080)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq React Frontend (Port 5173)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq AI Face Service (Port 5050)" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq Log Monitor" /T 2>nul

echo.
echo ===================================================
echo   ALL AI ^& BACKEND SERVICES STOPPED
echo ===================================================
pause
