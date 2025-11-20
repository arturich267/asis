# Virtual Companion - Release Build Guide

## Release Information

**Application Name:** Virtual Companion (Виртуальный собеседник)  
**Package Name:** com.asis.virtualcompanion  
**Version:** 1.0 (Build 1)  
**Min SDK:** 23 (Android 6.0 Marshmallow)  
**Target SDK:** 34 (Android 14)  

## Building the Release APK

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Android SDK with Build Tools
- Gradle 8.x (included via wrapper)

### Build Commands

1. **Clean the project:**
   ```bash
   ./gradlew clean
   ```

2. **Build the release APK:**
   ```bash
   ./gradlew assembleRelease
   ```

3. **Build and run all checks:**
   ```bash
   ./gradlew build
   ```

### Output Location

The generated APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

Or with custom naming:
```
app/build/outputs/apk/release/VirtualCompanion-v1.0-release.apk
```

### APK Size

Expected APK size: **50-100 MB**
- Core app: ~15-20 MB
- FFmpeg Kit library: ~30-40 MB
- TensorFlow Lite: ~5-10 MB
- Other dependencies: ~10-15 MB

## Installation Instructions

### For Developers (Debug Installation)

1. Enable "Unknown Sources" or "Install from Unknown Sources" in Android settings
2. Copy the APK to your device via USB or download link
3. Tap the APK file to install
4. Grant necessary permissions when prompted

### Via ADB (Android Debug Bridge)

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

Or to replace existing installation:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Required Permissions

The app requires the following permissions:

### Essential Permissions:
- **RECORD_AUDIO** - For voice interaction feature
- **READ_MEDIA_AUDIO** - To access audio files (Android 13+)
- **READ_MEDIA_IMAGES** - To access image files (Android 13+)
- **READ_MEDIA_VIDEO** - To access video files (Android 13+)
- **READ_EXTERNAL_STORAGE** - To access files on older Android versions
- **ACCESS_MEDIA_LOCATION** - To read media location metadata

### Optional Permissions:
- **INTERNET** - For future online features (currently not used)
- **WRITE_EXTERNAL_STORAGE** - For saving generated content

## Features

### Core Features:
1. **Voice Interaction** - Record voice messages and receive text-to-speech responses
2. **Chat Interface** - Text-based conversation with the virtual companion
3. **Meme Generation** - Context-aware meme responses based on conversation
4. **Custom Backgrounds** - Personalize the app with custom background images
5. **Privacy Controls** - Manage data retention and voice recordings
6. **Settings** - Customize app behavior and appearance

### Technical Features:
- **Room Database** - Local data persistence for chats and messages
- **TensorFlow Lite** - AI-powered response generation
- **FFmpeg Integration** - Advanced audio processing
- **Material Design 3** - Modern UI with dynamic theming
- **Dark Mode Support** - System-wide dark mode compatibility

## Build Configuration

### ProGuard/R8 Optimization

The release build includes:
- **Code Obfuscation** - Protects against reverse engineering
- **Resource Shrinking** - Reduces APK size by removing unused resources
- **Code Optimization** - Improves runtime performance

### Signing Configuration

The release APK is signed with:
- **Keystore:** Android Debug Keystore (for development)
- **Key Alias:** androiddebugkey
- **Validity:** 10,000 days

**⚠️ Important:** For production release to Google Play Store, replace with a proper production keystore.

## Verification Checklist

Before distribution, verify:

- [ ] APK builds successfully without errors
- [ ] APK size is within acceptable range (50-100 MB)
- [ ] App installs on test device
- [ ] App launches without crashes
- [ ] All permissions are requested correctly
- [ ] Voice recording works
- [ ] Text-to-speech works
- [ ] Chat functionality works
- [ ] Settings are persistent
- [ ] Navigation flows correctly
- [ ] No memory leaks in critical paths
- [ ] ProGuard rules are correct (no runtime crashes)

## Troubleshooting

### Build Issues

**Issue:** Gradle build fails with "Android SDK not found"
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/android/sdk
```

**Issue:** Out of memory during build
```bash
# Increase Gradle memory in gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

**Issue:** ProGuard build errors
```bash
# Check proguard-rules.pro for missing keep rules
# Add -keep rules for classes causing issues
```

### Installation Issues

**Issue:** "App not installed" error
- Ensure you're not downgrading from a higher version code
- Uninstall existing version first
- Check device storage space

**Issue:** "Parse error" during installation
- APK may be corrupted - rebuild
- Device architecture may be incompatible
- Android version may be too old (requires Android 6.0+)

## Distribution

### Google Play Store Release

1. Generate a production keystore:
   ```bash
   keytool -genkey -v -keystore production-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias virtualcompanion-release
   ```

2. Update `app/build.gradle.kts` signing config to use production keystore

3. Build release APK with production signing:
   ```bash
   ./gradlew assembleRelease
   ```

4. Generate an App Bundle (recommended for Play Store):
   ```bash
   ./gradlew bundleRelease
   ```
   Output: `app/build/outputs/bundle/release/app-release.aab`

5. Upload to Google Play Console

### Direct Distribution (APK)

For direct distribution outside Google Play:
- Host the APK on a secure server
- Provide SHA-256 checksum for verification
- Include installation instructions
- Warn users about "Unknown Sources" setting

## Version History

### Version 1.0 (Build 1) - Initial Release
- ✅ Core chat functionality
- ✅ Voice interaction with recording and TTS
- ✅ Meme generation with TensorFlow Lite
- ✅ Custom backgrounds
- ✅ Privacy controls and data management
- ✅ Settings and theming
- ✅ Full test coverage

## Support

For issues, bugs, or feature requests, please contact the development team or file an issue in the project repository.

## License

Copyright © 2024 Asis Virtual Companion Team
All rights reserved.

---

**Last Updated:** 2024
**Build Configuration:** Release with R8 optimization and code shrinking
**Testing Status:** ✅ All tests passing
