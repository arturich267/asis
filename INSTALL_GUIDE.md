# Virtual Companion - Installation Guide

## Руководство по установке / Installation Guide

### 📱 For Android Users

---

## System Requirements / Системные требования

- **Android Version / Версия Android:** 6.0 (Marshmallow) or higher / или выше
- **Storage Space / Свободное место:** Minimum 150 MB / Минимум 150 МБ
- **RAM / Оперативная память:** 2 GB or more / 2 ГБ или больше
- **Permissions / Разрешения:** Audio recording, Media access / Запись аудио, Доступ к медиа

---

## Installation Steps / Шаги установки

### Method 1: Install from APK File / Способ 1: Установка из APK файла

#### Step 1: Enable Unknown Sources / Шаг 1: Разрешить установку из неизвестных источников

**For Android 8.0+ / Для Android 8.0+:**
1. Open **Settings** / Откройте **Настройки**
2. Go to **Apps & notifications** / Перейдите в **Приложения и уведомления**
3. Tap **Advanced** → **Special app access** / Нажмите **Дополнительно** → **Специальный доступ**
4. Tap **Install unknown apps** / Нажмите **Установка неизвестных приложений**
5. Select your browser or file manager / Выберите браузер или файловый менеджер
6. Enable **Allow from this source** / Включите **Разрешить из этого источника**

**For Android 7.x and below / Для Android 7.x и ниже:**
1. Open **Settings** / Откройте **Настройки**
2. Go to **Security** / Перейдите в **Безопасность**
3. Enable **Unknown sources** / Включите **Неизвестные источники**
4. Tap **OK** to confirm / Нажмите **ОК** для подтверждения

#### Step 2: Download the APK / Шаг 2: Загрузите APK

1. Download `VirtualCompanion-v1.0-release.apk` to your device
2. Загрузите `VirtualCompanion-v1.0-release.apk` на устройство

#### Step 3: Install / Шаг 3: Установка

1. Open your **Downloads** folder / Откройте папку **Загрузки**
2. Tap on the APK file / Нажмите на APK файл
3. Tap **Install** / Нажмите **Установить**
4. Wait for installation to complete / Дождитесь завершения установки
5. Tap **Open** to launch the app / Нажмите **Открыть** для запуска

---

### Method 2: Install via ADB (Developers) / Способ 2: Установка через ADB (Разработчики)

#### Requirements / Требования:
- Android Debug Bridge (ADB) installed on computer / ADB установлен на компьютере
- USB Debugging enabled on device / USB отладка включена на устройстве

#### Commands / Команды:

```bash
# Check if device is connected / Проверить подключение устройства
adb devices

# Install APK / Установить APK
adb install VirtualCompanion-v1.0-release.apk

# Or replace existing installation / Или заменить существующую установку
adb install -r VirtualCompanion-v1.0-release.apk
```

---

## First Launch / Первый запуск

### 1. Grant Permissions / Предоставить разрешения

On first launch, the app will request necessary permissions:

При первом запуске приложение запросит необходимые разрешения:

- **📱 Media Access / Доступ к медиа** - To import WhatsApp archives and access media files / Для импорта архивов WhatsApp и доступа к медиафайлам
- **🎤 Audio Recording / Запись аудио** - For voice interaction features / Для функций голосового взаимодействия

**Tap "Grant Permissions" / Нажмите "Предоставить разрешения"**

### 2. Explore Features / Изучить возможности

After granting permissions, you can:

После предоставления разрешений вы можете:

- 💬 **Chat** - Text conversation with virtual companion / Текстовый чат с виртуальным собеседником
- 🎤 **Voice** - Record and play voice messages / Записывать и воспроизводить голосовые сообщения
- ⚙️ **Settings** - Customize app appearance and behavior / Настроить внешний вид и поведение
- 🎨 **Background** - Set custom background image / Установить пользовательское фоновое изображение

---

## Troubleshooting / Устранение неполадок

### Issue: "App not installed" / Проблема: "Приложение не установлено"

**Possible solutions / Возможные решения:**

1. **Check storage space / Проверьте свободное место**
   - Ensure you have at least 150 MB free / Убедитесь, что есть минимум 150 МБ свободного места

2. **Uninstall previous version / Удалите предыдущую версию**
   - Go to Settings → Apps → Virtual Companion → Uninstall
   - Перейдите в Настройки → Приложения → Virtual Companion → Удалить

3. **Re-download APK / Загрузите APK заново**
   - The file might be corrupted / Файл может быть поврежден

### Issue: "Parse error" / Проблема: "Ошибка парсинга"

**Solutions / Решения:**

1. **Check Android version / Проверьте версию Android**
   - Requires Android 6.0+ / Требуется Android 6.0+

2. **Re-download APK / Загрузите APK заново**
   - Download might be incomplete / Загрузка может быть неполной

3. **Check device compatibility / Проверьте совместимость устройства**
   - Some custom ROMs may have issues / Некоторые кастомные прошивки могут иметь проблемы

### Issue: App crashes on launch / Проблема: Приложение вылетает при запуске

**Solutions / Решения:**

1. **Clear app data / Очистить данные приложения**
   - Settings → Apps → Virtual Companion → Storage → Clear Data
   - Настройки → Приложения → Virtual Companion → Хранилище → Очистить данные

2. **Restart device / Перезагрузить устройство**

3. **Reinstall app / Переустановить приложение**

### Issue: Permissions not working / Проблема: Разрешения не работают

**Solutions / Решения:**

1. **Manually grant permissions / Вручную предоставить разрешения**
   - Settings → Apps → Virtual Companion → Permissions
   - Настройки → Приложения → Virtual Companion → Разрешения

2. **Check if permissions are restricted / Проверить, не ограничены ли разрешения**
   - Some devices have additional permission managers / Некоторые устройства имеют дополнительные менеджеры разрешений

### Issue: Voice recording not working / Проблема: Запись голоса не работает

**Solutions / Решения:**

1. **Check microphone permission / Проверить разрешение микрофона**
2. **Test microphone in other apps / Протестировать микрофон в других приложениях**
3. **Check if another app is using microphone / Проверить, не использует ли микрофон другое приложение**

---

## Uninstallation / Удаление

### Method 1: From Settings / Способ 1: Через настройки

1. Open **Settings** / Откройте **Настройки**
2. Go to **Apps** / Перейдите в **Приложения**
3. Find **Virtual Companion** / Найдите **Virtual Companion**
4. Tap **Uninstall** / Нажмите **Удалить**
5. Confirm by tapping **OK** / Подтвердите, нажав **ОК**

### Method 2: From Home Screen / Способ 2: С главного экрана

1. Long-press the app icon / Долгое нажатие на иконку
2. Drag to **Uninstall** / Перетащите на **Удалить**
3. Confirm / Подтвердите

### Method 3: Via ADB (Developers) / Способ 3: Через ADB (Разработчики)

```bash
adb uninstall com.asis.virtualcompanion
```

---

## Data Privacy / Конфиденциальность данных

- All data is stored locally on your device / Все данные хранятся локально на устройстве
- No data is sent to external servers / Никакие данные не отправляются на внешние серверы
- You can clear all data at any time in Settings / Вы можете очистить все данные в любое время в Настройках
- Uninstalling the app removes all data / Удаление приложения удаляет все данные

---

## Support / Поддержка

If you encounter any issues or have questions:
Если у вас возникли проблемы или вопросы:

- **Email:** support@asis-companion.com
- **GitHub Issues:** github.com/asis/virtualcompanion/issues
- **Documentation:** github.com/asis/virtualcompanion/wiki

---

## Version Information / Информация о версии

- **Current Version / Текущая версия:** 1.0
- **Release Date / Дата выпуска:** 2024
- **Build / Сборка:** 1
- **APK Size / Размер APK:** ~50-100 MB

---

## What's New in v1.0 / Что нового в v1.0

✨ **Initial Release / Первый релиз**

- 💬 Text chat with virtual companion / Текстовый чат с виртуальным собеседником
- 🎤 Voice recording and playback / Запись и воспроизведение голоса
- 🤖 AI-powered responses / Ответы на основе ИИ
- 🎨 Custom backgrounds / Пользовательские фоны
- 🔒 Privacy controls / Контроль конфиденциальности
- ⚙️ Flexible settings / Гибкие настройки
- 🌙 Dark mode support / Поддержка темной темы

---

**Thank you for using Virtual Companion! / Спасибо за использование Virtual Companion!** 🎉
