# GitHub Upload Guide for Medical IoT System

## Prerequisites
1. Install Git for Windows from: https://git-scm.com/download/win
2. Restart your terminal/PowerShell after installation
3. Have your GitHub credentials ready

## Step-by-Step Upload Process

### Step 1: Initialize Git Repository
Open PowerShell in the project directory and run:

```powershell
cd "d:\Malcolm\DSCE\Major Project\Framework Reworked\Medical_IoT_System"
git init
```

### Step 2: Configure Git (First Time Only)
```powershell
git config --global user.name "Malcolm Cephas"
git config --global user.email "your-email@example.com"
```

### Step 3: Add Remote Repository
```powershell
git remote add origin https://github.com/malcolm-cephas/Medical-IoT.git
```

### Step 4: Check Current Branch
```powershell
git branch
```

If you're not on 'main', create and switch to it:
```powershell
git checkout -b main
```

### Step 5: Add All Files
```powershell
git add .
```

### Step 6: Commit Changes
```powershell
git commit -m "Initial commit: Complete Medical IoT System with ABE, IPFS, and Blockchain"
```

### Step 7: Pull Existing Repository (If Any)
```powershell
git pull origin main --allow-unrelated-histories
```

If there are conflicts, resolve them and commit:
```powershell
git add .
git commit -m "Merge with existing repository"
```

### Step 8: Push to GitHub
```powershell
git push -u origin main
```

You'll be prompted for your GitHub credentials. Use a Personal Access Token instead of password:
- Go to GitHub → Settings → Developer settings → Personal access tokens
- Generate new token with 'repo' scope
- Use the token as your password

## Alternative: Force Push (If Repository is Empty or You Want to Overwrite)

⚠️ **WARNING**: This will overwrite everything in the remote repository!

```powershell
git push -u origin main --force
```

## Troubleshooting

### Error: "fatal: not a git repository"
Solution: Make sure you ran `git init` first

### Error: "remote origin already exists"
Solution: Remove and re-add the remote:
```powershell
git remote remove origin
git remote add origin https://github.com/malcolm-cephas/Medical-IoT.git
```

### Error: "failed to push some refs"
Solution: Pull first, then push:
```powershell
git pull origin main --rebase
git push -u origin main
```

### Large Files Warning
If you get warnings about large files (like node_modules), they're already in .gitignore.
Run:
```powershell
git rm -r --cached node_modules
git rm -r --cached backend-spring/target
git rm -r --cached frontend-dashboard/dist
git commit -m "Remove build artifacts"
git push
```

## What Gets Uploaded

✅ **Included:**
- All source code (Java, Python, JavaScript)
- Configuration files
- README.md and documentation
- Batch scripts
- Database setup files

❌ **Excluded (via .gitignore):**
- node_modules/
- target/ (Java build)
- dist/ (React build)
- *.log files
- __pycache__/
- .env files

## After Upload

1. Go to https://github.com/malcolm-cephas/Medical-IoT
2. Verify all files are uploaded
3. Check the README.md displays correctly
4. Add a repository description and topics (tags)

## Recommended Repository Settings

### Topics to Add:
- healthcare
- iot
- encryption
- blockchain
- ipfs
- spring-boot
- react
- python
- security
- medical-devices

### Description:
"Secure Medical IoT System with ABE Encryption, IPFS Storage, and Blockchain Audit Trail"

## Keeping Repository Updated

After making changes:
```powershell
git add .
git commit -m "Description of changes"
git push
```

## Need Help?

If Git is not installed:
1. Download: https://git-scm.com/download/win
2. Install with default settings
3. Restart PowerShell
4. Try the commands again
