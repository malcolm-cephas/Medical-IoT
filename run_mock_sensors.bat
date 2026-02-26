@echo off
title Medical IoT - HAL Simulator
echo ===================================================
echo ===================================================
echo   MEDICAL IOT - HAL SIMULATOR
echo ===================================================
echo This script simulates 35 active Arduino Uno R4 WiFi nodes.
echo sending live signal data to http://localhost:8080/api/sensor/upload
echo.

echo [1/2] Checking Dependencies...
pip install colorama requests

echo.
echo [2/2] Starting Simulation Stream...
python mock_data_generator.py

pause
