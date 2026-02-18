@echo off
setlocal

:: Create Log directory if it doesn't exist
if not exist "Logs" mkdir "Logs"

:: Clear existing logs
type nul > Logs\backend.log
type nul > Logs\frontend.log
type nul > Logs\analytics.log

:: Start Applications
echo ===========================================
echo Starting Medical IoT System v2.0
echo ===========================================

echo [1/3] Starting Analytics Service (Port 4242)...
start "Analytics Service (Port 4242)" powershell -NoExit -Command "cd 'analytics-python'; python -m pip install -r requirements.txt; python -m uvicorn main:app --reload --port 4242 2>&1 | Tee-Object -FilePath '..\Logs\analytics.log'"

echo [2/3] Starting Spring Backend (Port 8080)...
start "Spring Backend (Port 8080)" powershell -NoExit -Command "cd 'backend-spring'; mvn spring-boot:run 2>&1 | Tee-Object -FilePath '..\Logs\backend.log'"

echo [3/3] Starting React Frontend (Port 5173)...
start "React Frontend (Port 5173)" powershell -NoExit -Command "cd 'frontend-dashboard'; npm install; npm run dev 2>&1 | Tee-Object -FilePath '..\Logs\frontend.log'"

:: Start Log Monitor
start "Log Monitor" powershell -NoExit -ExecutionPolicy Bypass -File "monitor_logs.ps1"

echo ===========================================
echo All services launching...
echo Check Logs folder for detailed output.
echo ===========================================
pause
