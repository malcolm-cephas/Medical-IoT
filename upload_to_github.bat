@echo off
echo ========================================
echo Medical IoT System - GitHub Uploader
echo ========================================
echo.
echo This will automatically upload your project to GitHub
echo.
pause

PowerShell -ExecutionPolicy Bypass -File "%~dp0upload_to_github.ps1"

pause
