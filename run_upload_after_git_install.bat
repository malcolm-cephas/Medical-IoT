@echo off
echo ========================================
echo Git has been installed successfully!
echo ========================================
echo.
echo Now running the upload script...
echo.
pause

REM Close this window and open a new PowerShell
PowerShell -ExecutionPolicy Bypass -File "%~dp0upload_to_github.ps1"

pause
