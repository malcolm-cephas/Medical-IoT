@echo off
title Medical IoT - Sensor Simulation
echo ===================================================
echo   MEDICAL IOT - MOCK SENSOR GENERATOR
echo ===================================================
echo This script simulates 35 active patient monitors.
echo sending live data to http://localhost:8080/api/sensor/upload
echo.

echo [1/2] Checking Dependencies...
pip install colorama requests

echo.
echo [2/2] Starting Simulation Stream...
python mock_data_generator.py

pause
