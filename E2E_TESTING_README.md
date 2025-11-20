# End-to-End Testing Documentation
## Virtual Companion Android App

---

## Overview

This document describes the comprehensive E2E testing strategy for the Virtual Companion Android app. Testing is divided into **automated tests** (run via CI/CD or locally) and **manual tests** (performed on physical devices).

---

## Test Structure

### Automated Tests (`app/src/androidTest/`)

#### 1. **E2E Navigation Tests**
**Location:** `ui/e2e/AppNavigationE2ETest.kt`

Tests the complete navigation flow:
- Permissions → Home flow
- Home → Settings, Chat, Voice navigation
- Configuration changes (rotation)
- Accessibility (content descriptions)

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*AppNavigationE2ETest"
```

#### 2. **Settings Fragment Tests**
**Location:** `ui/settings/SettingsFragmentTest.kt`

Tests all Settings screen functionality:
- UI element visibility
- Archive picker button
- Theme selection
- Toggle switches (Real Voice, Retain Voice, Process Offline)
- Privacy Policy navigation
- Clear Data dialog
- Configuration changes

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*SettingsFragmentTest"
```

#### 3. **Voice Fragment Tests**
**Location:** `ui/voice/VoiceFragmentTest.kt`

Tests Voice interaction screen:
- UI elements display
- Mic button interaction
- Mode toggle functionality
- Recording visualization
- Playback controls
- Error states
- Configuration changes

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*VoiceFragmentTest"
```

#### 4. **Chat Tests**
**Location:** `ui/chat/ChatFragmentTest.kt`, `ChatIntegrationTest.kt`, `ChatPersistenceTest.kt`

Tests chat functionality:
- Message sending/receiving
- RecyclerView adapter
- Message persistence
- Auto-scroll behavior
- History loading

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*Chat*Test"
```

#### 5. **Permissions Tests**
**Location:** `ui/permissions/PermissionsFragmentTest.kt`

Tests permissions flow:
- UI display
- Grant button functionality
- Permission explanations

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*PermissionsFragmentTest"
```

#### 6. **Database Tests**
**Location:** `data/database/*Test.kt`

Tests all Room database operations:
- DAO operations
- Schema validation
- Migrations
- Seed data

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*Dao*Test"
```

#### 7. **Repository Integration Tests**
**Location:** `data/repository/*Test.kt`

Tests repository layer:
- Data operations
- Flow emissions
- Error handling

**Run command:**
```bash
./gradlew connectedAndroidTest --tests "*RepositoryTest"
```

---

### Unit Tests (`app/src/test/`)

#### Domain Layer Tests
- `MemeGeneratorTest` - Meme generation logic
- `TensorFlowLiteStyleClassifierTest` - AI style classification
- `FFmpegAudioServiceTest` - Audio processing
- `EmotionAnalysisServiceTest` - Emotion detection
- `AudioRecordingServiceTest` - Recording service

#### Data Layer Tests
- Repository tests (chat, messages, phrases, voice)
- Model tests (data classes, validation)

#### UI Layer Tests
- ViewModel tests (HomeViewModel, ChatViewModel)
- Adapter tests (ChatMessageAdapter)

#### Worker Tests
- `ImportArchiveWorkerTest` - WhatsApp import worker
- `WhatsAppChatParserTest` - Chat parsing logic
- `NLPAnalyticsProcessorTest` - NLP analysis

**Run all unit tests:**
```bash
./gradlew test
```

---

## Running Automated Tests

### All Automated Tests
```bash
# Run all unit tests
./gradlew test

# Run all instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run both
./gradlew test connectedAndroidTest
```

### Specific Test Suites
```bash
# E2E navigation tests only
./gradlew connectedAndroidTest --tests "*e2e*"

# Settings tests only
./gradlew connectedAndroidTest --tests "*SettingsFragmentTest"

# Voice tests only
./gradlew connectedAndroidTest --tests "*VoiceFragmentTest"

# All UI tests
./gradlew connectedAndroidTest --tests "*.ui.*"
```

### Test Reports
After running tests, reports are available at:
- **Unit tests:** `app/build/reports/tests/testDebugUnitTest/index.html`
- **Instrumentation tests:** `app/build/reports/androidTests/connected/index.html`

---

## Manual Testing

### Prerequisites
1. **Physical Android device** or **Emulator** with API 23+
2. **Test WhatsApp archive** (sample data)
3. **Microphone** enabled on device
4. **Storage permissions** available

### Test Guides

#### 1. **Complete Manual Testing Guide**
**Location:** `E2E_MANUAL_TESTING_GUIDE.md`

Comprehensive checklist covering:
- A. Permissions Screen
- B. Home Screen
- C. Settings Screen
- D. WhatsApp Archive Import
- E. Chat Screen (Meme Chat)
- F. Voice Interaction
- G. Privacy Policy
- H. Clear All Data
- I. Navigation & Back Button
- J. Performance & Stability
- K. Orientation & Adaptivity
- L. Accessibility
- M. Error & Edge Cases
- N. Logging & Debug

**Use this guide for:**
- Pre-release testing
- QA validation
- User acceptance testing
- Device compatibility testing

#### 2. **Test Report Template**
**Location:** `E2E_TEST_REPORT_TEMPLATE.md`

Use this template to document test results:
- Test session information
- Detailed test results (pass/fail)
- Issues found (critical, major, minor)
- UX assessment
- Final recommendation
- Sign-off

**Fill out after:**
- Each manual testing session
- Pre-release validation
- Beta testing feedback

---

## Building Test APK

### Debug Build (for testing)
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (for final testing)
```bash
./gradlew assembleRelease
```
APK location: `app/build/outputs/apk/release/app-release.apk`

### Install on Device
```bash
# Install debug build
adb install app/build/outputs/apk/debug/app-debug.apk

# Install release build
adb install app/build/outputs/apk/release/app-release.apk

# Reinstall (keep data)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Testing Workflow

### 1. **Pre-Commit Testing**
Before committing code:
```bash
# Run unit tests
./gradlew test

# Run static analysis
./gradlew lint

# Run type checking
./gradlew compileDebugKotlin
```

### 2. **Pre-Push Testing**
Before pushing to repository:
```bash
# Run all tests
./gradlew test connectedAndroidTest

# Build debug APK to ensure it compiles
./gradlew assembleDebug
```

### 3. **Pre-Release Testing**
Before releasing to production:
1. ✅ Run all automated tests (`./gradlew test connectedAndroidTest`)
2. ✅ Build release APK (`./gradlew assembleRelease`)
3. ✅ Install on physical device
4. ✅ Execute full manual testing (use `E2E_MANUAL_TESTING_GUIDE.md`)
5. ✅ Document results (use `E2E_TEST_REPORT_TEMPLATE.md`)
6. ✅ Fix any critical or major issues
7. ✅ Re-test after fixes
8. ✅ Get sign-off from QA/stakeholders
9. ✅ Release to Google Play Store

---

## Test Coverage

### Current Coverage

| Component | Unit Tests | Integration Tests | UI Tests | Manual Tests |
|-----------|------------|-------------------|----------|--------------|
| Permissions | ✅ | ✅ | ✅ | ✅ |
| Home Screen | ✅ | ✅ | ✅ | ✅ |
| Settings | ✅ | ✅ | ✅ | ✅ |
| Chat | ✅ | ✅ | ✅ | ✅ |
| Voice | ✅ | ✅ | ✅ | ✅ |
| WhatsApp Import | ✅ | ✅ | ⚠️ | ✅ |
| Meme Generator | ✅ | ✅ | N/A | ✅ |
| Database | ✅ | ✅ | N/A | N/A |
| Navigation | ⚠️ | ⚠️ | ✅ | ✅ |
| Privacy Policy | ⚠️ | ✅ | ✅ | ✅ |

**Legend:**
- ✅ Comprehensive coverage
- ⚠️ Partial coverage
- ❌ No coverage
- N/A Not applicable

---

## Known Testing Limitations

### What Cannot Be Fully Automated

1. **System Permissions**
   - Granting/denying permissions requires manual interaction
   - Automated tests use mocks or assume permissions granted

2. **Storage Access Framework (SAF)**
   - File picker cannot be automated
   - Tests mock file selection

3. **Actual Audio Recording**
   - Cannot test real microphone input in automated tests
   - Tests mock audio data

4. **TTS Playback Quality**
   - Audio quality assessment requires human judgment
   - Automated tests verify playback starts/stops

5. **Performance Profiling**
   - Memory leaks, ANRs, jank require manual inspection
   - Use Android Studio Profiler

6. **Accessibility (TalkBack)**
   - TalkBack interaction requires manual testing
   - Automated tests verify content descriptions exist

7. **Multi-Device Testing**
   - Different screen sizes, Android versions, manufacturers
   - Requires manual testing on diverse devices

---

## Troubleshooting Tests

### Tests Not Running
```bash
# Ensure emulator/device is connected
adb devices

# Ensure app is installed
adb shell pm list packages | grep virtualcompanion

# Clear app data before testing
adb shell pm clear com.asis.virtualcompanion
```

### Tests Failing
1. **Check logcat:**
   ```bash
   adb logcat | grep "TestRunner"
   ```

2. **Check test reports:**
   - Unit: `app/build/reports/tests/testDebugUnitTest/index.html`
   - Instrumentation: `app/build/reports/androidTests/connected/index.html`

3. **Common issues:**
   - Device locked during test
   - Insufficient storage
   - Wrong API level
   - Network issues (if applicable)

### Test Flakiness
If tests fail intermittently:
1. Add delays/waits for async operations
2. Use Espresso idling resources
3. Increase timeout values
4. Disable animations on test device:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

---

## CI/CD Integration

### GitHub Actions / GitLab CI
Example workflow for automated testing:

```yaml
name: Android Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          
      - name: Run unit tests
        run: ./gradlew test
        
      - name: Build debug APK
        run: ./gradlew assembleDebug
        
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: app/build/reports/
```

For instrumentation tests, add:
```yaml
      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedAndroidTest
```

---

## Test Data

### Sample WhatsApp Archive
For testing imports, create a sample archive with:
- 50+ messages
- Various message types (text, emoji, slang)
- Voice notes (if available)
- Proper WhatsApp format

Structure:
```
test_archive.zip
├── _chat.txt
└── Media/
    └── WhatsApp Voice Notes/
        └── PTT-*.opus
```

---

## Contributing to Tests

### Adding New Tests

1. **Unit Tests:**
   - Place in `app/src/test/java/com/asis/virtualcompanion/`
   - Follow AAA pattern (Arrange-Act-Assert)
   - Mock dependencies
   - Test edge cases

2. **Instrumentation Tests:**
   - Place in `app/src/androidTest/java/com/asis/virtualcompanion/`
   - Use Espresso for UI testing
   - Use `launchFragmentInContainer` for fragments
   - Test with real Android framework

3. **Test Naming:**
   - Format: `methodName_condition_expectedResult`
   - Example: `sendMessage_withEmptyText_showsError`

4. **Documentation:**
   - Add comments for complex test logic
   - Update this README if adding new test suites
   - Update manual testing guide if applicable

---

## Resources

### Documentation
- [Android Testing Guide](https://developer.android.com/training/testing)
- [Espresso Documentation](https://developer.android.com/training/testing/espresso)
- [JUnit4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)

### Tools
- **Android Studio Profiler** - Memory, CPU, network profiling
- **Layout Inspector** - UI hierarchy inspection
- **Logcat** - Runtime log monitoring
- **ADB** - Device communication

---

## Contact

For questions or issues with testing:
- Create an issue in the repository
- Contact the development team
- Refer to the main README.md

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Maintained By:** Virtual Companion Development Team
