@echo off
title Stop Medical IoT System
cls
echo ===================================================
echo   STOPPING MEDICAL IOT SYSTEM
echo ===================================================

echo.
echo [1/3] Stopping Java Processes (Backend)...
taskkill /F /IM java.exe /T 2>nul
if %errorlevel% equ 0 (echo    - Java stopped.) else (echo    - Java was not running.)

echo.
echo [2/3] Stopping Python Processes (Analytics)...
taskkill /F /IM python.exe /T 2>nul
if %errorlevel% equ 0 (echo    - Python stopped.) else (echo    - Python was not running.)
taskkill /F /IM uvicorn.exe /T 2>nul

echo.
echo [3/3] Stopping Node.js Processes (Frontend)...
taskkill /F /IM node.exe /T 2>nul
if %errorlevel% equ 0 (echo    - Node.js stopped.) else (echo    - Node.js was not running.)

echo.
echo [CLEANUP] Closing Command Windows...
taskkill /F /FI "WINDOWTITLE eq Python Analytics" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq Spring Boot Backend" /T 2>nul
taskkill /F /FI "WINDOWTITLE eq Frontend Dashboard" /T 2>nul

echo.
echo ===================================================
echo   ALL SERVICES STOPPED
echo ===================================================
pause
