# Automated GitHub Upload Script for Medical IoT System
# This script will install Git (if needed) and upload your project

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Medical IoT System - GitHub Uploader" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$REPO_URL = "https://github.com/malcolm-cephas/Medical-IoT.git"
$PROJECT_DIR = "d:\Malcolm\DSCE\Major Project\Framework Reworked\Medical_IoT_System"

# Check if Git is installed
Write-Host "[1/6] Checking for Git installation..." -ForegroundColor Yellow
$gitPath = Get-Command git -ErrorAction SilentlyContinue

if (-not $gitPath) {
    Write-Host "Git not found. Installing Git..." -ForegroundColor Red
    
    # Download Git installer
    $gitInstallerUrl = "https://github.com/git-for-windows/git/releases/download/v2.43.0.windows.1/Git-2.43.0-64-bit.exe"
    $installerPath = "$env:TEMP\GitInstaller.exe"
    
    Write-Host "Downloading Git installer..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $gitInstallerUrl -OutFile $installerPath
    
    Write-Host "Installing Git (this may take a minute)..." -ForegroundColor Yellow
    Start-Process -FilePath $installerPath -ArgumentList "/VERYSILENT /NORESTART" -Wait
    
    # Add Git to PATH for current session
    $env:Path += ";C:\Program Files\Git\bin"
    
    Write-Host "Git installed successfully!" -ForegroundColor Green
    Write-Host "Please restart PowerShell and run this script again." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host "Git is installed!" -ForegroundColor Green

# Navigate to project directory
Write-Host ""
Write-Host "[2/6] Navigating to project directory..." -ForegroundColor Yellow
Set-Location $PROJECT_DIR

# Initialize Git repository
Write-Host ""
Write-Host "[3/6] Initializing Git repository..." -ForegroundColor Yellow
if (-not (Test-Path ".git")) {
    git init -b main
    Write-Host "Git repository initialized with 'main' branch!" -ForegroundColor Green
}
else {
    Write-Host "Git repository already exists!" -ForegroundColor Green
    # Ensure we're on main branch
    $currentBranch = git branch --show-current
    if ($currentBranch -ne "main") {
        git checkout -b main 2>$null
        if ($LASTEXITCODE -ne 0) {
            git branch -M main
        }
        Write-Host "Switched to 'main' branch!" -ForegroundColor Green
    }
}

# Configure Git user (if not configured)
Write-Host ""
Write-Host "[4/6] Configuring Git..." -ForegroundColor Yellow
$userName = git config user.name
if (-not $userName) {
    git config user.name "Malcolm Cephas"
    git config user.email "malcolm.cephas@example.com"
    Write-Host "Git user configured!" -ForegroundColor Green
}
else {
    Write-Host "Git already configured for: $userName" -ForegroundColor Green
}

# Add remote repository
Write-Host ""
Write-Host "[5/6] Setting up remote repository..." -ForegroundColor Yellow
$existingRemote = git remote get-url origin 2>$null
if (-not $existingRemote) {
    git remote add origin $REPO_URL
    Write-Host "Remote repository added!" -ForegroundColor Green
}
else {
    Write-Host "Remote repository already configured!" -ForegroundColor Green
    # Update remote URL in case it changed
    git remote set-url origin $REPO_URL
}

# Stage all files
Write-Host ""
Write-Host "[6/6] Preparing files for upload..." -ForegroundColor Yellow
git add .

# Show what will be committed
Write-Host ""
Write-Host "Files to be uploaded:" -ForegroundColor Cyan
$statusOutput = git status --short
if ($statusOutput) {
    Write-Host $statusOutput
}
else {
    Write-Host "No new files to upload (everything up to date)" -ForegroundColor Yellow
}

# Commit changes
Write-Host ""
Write-Host "Creating commit..." -ForegroundColor Yellow
git commit -m "Add files via upload

Complete Medical IoT System with advanced security features:

- Spring Boot backend with REST API
- React frontend with real-time monitoring
- Python analytics service for ABE encryption
- Consent-based access control system
- IPFS integration for decentralized storage
- Blockchain audit logging
- ECDH image encryption
- Real-time patient vital monitoring (HR, SpO2, Temp, Humidity, BP)
- Ward statistics dashboard
- Browser notifications for critical alerts
- CSV data export functionality
- Mobile-responsive design
- Dark/Light theme support

Tech Stack: Spring Boot, React, Python, MySQL, FastAPI, Chart.js
Security: ABE, ECDH, IPFS, Blockchain"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Commit created successfully!" -ForegroundColor Green
}
else {
    Write-Host "No changes to commit or commit already exists" -ForegroundColor Yellow
}

# Push to GitHub
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Ready to upload to GitHub!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Repository: $REPO_URL" -ForegroundColor White
Write-Host ""
Write-Host "You will be prompted for your GitHub credentials:" -ForegroundColor Yellow
Write-Host "  Username: malcolm-cephas" -ForegroundColor White
Write-Host "  Password: Use a Personal Access Token (not your password!)" -ForegroundColor White
Write-Host ""
Write-Host "To create a token:" -ForegroundColor Yellow
Write-Host "  1. Go to: https://github.com/settings/tokens" -ForegroundColor White
Write-Host "  2. Click 'Generate new token (classic)'" -ForegroundColor White
Write-Host "  3. Select 'repo' scope" -ForegroundColor White
Write-Host "  4. Copy the token and use it as password" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to continue with upload..." -ForegroundColor Green
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Push to GitHub
Write-Host ""
Write-Host "Uploading to GitHub..." -ForegroundColor Yellow

# Try to pull first if remote has content
git pull origin main --allow-unrelated-histories 2>$null

# Now push
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "SUCCESS! Project uploaded to GitHub!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "View your repository at:" -ForegroundColor Cyan
    Write-Host "https://github.com/malcolm-cephas/Medical-IoT" -ForegroundColor White
}
else {
    Write-Host ""
    Write-Host "Upload failed. Common issues:" -ForegroundColor Red
    Write-Host "  - Wrong credentials (use Personal Access Token)" -ForegroundColor Yellow
    Write-Host "  - Repository doesn't exist" -ForegroundColor Yellow
    Write-Host "  - Network connection issues" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Try running the script again or check the error above." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
