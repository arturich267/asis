# Asis Virtual Companion 🤖💬

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

**Виртуальный собеседник** - An AI-powered virtual companion Android application with voice interaction, intelligent chat, and privacy-focused local data management.

---

## 📱 Features

### Core Functionality
- 💬 **Intelligent Chat** - Context-aware text conversation with AI-powered responses
- 🎤 **Voice Interaction** - Record voice messages and receive text-to-speech responses
- 🤖 **Meme Generator** - AI-driven contextual and emotion-aware response generation
- 🎨 **Custom Backgrounds** - Personalize your experience with custom images
- ⚙️ **Flexible Settings** - Comprehensive configuration options
- 🔒 **Privacy Controls** - Complete control over your data with local-only storage

### Technical Highlights
- **Offline-First** - All processing done locally, no internet required
- **AI-Powered** - TensorFlow Lite for intelligent response generation
- **Modern Architecture** - MVVM pattern with Repository and Room database
- **Material Design 3** - Beautiful, adaptive UI with dark mode support
- **Privacy-Focused** - No data leaves your device

---

## 🏗️ Architecture

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/asis/virtualcompanion/
│   │   │   ├── data/              # Data layer
│   │   │   │   ├── database/      # Room database
│   │   │   │   ├── repository/    # Repository implementations
│   │   │   │   └── datastore/     # DataStore preferences
│   │   │   ├── domain/            # Domain layer
│   │   │   │   ├── model/         # Domain models
│   │   │   │   ├── repository/    # Repository interfaces
│   │   │   │   └── usecase/       # Use cases
│   │   │   ├── ui/                # UI layer
│   │   │   │   ├── chat/          # Chat feature
│   │   │   │   ├── home/          # Home screen
│   │   │   │   ├── permissions/   # Permissions
│   │   │   │   ├── settings/      # Settings
│   │   │   │   └── voice/         # Voice interaction
│   │   │   └── util/              # Utilities
│   │   └── res/                   # Resources
│   └── test/                      # Unit tests
│   └── androidTest/               # Instrumentation tests
```

### Design Pattern
- **MVVM** - Model-View-ViewModel architecture
- **Repository Pattern** - Clean separation of data sources
- **Single Activity** - Navigation Component for fragment navigation
- **Reactive** - LiveData and Flow for reactive updates

### Tech Stack
- **Language:** Kotlin 1.9.10
- **UI:** Material Design 3, ViewBinding, ConstraintLayout
- **Architecture:** ViewModel, LiveData, Navigation Component
- **Database:** Room 2.6.1
- **Preferences:** DataStore
- **Async:** Kotlin Coroutines 1.7.3
- **DI:** Manual dependency injection (AppModule)
- **AI/ML:** TensorFlow Lite 2.14.0
- **Media:** FFmpeg Kit 6.0-2
- **Testing:** JUnit, Mockito, Espresso, Robolectric

---

## 📥 Download & Install

### For End Users

👉 **[Download Virtual Companion v1.0.0 APK](https://github.com/arturich267/asis-virtual-companion/releases/download/v1.0.0/app-release.apk)**

**Quick Start:**
1. Download the APK file
2. Enable "Install from Unknown Sources" in Android settings
3. Tap the APK to install
4. Grant permissions and start chatting!

For detailed instructions, see [INSTALL.md](INSTALL.md)

### Release Information
- **Version:** 1.0.0 (Stable)
- **File:** app-release.apk
- **Size:** ~50-100 MB
- **Minimum Android:** 6.0 (API 23)
- **Status:** Production Ready ✅

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Android SDK API 34
- Gradle 8.x (included via wrapper)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/arturich267/asis-virtual-companion.git
   cd asis-virtual-companion
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync Gradle:**
   - Wait for Gradle sync to complete
   - Download dependencies automatically

4. **Run the app:**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10

### Building Release APK

#### Quick Build
```bash
./gradlew clean
./gradlew assembleRelease
```

#### Using Build Script
```bash
./build-release.sh
```

#### Output Location
```
app/build/outputs/apk/release/VirtualCompanion-v1.0-release.apk
```

For detailed build instructions, see [RELEASE.md](RELEASE.md)

---

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Android Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test
```bash
./gradlew test --tests "com.asis.virtualcompanion.ui.chat.ChatViewModelTest"
```

### Test Coverage
- **Unit Tests:** ViewModels, Repositories, Domain logic
- **Integration Tests:** Database, End-to-end flows
- **UI Tests:** Fragment interactions, Navigation

---

## 📦 Dependencies

### Core Android
- AndroidX Core KTX 1.12.0
- AppCompat 1.6.1
- Material Design 3 (1.11.0)
- ConstraintLayout 2.1.4

### Architecture Components
- Lifecycle ViewModel 2.7.0
- LiveData 2.7.0
- Navigation 2.7.6
- Room 2.6.1

### Kotlin & Coroutines
- Kotlin 1.9.10
- Coroutines 1.7.3

### AI & Media
- TensorFlow Lite 2.14.0
- FFmpeg Kit 6.0-2 (optional - currently stubbed)

### Storage
- DataStore Preferences 1.0.0
- Gson 2.10.1

### Testing
- JUnit 4.13.2
- Mockito 5.8.0
- Espresso 3.5.1
- Robolectric 4.11.1

For complete dependency list, see [build.gradle.kts](app/build.gradle.kts)

---

## 🔧 FFmpeg Audio Service (Stubbed)

The FFmpeg audio service is currently **disabled and stubbed** to keep the build lean. The service provides APIs for:
- **trimAudio** - Trim audio to a specific duration
- **mixAudio** - Mix multiple audio tracks
- **concatenateAudio** - Concatenate multiple audio files
- **convertAudio** - Convert audio format and sample rate
- **getAudioDuration** - Get audio file duration

### Status
All methods currently return `Result.Error` with a descriptive message indicating the service is not available.

### Enabling FFmpeg Support

To restore full FFmpeg functionality:

1. **Uncomment the dependency** in `app/build.gradle.kts`:
   ```gradle
   implementation("com.arthenica:ffmpeg-kit-min:6.0")
   ```

2. **Restore FFmpeg imports** in `app/src/main/java/com/asis/virtualcompanion/domain/service/FFmpegAudioService.kt`:
   ```kotlin
   import com.arthenica.ffmpegkit.FFmpegKit
   import com.arthenica.ffmpegkit.ReturnCode
   ```

3. **Replace stub implementations** with actual FFmpeg commands

4. **Rebuild the project**:
   ```bash
   ./gradlew clean assembleDebug
   ```

See [FFmpegAudioService.kt](app/src/main/java/com/asis/virtualcompanion/domain/service/FFmpegAudioService.kt) for detailed instructions.

---

## 🔒 Permissions

The app requires the following permissions:

### Essential
- `RECORD_AUDIO` - Voice recording
- `READ_MEDIA_AUDIO` - Audio file access (Android 13+)
- `READ_MEDIA_IMAGES` - Image file access (Android 13+)
- `READ_MEDIA_VIDEO` - Video file access (Android 13+)
- `READ_EXTERNAL_STORAGE` - File access (Android 6-12)
- `ACCESS_MEDIA_LOCATION` - Media metadata

### Optional
- `INTERNET` - Future online features
- `WRITE_EXTERNAL_STORAGE` - Save content (Android ≤28)

---

## 📚 Documentation

- [**RELEASE.md**](RELEASE.md) - Comprehensive release build guide
- [**INSTALL_GUIDE.md**](INSTALL_GUIDE.md) - End-user installation instructions
- [**RELEASE_SUMMARY.md**](RELEASE_SUMMARY.md) - Build status and summary
- [**PRE_RELEASE_CHECKLIST.md**](PRE_RELEASE_CHECKLIST.md) - Pre-release verification
- [**CHANGELOG.md**](CHANGELOG.md) - Version history and changes

---

## 🎯 Roadmap

### Version 1.0 (Current)
- ✅ Chat interface with AI responses
- ✅ Voice interaction with TTS
- ✅ Meme generation engine
- ✅ Privacy controls
- ✅ Custom backgrounds
- ✅ Settings management

### Version 1.1 (Planned)
- [ ] WhatsApp archive import
- [ ] Enhanced AI model
- [ ] Multiple companion personalities
- [ ] Conversation export
- [ ] Widget support

### Version 2.0 (Future)
- [ ] Multi-language support
- [ ] Cloud backup (optional)
- [ ] Voice commands
- [ ] Advanced analytics
- [ ] Customizable UI themes

---

## 🐛 Known Issues

1. **APK Size** - Large due to FFmpeg Kit (~40MB)
2. **TensorFlow Lite Model** - Optional model file not included (fallback works)
3. **WhatsApp Import** - Feature prepared but not fully implemented

For all issues, see [GitHub Issues](https://github.com/arturich267/asis-virtual-companion/issues)

---

## 🤝 Contributing

Contributions are currently not being accepted as this is a private project. However, feedback and bug reports are welcome!

### Reporting Bugs
1. Check if the issue already exists
2. Create a new issue with detailed description
3. Include steps to reproduce
4. Attach logs if applicable

---

## 📄 License

Copyright © 2024 Asis Virtual Companion Team. All rights reserved.

This project is proprietary software. Unauthorized copying, distribution, or modification is prohibited.

---

## 👥 Team

**Development Team:** Asis Virtual Companion Team  
**Repository:** [github.com/arturich267/asis-virtual-companion](https://github.com/arturich267/asis-virtual-companion)

---

## 📞 Support

For questions, issues, or feedback:
- **GitHub Issues:** [Create an issue](https://github.com/arturich267/asis-virtual-companion/issues)
- **Email:** TBD

---

## 🙏 Acknowledgments

- Material Design 3 by Google
- TensorFlow Lite team
- FFmpeg Kit contributors
- Android developer community
- All open source library maintainers

---

## 📱 Screenshots

> *Screenshots will be added in future updates*

---

## ⭐ Star History

If you find this project useful, please consider giving it a star!

---

**Made with ❤️ for Android developers who value privacy and local-first applications**
