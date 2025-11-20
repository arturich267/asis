#!/bin/bash

# Virtual Companion - GitHub Release Creation Script with APK Upload
# This script creates a GitHub release and uploads the APK
# 
# Requirements:
# - GitHub CLI (gh) installed
# - Authenticated with GitHub: GITHUB_TOKEN environment variable or gh auth login
# - Release APK already built: app/build/outputs/apk/release/app-release.apk
#
# Usage:
#   export GITHUB_TOKEN="your_token_here"
#   ./create-release-with-apk.sh
#   or
#   gh auth login
#   ./create-release-with-apk.sh

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

# Get repository info
REPO_OWNER="arturich267"
REPO_NAME="asis"
TAG="v1.0.0"
RELEASE_TITLE="Virtual Companion v1.0.0 - Final Release"

print_header "Virtual Companion - GitHub Release Creator (with APK)"

# Check if authenticated
if ! gh auth status &> /dev/null; then
    print_error "Not authenticated with GitHub"
    echo "Options:"
    echo "  1. Run: gh auth login"
    echo "  2. Or set: export GITHUB_TOKEN='your_token_here'"
    exit 1
fi

print_success "GitHub authentication verified"

# Check if APK exists
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    print_error "APK not found at $APK_PATH"
    print_info "Please build the APK first with: ./gradlew assembleRelease"
    exit 1
fi

# Get APK info
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
print_success "APK found at: $APK_PATH (Size: $APK_SIZE)"

# Check if tag exists, if not create it
if ! git rev-parse "$TAG" >/dev/null 2>&1; then
    print_info "Creating git tag: $TAG"
    git tag -a "$TAG" -m "$RELEASE_TITLE" HEAD
    git push origin "$TAG"
    print_success "Tag created and pushed"
else
    print_info "Tag $TAG already exists"
fi

# Check if release already exists
if gh release view "$TAG" --repo "$REPO_OWNER/$REPO_NAME" >/dev/null 2>&1; then
    print_warning "Release $TAG already exists on GitHub"
    
    read -p "Do you want to delete the existing release and recreate it? (y/N): " -n 1 -r DELETE_CHOICE
    echo
    if [[ $DELETE_CHOICE =~ ^[Yy]$ ]]; then
        print_info "Deleting existing release..."
        gh release delete "$TAG" --repo "$REPO_OWNER/$REPO_NAME" --yes
        print_success "Release deleted"
    else
        print_info "Skipping release creation"
        print_success "Release already exists at: https://github.com/$REPO_OWNER/$REPO_NAME/releases/tag/$TAG"
        exit 0
    fi
fi

print_info "Creating GitHub Release..."

# Create release with content from GITHUB_RELEASE.md
if [ -f "GITHUB_RELEASE.md" ]; then
    print_info "Using GITHUB_RELEASE.md for release notes"
    gh release create "$TAG" \
        "$APK_PATH" \
        --repo "$REPO_OWNER/$REPO_NAME" \
        --title "$RELEASE_TITLE" \
        --notes-file GITHUB_RELEASE.md \
        --draft=false
else
    print_warning "GITHUB_RELEASE.md not found, using generic notes"
    gh release create "$TAG" \
        "$APK_PATH" \
        --repo "$REPO_OWNER/$REPO_NAME" \
        --title "$RELEASE_TITLE" \
        --notes "Virtual Companion v1.0.0 - Final Release

## Финальная версия приложения \"Виртуальный собеседник\"

🎉 Возможности:
✅ Импорт архивов WhatsApp
✅ Генерация мемных ответов
✅ Текстовый чат с историей
✅ Голосовое взаимодействие с TTS
✅ Управление приватностью
✅ Кастомизация тем

📱 Требования:
- Android 5.2+ (minSdk 23)
- ~100 MB памяти
- Разрешения: Хранилище + Микрофон

📥 Установка:
1. Скачайте app-release.apk
2. Разрешите установку из неизвестных источников
3. Откройте файл и нажмите Install

APK Details:
- File: app-release.apk
- Size: $APK_SIZE
- Minimum Android: 6.0 (API 23)

For more information, visit: https://github.com/arturich267/asis" \
        --draft=false
fi

print_success "Release created successfully!"
echo ""
echo "Release URL: https://github.com/$REPO_OWNER/$REPO_NAME/releases/tag/$TAG"
echo "Direct APK Download: https://github.com/$REPO_OWNER/$REPO_NAME/releases/download/$TAG/app-release.apk"
echo ""

print_info "Release Details:"
echo "  - Version: 1.0.0"
echo "  - Tag: $TAG"
echo "  - APK File: app-release.apk"
echo "  - Size: $APK_SIZE"
echo "  - Status: Published"
echo ""

print_success "All done! Your app is ready for download."
echo ""
echo "Verify the release:"
echo "  https://github.com/$REPO_OWNER/$REPO_NAME/releases"
