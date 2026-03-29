@echo off
title EMERGENCY RELAUNCH - ALL AI SERVICES
cls
echo ===================================================
echo   CLEANING SYSTEMS...
echo ===================================================
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM python.exe /T 2>nul
taskkill /F /IM uvicorn.exe /T 2>nul
taskkill /F /IM node.exe /T 2>nul

echo.
echo ===================================================
echo   1. STARTING AI ANALYTICS (PORT 4444)
echo ===================================================
cd analytics-python
start "AI Analytics (4444)" cmd /c "python main.py"
cd ..

echo.
echo ===================================================
echo   2. STARTING AUTH SERVER (PORT 9000)
echo ===================================================
cd medical-auth-server
start "Auth Server (9000)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   3. STARTING MAIN BACKEND (PORT 8080)
echo ===================================================
cd backend-spring
start "Spring Backend (8080)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   4. STARTING AI MCP SERVICE (PORT 9090)
echo ===================================================
cd spring-ai-mcp
start "AI MCP Server (9090)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   5. STARTING FRONTEND (PORT 5173)
echo ===================================================
cd frontend-dashboard
start "React Frontend (5173)" cmd /c "npm run dev"
cd ..

echo.
echo ===================================================
echo   RELAUNCH INITIATED - PLEASE REFRESH IN 1 MINUTE
echo ===================================================
pause
