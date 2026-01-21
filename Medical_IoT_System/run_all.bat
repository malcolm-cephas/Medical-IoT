@echo off
echo ===================================================
echo Starting Decentralized Medical IoT System
echo ===================================================

echo 1. Starting Python Analytics Service...
start "Python Analytics" cmd /k "cd analytics-python && pip install -r requirements.txt && uvicorn main:app --reload --port 8000"

echo 2. Starting Spring Boot Backend...
start "Spring Boot Backend" cmd /k "cd backend-spring && mvn spring-boot:run"

echo 3. Starting Frontend Dashboard...
start "Frontend Dashboard" cmd /k "cd frontend-dashboard && npm install && npm run dev"

echo ===================================================
echo Services are launching in separate windows.
echo - Frontend: http://localhost:5173
echo - Backend:  http://localhost:8080
echo - Analytics: http://localhost:8000
echo ===================================================
pause
