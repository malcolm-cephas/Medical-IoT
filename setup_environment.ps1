# Medical IoT System - Environment Setup Script
# Run this in an Administrator PowerShell window.

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   Medical IoT System: Automated Installer     " -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

# 1. Check for Administrative Privileges
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "`n[ERROR] PLEASE RUN THIS SCRIPT AS AN ADMINISTRATOR." -ForegroundColor Red
    pause
    exit
}

# 2. Install Tools via Winget
Write-Host "`n[1/5] Installing System Prerequisites..." -ForegroundColor Yellow

# Winget IDs for the required stack
$tools = @(
    @{ Name = "Java 21 JDK"; Id = "Oracle.JDK.21"; Command = "java" },
    @{ Name = "Node.js LTS"; Id = "OpenJS.NodeJS.LTS"; Command = "node" },
    @{ Name = "Python 3.11"; Id = "Python.Python.3.11"; Command = "python" },
    @{ Name = "Apache Maven"; Id = "Apache.Maven"; Command = "mvn" },
    @{ Name = "MySQL Server"; Id = "MySQL.MySQL"; Command = "mysql" }
)

foreach ($tool in $tools) {
    Write-Host "Checking for $($tool.Name)..." -NoNewline
    # 1. Check if the command already exists in the PATH
    $installed = Get-Command $tool.Command -ErrorAction SilentlyContinue
    
    # 2. If not in PATH, check if it's installed via Winget specifically
    if (-not $installed) {
        winget list --id $($tool.Id) -e *>$null
        if ($lastExitCode -eq 0) { $installed = $true }
    }

    if (-not $installed) {
        Write-Host " Not found. Installing..." -ForegroundColor Green
        winget install --id $($tool.Id) --silent --accept-package-agreements --accept-source-agreements
    }
    else {
        Write-Host " Already installed." -ForegroundColor Gray
    }
}

# Refresh Path in current session
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")

# 3. Project Dependencies
Write-Host "`n[2/5] Building Backend (Maven)..." -ForegroundColor Yellow
if (Test-Path "backend-spring") {
    Set-Location "backend-spring"
    mvn clean install -DskipTests
    Set-Location ".."
}

Write-Host "`n[3/5] Installing Frontend Dependencies (NPM)..." -ForegroundColor Yellow
if (Test-Path "frontend-dashboard") {
    Set-Location "frontend-dashboard"
    npm install
    Set-Location ".."
}

Write-Host "`n[4/5] Installing Analytics Dependencies (Python)..." -ForegroundColor Yellow
if (Test-Path "analytics-python") {
    Set-Location "analytics-python"
    python -m pip install --upgrade pip
    python -m pip install -r requirements.txt
    Set-Location ".."
}

# 4. Environment File Setup
Write-Host "`n[5/5] Setting up .env file..." -ForegroundColor Yellow
if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host " SUCCESS: .env created from template." -ForegroundColor Green
}
else {
    Write-Host " SKIP: .env already exists." -ForegroundColor Gray
}

Write-Host "`n===============================================" -ForegroundColor Green
Write-Host "       SETUP COMPLETE! READY TO RUN           " -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host "PLEASE RESTART YOUR TERMINAL NOW."
Write-Host "Next Steps:"
Write-Host "1. Open '.env' and set your MySQL password."
Write-Host "2. Run 'run_all.bat' to start the system."
pause
