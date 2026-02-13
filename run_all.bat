@echo off
title Medical IoT System Launcher
cls
echo ===================================================
echo   MEDICAL IOT SYSTEM - LAUNCHER
echo ===================================================

echo.
echo [CHECK] Ensuring MySQL requirements...
echo Attempting to start MySQL Service (Requires Admin)...
net start MySQL80 || echo [WARN] Could not start MySQL via script. Please ensure it is running manually.

echo.
echo [1/3] Starting Python Analytics Service (Port 4242)...
start "Python Analytics" cmd /k "cd analytics-python && echo Installing dependencies... && pip install -r requirements.txt && echo Starting FastAPI... && uvicorn main:app --reload --port 4242 || pause"

echo.
echo [2/3] Starting Spring Boot Backend...
start "Spring Boot Backend" cmd /k "cd backend-spring && echo Building... && mvn clean package -DskipTests && echo Starting JAR... && java -jar target/medical-iot-0.0.1-SNAPSHOT.jar || pause"

echo.
echo [3/3] Starting Frontend Dashboard...
start "Frontend Dashboard" cmd /k "cd frontend-dashboard && echo Installing dependencies... && npm install && echo Starting Vite... && npm run dev || pause"

echo.
echo ===================================================
echo   SYSTEM LAUNCHED SUCCESSFULLY
echo ===================================================
echo Services are starting in separate windows:
echo  - Frontend:  http://localhost:5173
echo  - Backend:   http://localhost:8080
echo  - Analytics: http://localhost:4242
echo  - Validating: MySQL Connection (Check Backend Logs)
echo ===================================================
echo.
echo NEW FEATURES AVAILABLE:
echo  - Doctor-Patient Appointments (Click Appointments Tab)
echo  - Doctors: Set availability and manage appointments
echo  - Patients: Browse doctors and book appointments
echo.
echo QUICK START:
echo  1. Login as doctor_micheal to set availability
echo  2. Login as patient_alpha to book appointments
echo  3. Check the Appointments tab in the dashboard
echo ===================================================
echo Press any key to exit this launcher (Services will keep running)...
pause >nul
