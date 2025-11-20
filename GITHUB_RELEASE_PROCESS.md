# GitHub Release v1.0.0 Creation Process

This document describes the complete process for creating and publishing the GitHub Release with APK download for Virtual Companion v1.0.0.

## 📋 Overview

The release process involves:
1. Building the release APK
2. Creating a GitHub tag
3. Creating a GitHub Release
4. Uploading the APK file
5. Publishing the release with detailed notes

## 🔄 Current Status

- **Version:** 1.0.0
- **Branch:** `gh-release-v1.0.0-apk-upload`
- **Repository:** https://github.com/arturich267/asis
- **Status:** Ready for Release

## 📝 Release Details

### Release Specification
- **Version Name:** 1.0.0
- **Version Code:** 1
- **Title:** Virtual Companion v1.0.0 - Final Release
- **Tag:** v1.0.0
- **Status:** Published (not draft)

### Release URLs
```
Release Page: https://github.com/arturich267/asis/releases/tag/v1.0.0
Direct Download: https://github.com/arturich267/asis/releases/download/v1.0.0/app-release.apk
All Releases: https://github.com/arturich267/asis/releases
```

## 🚀 Release Process

### Option 1: Automated GitHub Actions (Recommended)

When a tag is pushed matching pattern `v*`, GitHub Actions automatically:
1. ✅ Checks out code
2. ✅ Sets up Java and Android SDK
3. ✅ Builds release APK
4. ✅ Creates GitHub Release
5. ✅ Uploads APK file
6. ✅ Publishes with release notes from `GITHUB_RELEASE.md`

**To trigger:**
```bash
cd /home/engine/project
git tag -a v1.0.0 -m "Virtual Companion v1.0.0 - Final Release" HEAD
git push origin v1.0.0
```

**Workflow:** `.github/workflows/release-apk.yml`

### Option 2: Manual Local Release

**Step 1: Build APK**
```bash
cd /home/engine/project
./gradlew clean assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

**Step 2: Create Git Tag**
```bash
git tag -a v1.0.0 -m "Virtual Companion v1.0.0 - Final Release" HEAD
git push origin v1.0.0
```

**Step 3: Create GitHub Release**

Using the provided script:
```bash
export GITHUB_TOKEN="your_github_token_here"
./create-release-with-apk.sh
```

Or using GitHub CLI directly:
```bash
export GITHUB_TOKEN="your_github_token_here"
gh auth status  # Verify authentication

gh release create v1.0.0 \
  app/build/outputs/apk/release/app-release.apk \
  --title "Virtual Companion v1.0.0 - Final Release" \
  --notes-file GITHUB_RELEASE.md \
  --draft=false
```

Or using the original script:
```bash
./create-release.sh
```

## 📄 Release Notes

The release uses content from `GITHUB_RELEASE.md` which includes:

### Features (English + Russian)
- 💬 Intelligent Chat
- 🎤 Voice Interaction
- 🤖 AI-Powered Meme Generator
- 🎨 Customization
- 🔒 Privacy First
- ⚙️ Comprehensive Settings

### Russian Features (🎉 Возможности)
✅ Импорт архивов WhatsApp  
✅ Генерация мемных ответов  
✅ Текстовый чат с историей  
✅ Голосовое взаимодействие с TTS  
✅ Управление приватностью  
✅ Кастомизация тем  

### Installation Instructions (Russian - 📥 Установка)
1. Скачайте app-release.apk
2. Разрешите установку из неизвестных источников
3. Откройте файл и нажмите Install

### Requirements (Russian - 📱 Требования)
- Android 5.2+ (minSdk 23)
- ~100 MB памяти
- Разрешения: Хранилище + Микрофон

## ✅ Verification Checklist

After release creation, verify:

- [ ] Release visible at: https://github.com/arturich267/asis/releases/tag/v1.0.0
- [ ] Release title: "Virtual Companion v1.0.0 - Final Release"
- [ ] Release status: Published (not Draft)
- [ ] APK file attached: app-release.apk
- [ ] APK file size visible (50-100 MB)
- [ ] Release notes display correctly
- [ ] Both English and Russian text visible
- [ ] Download link accessible
- [ ] Direct APK URL works: https://github.com/arturich267/asis/releases/download/v1.0.0/app-release.apk

## 🔗 Required GitHub Token

For manual release creation, you need a GitHub Personal Access Token with:
- ✅ `repo` scope (full control of repositories)
- ✅ `gist` scope (create gists)
- ✅ Appropriate expiration (90 days - 1 year recommended)

**Environment Variable:** `GITHUB_TOKEN`

### Example:
```bash
export GITHUB_TOKEN="ghs_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
gh auth status  # Verify
```

## 📦 APK Specifications

### File Details
- **Filename:** app-release.apk
- **Location:** app/build/outputs/apk/release/app-release.apk
- **Size:** 50-100 MB
- **Format:** Signed APK
- **Architecture:** Universal (arm64-v8a + armeabi-v7a)

### Build Configuration
- **Minification:** R8 ProGuard enabled
- **Resource Shrinking:** Enabled
- **Code Obfuscation:** Enabled
- **Signing:** Debug keystore (for v1.0.0)
- **compileSdk:** 34
- **targetSdk:** 34
- **minSdk:** 23

## 🛠️ Available Scripts

### create-release.sh
Original release creation script using GitHub CLI.
```bash
./create-release.sh
```

### create-release-with-apk.sh
Enhanced script with explicit repository configuration and APK upload.
```bash
export GITHUB_TOKEN="your_token"
./create-release-with-apk.sh
```

### build-release.sh
Build script for creating release APK locally.
```bash
./build-release.sh
```

## 📋 Documentation Files

### Release Documentation
- **GITHUB_RELEASE.md** - Main release notes (EN + RU)
- **APK_RELEASE_NOTES.md** - Comprehensive APK release information
- **RELEASE_CHECKLIST_v1.0.0.md** - Complete release checklist
- **INSTALL.md** - User installation guide
- **INSTALL_GUIDE.md** - Detailed installation instructions
- **README.md** - Main repository readme with download link

### Release Process Documentation
- **RELEASE_v1.0.0.md** - Release preparation documentation
- **RELEASE_READY.md** - Quick reference for ready state
- **RELEASE_UPLOAD_COMPLETED.md** - Completion report
- **RELEASE_NOTES_v1.0.md** - Version notes

## 🔍 Troubleshooting

### GitHub CLI Not Found
```bash
# Install GitHub CLI
sudo apt-get install gh

# Authenticate
gh auth login
# OR set environment variable
export GITHUB_TOKEN="your_token_here"
```

### APK Not Found After Build
```bash
# Verify build location
ls -lh app/build/outputs/apk/release/

# Check build logs
./gradlew clean assembleRelease --info
```

### GitHub Release Already Exists
```bash
# View existing release
gh release view v1.0.0

# Delete and recreate (if needed)
gh release delete v1.0.0 --yes
git tag -d v1.0.0
git push origin --delete v1.0.0

# Recreate release and tag
git tag -a v1.0.0 -m "Virtual Companion v1.0.0 - Final Release" HEAD
git push origin v1.0.0
```

### Authentication Issues
```bash
# Verify GitHub CLI authentication
gh auth status

# Re-authenticate if needed
gh auth logout
gh auth login

# Or use personal access token
export GITHUB_TOKEN="ghs_xxxxxxxxxxxx"
gh auth status
```

## 📊 Release Statistics

- **Version:** 1.0.0
- **Build:** 1
- **Architecture:** Android
- **Minimum SDK:** 23 (Android 6.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Size:** 50-100 MB (with FFmpeg library)
- **Format:** Single APK (universal architecture)
- **Support:** 99%+ of Android devices

## 🎯 Success Criteria

The release is successfully created when:

1. ✅ GitHub Release created at v1.0.0
2. ✅ Release is Published (not Draft)
3. ✅ APK file uploaded and visible
4. ✅ Release notes display both English and Russian
5. ✅ Download link is accessible and working
6. ✅ File size and checksum visible
7. ✅ Release appears on /releases page
8. ✅ Direct download URL works
9. ✅ All formatting and links work correctly
10. ✅ Installation instructions clear

## 📞 Support

For issues or questions about the release process:
1. Check this document first
2. Review GitHub Actions workflow logs
3. Check GitHub Release page for errors
4. Review `RELEASE_CHECKLIST_v1.0.0.md` for pre-release requirements

## 🎊 Next Steps

After successful release:
1. Announce release on social media
2. Share direct download link
3. Update project documentation
4. Monitor for user feedback
5. Plan version 1.1 improvements
6. Track download statistics

## 📄 Links

- **Repository:** https://github.com/arturich267/asis
- **Releases:** https://github.com/arturich267/asis/releases
- **v1.0.0 Release:** https://github.com/arturich267/asis/releases/tag/v1.0.0
- **Direct APK Download:** https://github.com/arturich267/asis/releases/download/v1.0.0/app-release.apk
- **Issues:** https://github.com/arturich267/asis/issues

---

**Virtual Companion v1.0.0 Release Process**  
*Ready for publication on GitHub*  
*Date: 2024 | Status: Complete*
