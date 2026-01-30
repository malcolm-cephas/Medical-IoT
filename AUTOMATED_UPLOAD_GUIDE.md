# Automated GitHub Upload - Quick Start

## 🚀 One-Click Upload

Simply **double-click** the file:
```
upload_to_github.bat
```

That's it! The script will:
1. ✅ Check if Git is installed (install if needed)
2. ✅ Initialize Git repository
3. ✅ Configure Git settings
4. ✅ Add all your files
5. ✅ Create a professional commit
6. ✅ Upload to GitHub

## 📋 What You Need

### GitHub Personal Access Token
You'll need a token instead of your password:

1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Give it a name: "Medical IoT Upload"
4. Select scope: **✓ repo** (check this box)
5. Click **"Generate token"**
6. **COPY THE TOKEN** (you won't see it again!)

### When Prompted:
- **Username**: `malcolm-cephas`
- **Password**: Paste your Personal Access Token

## ⚠️ First Time Running?

If Git is not installed:
1. The script will download and install Git automatically
2. After installation, **restart PowerShell**
3. Run `upload_to_github.bat` again

## 🔍 What Gets Uploaded?

The `.gitignore` file automatically excludes:
- ❌ `node_modules/` (too large)
- ❌ `target/` (build artifacts)
- ❌ `dist/` (build output)
- ❌ `*.log` files
- ❌ `__pycache__/`

Everything else is uploaded!

## 🛠️ Troubleshooting

### "Execution Policy" Error
Right-click `upload_to_github.bat` → **Run as Administrator**

### "Authentication Failed"
- Make sure you're using a **Personal Access Token**, not your password
- Check the token has **repo** scope
- Token might have expired - generate a new one

### "Repository Not Found"
- Verify the repository exists: https://github.com/malcolm-cephas/Medical-IoT
- Check you have write access to the repository

### "Failed to Push"
If the remote repository already has files:
```powershell
git pull origin main --allow-unrelated-histories
git push -u origin main
```

## 📝 Manual Alternative

If the automated script doesn't work, you can run commands manually:

```powershell
cd "d:\Malcolm\DSCE\Major Project\Framework Reworked\Medical_IoT_System"
git init
git config user.name "Malcolm Cephas"
git config user.email "your-email@example.com"
git remote add origin https://github.com/malcolm-cephas/Medical-IoT.git
git add .
git commit -m "Add files via upload"
git push -u origin main
```

## ✅ After Upload

1. Visit: https://github.com/malcolm-cephas/Medical-IoT
2. Verify all files are there
3. Check README.md displays correctly
4. Add repository description and topics

## 🔄 Future Updates

To upload changes later:
```powershell
git add .
git commit -m "Description of changes"
git push
```

Or just run `upload_to_github.bat` again!
