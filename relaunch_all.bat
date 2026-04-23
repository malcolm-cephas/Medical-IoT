@echo off
title EMERGENCY RELAUNCH - ALL SERVICES
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
echo   1. STARTING AI ANALYTICS (PORT 4242)
echo ===================================================
cd analytics-python
start "AI Analytics (4242)" cmd /c "python -m uvicorn main:app --reload --port 4242"
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
echo   3. STARTING AI FACE SERVICE (PORT 5050)
echo ===================================================
cd ai-face-service
start "AI Face Service (5050)" cmd /c "python app.py"
cd ..

echo.
echo ===================================================
echo   4. STARTING AI MCP SERVER (PORT 9090)
echo ===================================================
cd ai/mcp-server
start "AI MCP Server (9090)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   5. STARTING AI MCP CLIENT (PORT 8083)
echo ===================================================
cd ai/mcp-client
start "AI MCP Client (8083)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   6. STARTING MAIN BACKEND (PORT 8080)
echo ===================================================
cd backend-spring
start "Spring Backend (8080)" cmd /c "mvn spring-boot:run"
cd ..

echo.
echo ===================================================
echo   7. STARTING FRONTEND (PORT 5173)
echo ===================================================
cd frontend-dashboard
start "React Frontend (5173)" cmd /c "npm run dev"
cd ..

echo.
echo ===================================================
echo   RELAUNCH INITIATED - PLEASE REFRESH IN 1 MINUTE
echo ===================================================
pause
