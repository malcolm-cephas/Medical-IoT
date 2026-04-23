@echo off
setlocal

:: Create Log directory if it doesn't exist
if not exist "Logs" mkdir "Logs"

:: Clear existing logs
type nul > Logs\backend.log
type nul > Logs\frontend.log
type nul > Logs\analytics.log
type nul > Logs\mcp-server.log
type nul > Logs\mcp-client.log

:: Start Applications
echo ===========================================
echo Starting Medical IoT System v2.0 (IoT Engineering Edition)
echo ===========================================

echo [1/7] Starting Analytics Service (Port 4242)...
start "Analytics Service (Port 4242)" powershell -NoExit -Command "cd 'analytics-python'; python -m pip install -r requirements.txt; python -m uvicorn main:app --reload --port 4242 2>&1 | Tee-Object -FilePath '..\Logs\analytics.log'"

echo [2/7] Starting Auth Server (Port 9000)...
start "Auth Server (Port 9000)" powershell -NoExit -Command "cd 'medical-auth-server'; mvn spring-boot:run 2>&1 | Tee-Object -FilePath '..\Logs\auth.log'"

echo [3/7] Starting AI MCP Server (Port 9090)...
start "AI MCP Server (Port 9090)" powershell -NoExit -Command "cd 'ai/mcp-server'; mvn spring-boot:run 2>&1 | Tee-Object -FilePath '..\..\Logs\mcp-server.log'"

echo [4/7] Starting AI MCP Client (Port 8083)...
start "AI MCP Client (Port 8083)" powershell -NoExit -Command "cd 'ai/mcp-client'; mvn spring-boot:run 2>&1 | Tee-Object -FilePath '..\..\Logs\mcp-client.log'"

echo [5/7] Starting Spring Backend (Port 8080)...
start "Spring Backend (Port 8080)" powershell -NoExit -Command "cd 'backend-spring'; mvn spring-boot:run 2>&1 | Tee-Object -FilePath '..\Logs\backend.log'"

echo [6/7] Starting React Frontend (Port 5173)...
start "React Frontend (Port 5173)" powershell -NoExit -Command "cd 'frontend-dashboard'; npm install; npm run dev 2>&1 | Tee-Object -FilePath '..\Logs\frontend.log'"

echo [7/7] Starting AI Face Biometrics Service (Port 5050)...
start "AI Face Service (Port 5050)" powershell -NoExit -Command "cd 'ai-face-service'; python -m pip install -r requirements.txt; python app.py 2>&1 | Tee-Object -FilePath '..\Logs\face-ai.log'"

:: Start Log Monitor
start "Log Monitor" powershell -NoExit -ExecutionPolicy Bypass -File "monitor_logs.ps1"

echo ===========================================
echo All 7 medical services launching...
echo Check Logs folder for detailed output.
echo ===========================================
pause
