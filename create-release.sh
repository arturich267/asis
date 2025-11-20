#!/bin/bash

# Virtual Companion - GitHub Release Creation Script
# This script creates a GitHub release with the APK file
# 
# Requirements:
# - GitHub CLI (gh) installed: https://cli.github.com/
# - Authenticated with GitHub: gh auth login
# - Release APK already built: ./gradlew assembleRelease
#
# Usage:
#   ./create-release.sh                    # Create release from current directory
#   ./create-release.sh --publish          # Create and publish release

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
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
    echo -e "${BLUE}ℹ $1${NC}"
}

print_header() {
    echo ""
    echo "========================================"
    echo "$1"
    echo "========================================"
    echo ""
}

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    print_error "GitHub CLI (gh) is not installed"
    echo "Install from: https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    print_error "Not authenticated with GitHub"
    echo "Run: gh auth login"
    exit 1
fi

print_header "Virtual Companion - GitHub Release Creator"

# Check if tag exists
TAG="v1.0.0"
if ! git rev-parse "$TAG" >/dev/null 2>&1; then
    print_info "Creating git tag: $TAG"
    git tag -a "$TAG" -m "Virtual Companion v1.0.0 - Final Release" HEAD
    git push origin "$TAG"
    print_success "Tag created and pushed"
else
    print_info "Tag $TAG already exists"
fi

# Check if APK exists
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    print_warning "APK not found at $APK_PATH"
    print_info "Building release APK..."
    
    if [ ! -f "./gradlew" ]; then
        print_error "gradlew not found! Are you in the project root?"
        exit 1
    fi
    
    ./gradlew clean assembleRelease
fi

# Verify APK exists
if [ ! -f "$APK_PATH" ]; then
    print_error "Failed to build APK"
    exit 1
fi

# Get APK info
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
print_success "APK found at: $APK_PATH (Size: $APK_SIZE)"

# Check if release already exists
if gh release view "$TAG" >/dev/null 2>&1; then
    print_warning "Release $TAG already exists on GitHub"
    
    read -p "Do you want to delete the existing release? (y/N): " -n 1 -r DELETE_CHOICE
    echo
    if [[ $DELETE_CHOICE =~ ^[Yy]$ ]]; then
        print_info "Deleting existing release..."
        gh release delete "$TAG" --yes
        print_success "Release deleted"
    else
        print_info "Skipping release creation"
        exit 0
    fi
fi

print_info "Creating GitHub Release..."

# Create release with content from GITHUB_RELEASE.md
if [ -f "GITHUB_RELEASE.md" ]; then
    print_info "Using GITHUB_RELEASE.md for release notes"
    gh release create "$TAG" \
        "$APK_PATH" \
        --title "Virtual Companion v1.0.0 - Final Release" \
        --notes-file GITHUB_RELEASE.md \
        --draft=false
else
    print_warning "GITHUB_RELEASE.md not found, using generic notes"
    gh release create "$TAG" \
        "$APK_PATH" \
        --title "Virtual Companion v1.0.0 - Final Release" \
        --notes "Virtual Companion v1.0.0 - Final Release

APK Details:
- File: app-release.apk
- Size: $APK_SIZE
- Minimum Android: 6.0 (API 23)

For installation instructions, see INSTALL.md in the repository.

Features:
- 💬 Intelligent Chat
- 🎤 Voice Interaction
- 🤖 AI-Powered Meme Generator
- 🎨 Customizable UI
- 🔒 Privacy-First Design

All data stored locally. No internet required.

For more information, visit: https://github.com/arturich267/asis-virtual-companion" \
        --draft=false
fi

print_success "Release created successfully!"
echo ""
echo "Release URL: https://github.com/arturich267/asis-virtual-companion/releases/tag/$TAG"
echo "Direct APK Download: https://github.com/arturich267/asis-virtual-companion/releases/download/$TAG/app-release.apk"
echo ""

print_info "Release Details:"
echo "  - Version: 1.0.0"
echo "  - Tag: $TAG"
echo "  - APK File: app-release.apk"
echo "  - Size: $APK_SIZE"
echo "  - Status: Published"
echo ""

print_success "All done! Your app is ready for download."
