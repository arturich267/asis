# Testing Quick Reference Guide
## Virtual Companion Android App

---

## 🚀 Quick Start

### Run All Tests
```bash
# All unit tests (fast, no device needed)
./gradlew test

# All instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Everything
./gradlew test connectedAndroidTest
```

### Build & Install APK
```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or build and install in one step
./gradlew installDebug
```

---

## 📋 Test Checklists

### Pre-Commit Checklist
- [ ] `./gradlew test` (unit tests pass)
- [ ] `./gradlew compileDebugKotlin` (no compile errors)
- [ ] Code follows existing patterns

### Pre-Push Checklist
- [ ] `./gradlew test` (unit tests pass)
- [ ] `./gradlew connectedAndroidTest` (instrumentation tests pass)
- [ ] `./gradlew assembleDebug` (builds successfully)

### Pre-Release Checklist
- [ ] All automated tests pass
- [ ] Release APK builds successfully
- [ ] Manual testing completed (use `E2E_MANUAL_TESTING_GUIDE.md`)
- [ ] Test report filled out (use `E2E_TEST_REPORT_TEMPLATE.md`)
- [ ] No critical or major bugs
- [ ] Stakeholder approval

---

## 🧪 Test Suites

### Unit Tests (No Device Required)

| Suite | Command | Coverage |
|-------|---------|----------|
| All Unit Tests | `./gradlew test` | Everything |
| Domain Tests | `./gradlew test --tests "*.domain.*"` | MemeGenerator, Services |
| Data Tests | `./gradlew test --tests "*.data.*"` | Repositories, Models |
| UI Tests | `./gradlew test --tests "*.ui.*"` | ViewModels, Adapters |
| Worker Tests | `./gradlew test --tests "*.work.*"` | Import Workers |

### Instrumentation Tests (Requires Device)

| Suite | Command | Coverage |
|-------|---------|----------|
| All Instrumentation | `./gradlew connectedAndroidTest` | Everything |
| E2E Tests | `./gradlew connectedAndroidTest --tests "*e2e*"` | User Journeys |
| UI Tests | `./gradlew connectedAndroidTest --tests "*.ui.*"` | Fragment Tests |
| Database Tests | `./gradlew connectedAndroidTest --tests "*.database.*"` | DAO Tests |
| Home Tests | `./gradlew connectedAndroidTest --tests "*HomeFragmentTest"` | Home Screen |
| Settings Tests | `./gradlew connectedAndroidTest --tests "*Settings*Test"` | Settings |
| Chat Tests | `./gradlew connectedAndroidTest --tests "*Chat*Test"` | Chat |
| Voice Tests | `./gradlew connectedAndroidTest --tests "*Voice*Test"` | Voice |
| Permissions Tests | `./gradlew connectedAndroidTest --tests "*Permissions*Test"` | Permissions |

---

## 📱 Device Setup

### Prepare Device for Testing
```bash
# Check device is connected
adb devices

# Disable animations (recommended for stable tests)
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# Clear app data before testing
adb shell pm clear com.asis.virtualcompanion

# Uninstall app
adb uninstall com.asis.virtualcompanion
```

### Re-enable Animations (After Testing)
```bash
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
```

---

## 🔍 Debugging Tests

### View Test Results
```bash
# Open unit test report in browser
open app/build/reports/tests/testDebugUnitTest/index.html

# Open instrumentation test report
open app/build/reports/androidTests/connected/index.html
```

### Logcat During Tests
```bash
# Watch app logs
adb logcat | grep "com.asis.virtualcompanion"

# Watch test runner logs
adb logcat | grep "TestRunner"

# Watch errors only
adb logcat *:E | grep "com.asis.virtualcompanion"
```

### Run Single Test
```bash
# Run one specific test class
./gradlew test --tests "com.asis.virtualcompanion.domain.MemeGeneratorTest"

# Run one specific test method
./gradlew test --tests "com.asis.virtualcompanion.domain.MemeGeneratorTest.generateMemeResponse_withHighConfidence_returnsResponse"
```

---

## 📖 Test Documentation

### Main Documents
| Document | Purpose | When to Use |
|----------|---------|-------------|
| `E2E_TESTING_README.md` | Comprehensive testing guide | Reference for all testing info |
| `E2E_MANUAL_TESTING_GUIDE.md` | Step-by-step manual testing | Pre-release device testing |
| `E2E_TEST_REPORT_TEMPLATE.md` | Test results documentation | After manual testing session |
| `TESTING_QUICK_REFERENCE.md` | This file! | Quick command reference |

### Test Code Locations
| Component | Unit Tests | Instrumentation Tests |
|-----------|------------|----------------------|
| E2E | - | `ui/e2e/` |
| Home | `ui/main/HomeViewModelTest.kt` | `ui/main/HomeFragmentTest.kt` |
| Settings | - | `ui/settings/SettingsFragmentTest.kt` |
| Chat | `ui/chat/ChatViewModelTest.kt` | `ui/chat/ChatFragmentTest.kt` |
| Voice | - | `ui/voice/VoiceFragmentTest.kt` |
| Permissions | - | `ui/permissions/PermissionsFragmentTest.kt` |

---

## 🐛 Common Issues & Solutions

### Tests Won't Run
**Problem:** `No connected devices` error  
**Solution:**
```bash
adb devices  # Check device is connected
adb kill-server && adb start-server  # Restart ADB
```

**Problem:** `Installation failed` error  
**Solution:**
```bash
adb uninstall com.asis.virtualcompanion  # Uninstall first
./gradlew installDebug  # Reinstall
```

### Tests Fail Intermittently
**Problem:** Flaky tests  
**Solution:**
- Disable device animations (see Device Setup)
- Clear app data: `adb shell pm clear com.asis.virtualcompanion`
- Ensure device is unlocked during tests
- Check available storage space

### Tests Timeout
**Problem:** `Test timeout` error  
**Solution:**
- Increase timeout in test configuration
- Check for deadlocks in code
- Verify async operations complete properly

---

## 📊 Test Coverage

### Current Status
✅ **Unit Tests:** ~37 test classes  
✅ **Instrumentation Tests:** ~15 test classes  
✅ **Manual Tests:** Comprehensive checklist  

### Coverage by Component
| Component | Coverage | Status |
|-----------|----------|--------|
| MemeGenerator | ✅ High | Complete |
| Chat | ✅ High | Complete |
| Voice | ⚠️ Medium | UI tests added |
| Settings | ⚠️ Medium | UI tests added |
| Database | ✅ High | Complete |
| WhatsApp Import | ✅ High | Complete |
| Navigation | ⚠️ Medium | E2E tests added |

---

## 🎯 Testing Strategy

### Test Pyramid
```
         ⬆️
        / \
       /   \      Unit Tests (70%)
      /_____\     - Fast, isolated, no Android framework
     /       \
    /         \   Integration Tests (20%)
   /___________\  - Repository, database, worker tests
  /             \
 /               \ E2E Tests (10%)
/_________________\- Full user journey, UI interactions
```

### What to Test Where

**Unit Tests:**
- Business logic (MemeGenerator, parsers)
- ViewModels (state changes, events)
- Repositories (data operations)
- Services (audio, TFLite, NLP)

**Instrumentation Tests:**
- UI interactions (clicks, navigation)
- Database operations (DAOs)
- Fragment lifecycle
- Integration flows

**Manual Tests:**
- Real device behavior
- Permissions flow
- SAF file picker
- Audio recording/playback quality
- Performance under load
- Accessibility (TalkBack)

---

## 🚦 Test Status

### Before Release - ALL MUST PASS ✅

- [ ] All unit tests passing
- [ ] All instrumentation tests passing
- [ ] Manual testing completed
- [ ] No critical bugs
- [ ] Test report signed off
- [ ] APK builds successfully
- [ ] Performance acceptable
- [ ] Accessibility verified
- [ ] Stakeholder approval

---

## 📞 Need Help?

1. **Check full documentation:** `E2E_TESTING_README.md`
2. **Review manual guide:** `E2E_MANUAL_TESTING_GUIDE.md`
3. **Check test reports:** `app/build/reports/`
4. **View logcat:** `adb logcat`
5. **Create issue** in repository

---

## 📝 Quick Commands Cheat Sheet

```bash
# Testing
./gradlew test                          # Unit tests
./gradlew connectedAndroidTest          # Instrumentation tests
./gradlew test connectedAndroidTest     # All tests

# Building
./gradlew assembleDebug                 # Build debug APK
./gradlew assembleRelease               # Build release APK
./gradlew installDebug                  # Build + install debug

# Device Management
adb devices                             # List devices
adb install <path-to-apk>              # Install APK
adb uninstall com.asis.virtualcompanion # Uninstall app
adb shell pm clear com.asis.virtualcompanion # Clear app data

# Debugging
adb logcat | grep "com.asis"           # View app logs
adb logcat *:E                         # Errors only
adb shell dumpsys meminfo com.asis.virtualcompanion # Memory usage

# Test Specific
./gradlew test --tests "*Test"         # Run specific test
./gradlew connectedAndroidTest --tests "*e2e*" # E2E tests only
```

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Quick Reference for:** Virtual Companion Android App Testing
