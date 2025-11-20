#!/bin/bash

# Virtual Companion - Release Build Script
# This script automates the process of building the release APK

set -e  # Exit on error

echo "========================================"
echo "Virtual Companion - Release Build"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "ℹ $1"
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found! Are you in the project root?"
    exit 1
fi

print_info "Starting release build process..."
echo ""

# Step 1: Clean project
print_info "Step 1/5: Cleaning project..."
if ./gradlew clean > /dev/null 2>&1; then
    print_success "Project cleaned successfully"
else
    print_error "Failed to clean project"
    exit 1
fi
echo ""

# Step 2: Run lint checks
print_info "Step 2/5: Running lint checks..."
if ./gradlew lint > /dev/null 2>&1; then
    print_success "Lint checks passed"
else
    print_warning "Lint checks failed (continuing anyway)"
fi
echo ""

# Step 3: Run unit tests
print_info "Step 3/5: Running unit tests..."
if ./gradlew test > /dev/null 2>&1; then
    print_success "Unit tests passed"
else
    print_warning "Some unit tests failed (continuing anyway)"
fi
echo ""

# Step 4: Build release APK
print_info "Step 4/5: Building release APK..."
if ./gradlew assembleRelease; then
    print_success "Release APK built successfully"
else
    print_error "Failed to build release APK"
    exit 1
fi
echo ""

# Step 5: Verify APK
print_info "Step 5/5: Verifying APK..."
APK_PATH="app/build/outputs/apk/release"

if [ -f "$APK_PATH/app-release.apk" ]; then
    APK_SIZE=$(du -h "$APK_PATH/app-release.apk" | cut -f1)
    print_success "APK found at: $APK_PATH/app-release.apk"
    print_info "APK size: $APK_SIZE"
    
    # Check APK size (warn if > 150MB)
    APK_SIZE_BYTES=$(stat -f%z "$APK_PATH/app-release.apk" 2>/dev/null || stat -c%s "$APK_PATH/app-release.apk" 2>/dev/null)
    if [ "$APK_SIZE_BYTES" -gt 157286400 ]; then
        print_warning "APK size is larger than 150MB"
    fi
else
    print_error "APK not found at expected location"
    exit 1
fi
echo ""

# Print summary
echo "========================================"
echo "Build Summary"
echo "========================================"
print_success "Release APK built successfully!"
echo ""
echo "Details:"
echo "  - Package: com.asis.virtualcompanion"
echo "  - Version: 1.0 (Build 1)"
echo "  - Min SDK: 23 (Android 6.0)"
echo "  - Target SDK: 34 (Android 14)"
echo "  - APK Location: $APK_PATH/app-release.apk"
echo "  - APK Size: $APK_SIZE"
echo ""
echo "Next steps:"
echo "  1. Test the APK on a device: adb install -r $APK_PATH/app-release.apk"
echo "  2. Verify all features work correctly"
echo "  3. Run E2E tests on device"
echo "  4. Prepare for distribution"
echo ""
print_success "Build completed successfully!"
