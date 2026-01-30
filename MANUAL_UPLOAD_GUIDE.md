# Manual Upload to GitHub Guide

## Quick Upload via GitHub Web Interface

Since you'll be uploading files manually through GitHub's "Add files via upload" feature, follow these steps:

### Step 1: Prepare Files for Upload

**Files to Upload:**
All files in `d:\Malcolm\DSCE\Major Project\Framework Reworked\Medical_IoT_System\`

**EXCLUDE these folders/files** (they're too large or auto-generated):
- ❌ `node_modules/` (in frontend-dashboard)
- ❌ `target/` (in backend-spring)
- ❌ `dist/` (in frontend-dashboard)
- ❌ Any `.log` files

### Step 2: Create a ZIP File

1. Open the project folder
2. Select all files and folders EXCEPT the excluded ones above
3. Right-click → Send to → Compressed (zipped) folder
4. Name it: `Medical-IoT-System.zip`

### Step 3: Upload to GitHub

1. Go to: https://github.com/malcolm-cephas/Medical-IoT
2. Click **"Add file"** → **"Upload files"**
3. Drag and drop your ZIP file OR click "choose your files"
4. In the commit message box, use the text below

### Step 4: Commit Message

**Title:**
```
Add files via upload
```

**Description:**
```
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
Security: ABE, ECDH, IPFS, Blockchain
```

### Step 5: Complete Upload

1. Click **"Commit changes"**
2. Wait for upload to complete
3. Verify files appear in the repository

## Alternative: Upload Folders Separately

If the ZIP is too large, upload in parts:

1. **First Upload**: Core files
   - README.md
   - .gitignore
   - DATABASE_SETUP.md
   - *.bat files
   - mock_data_generator.py

2. **Second Upload**: backend-spring folder
   - Upload `backend-spring/src/` folder
   - Upload `backend-spring/pom.xml`

3. **Third Upload**: frontend-dashboard folder
   - Upload `frontend-dashboard/src/` folder
   - Upload `frontend-dashboard/public/` folder
   - Upload package.json, vite.config.js, etc.

4. **Fourth Upload**: analytics-python folder
   - Upload all Python files
   - Upload requirements.txt

## What to Include/Exclude

### ✅ INCLUDE:
- All `.java` source files
- All `.jsx`, `.js`, `.css` files
- All `.py` files
- `package.json`, `pom.xml`
- Configuration files
- README.md
- .gitignore
- Batch scripts

### ❌ EXCLUDE:
- `node_modules/` (too large, 100+ MB)
- `target/` (build artifacts)
- `dist/` (build output)
- `.log` files
- `__pycache__/`

## After Upload

Add repository details:
- **Description**: "Secure Medical IoT System with ABE Encryption, IPFS Storage, and Blockchain Audit Trail"
- **Topics**: healthcare, iot, encryption, blockchain, ipfs, spring-boot, react, python, security, medical-devices
- **Website**: (optional) Link to demo if deployed

## File Size Limits

GitHub has a 100 MB file size limit. If any single file is larger:
1. Check if it's in the exclude list
2. If needed, use Git LFS (Large File Storage)
3. Or split into smaller parts

## Need Help?

If upload fails:
- Check file sizes
- Ensure you excluded node_modules and target folders
- Try uploading folders one at a time
- Clear browser cache and try again
