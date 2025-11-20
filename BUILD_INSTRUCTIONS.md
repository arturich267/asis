# Quick Build Instructions

## 🚀 Build Release APK - Quick Reference

### Prerequisites Checklist
- [x] All PRs merged into main branch
- [x] Build configuration updated
- [x] ProGuard rules configured
- [x] Signing configuration added
- [x] Documentation created

### Build Commands

#### Option 1: Automated Build Script (Recommended)
```bash
./build-release.sh
```

This script will:
1. Clean the project
2. Run lint checks
3. Run unit tests
4. Build release APK
5. Verify APK creation

#### Option 2: Manual Build
```bash
# Step 1: Clean
./gradlew clean

# Step 2: Build Release
./gradlew assembleRelease

# Step 3: Verify (optional)
./gradlew lint
./gradlew test
```

#### Option 3: Complete Build with All Checks
```bash
./gradlew clean build
```

### Expected Output

**APK Location:**
```
app/build/outputs/apk/release/app-release.apk
```

or

```
app/build/outputs/apk/release/VirtualCompanion-v1.0-release.apk
```

**APK Specifications:**
- Size: ~50-100 MB
- Min SDK: 23
- Target SDK: 34
- Signed: Yes (debug keystore)
- Obfuscated: Yes (R8)
- Optimized: Yes

### Installation

#### On Device via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

#### Manual Installation
1. Copy APK to device
2. Enable "Install from Unknown Sources"
3. Tap APK file
4. Follow installation prompts

### Verification

#### Check APK Info
```bash
# List installed packages
adb shell pm list packages | grep virtualcompanion

# Get package info
adb shell dumpsys package com.asis.virtualcompanion
```

#### Test Launch
```bash
# Launch app
adb shell am start -n com.asis.virtualcompanion/.ui.MainActivity

# Check logs
adb logcat | grep VirtualCompanion
```

### Troubleshooting

#### Build Fails
```bash
# Clear Gradle cache
./gradlew clean --no-daemon

# Invalidate caches (in Android Studio)
# File > Invalidate Caches > Invalidate and Restart
```

#### APK Not Found
```bash
# List all APKs
find app/build/outputs -name "*.apk"
```

#### Installation Fails
```bash
# Uninstall existing version
adb uninstall com.asis.virtualcompanion

# Reinstall
adb install app/build/outputs/apk/release/app-release.apk
```

### Build Artifacts

After successful build:
- `app/build/outputs/apk/release/*.apk` - Release APK
- `app/build/outputs/mapping/release/` - ProGuard mapping files
- `app/build/reports/lint-results.html` - Lint report
- `app/build/reports/tests/` - Test reports

### Production Release

For Google Play Store:

1. **Create Production Keystore:**
   ```bash
   keytool -genkey -v -keystore production-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias virtualcompanion-release
   ```

2. **Update build.gradle.kts** with production keystore

3. **Build App Bundle:**
   ```bash
   ./gradlew bundleRelease
   ```

4. **Upload to Play Console**

### Documentation

- **RELEASE.md** - Comprehensive guide
- **INSTALL_GUIDE.md** - User installation
- **PRE_RELEASE_CHECKLIST.md** - Verification
- **RELEASE_SUMMARY.md** - Build summary
- **CHANGELOG.md** - Version history

---

**Quick Links:**
- Build Config: `app/build.gradle.kts`
- ProGuard Rules: `app/proguard-rules.pro`
- Gradle Properties: `gradle.properties`
- Main Branch: All PRs merged ✅

**Status:** ✅ READY TO BUILD

---

Last Updated: 2024
