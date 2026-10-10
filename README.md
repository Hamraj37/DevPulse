# DevPulse ⚡

**DevPulse** is a modern, native Android system telemetry, hardware diagnostic suite, and real-time performance monitoring application built with **Jetpack Compose** and **Material 3**.

Designed for developers, power users, and QA engineers, DevPulse offers real-time hardware telemetry streams, live per-core CPU monitoring, zRAM swap analytics, dual-SIM network inspection, and a comprehensive hardware diagnostic test suite with persistent test reports.

> 🌐 **Web Landing Page & Showcase**: Check out the modern web landing page ([`docs/index.html`](file:///C:/Users/Administrator/AndroidStudioProjects/DevPulse2/docs/index.html)) created for DevPulse (GitHub Pages) featuring all app screenshots across Dashboard, System, CPU, Battery, Connectivity, Display, Memory, and Thermal subsystems!

---

## ✨ Features & Capabilities

### 📊 1. Real-Time Dashboard
* **RAM Telemetry**: Circular RAM usage gauge and live streaming sparkline chart.
* **CPU Core Status**: Live per-core frequency stream (MHz) and active core distribution.
* **Hardware Tests Report**: Interactive summary card displaying **Passed**, **Failed**, and **Pending** test counts with quick navigation to the diagnostic suite.
* **Storage & Battery Overview**: Internal storage gauge, battery charging state, voltage, and temperature readout.
* **Quick Tools**: Fast access to system analysis and specification export.

### 🧪 2. Hardware Diagnostic Suite
Interactive and automated hardware diagnostic tests with persistent results saved across app restarts:
* 📱 **Display & Touch**: Screen dead pixel check, multi-touch touch-point canvas.
* 🔦 **Flashlight & LED**: Torch LED hardware toggle.
* 🔊 **Audio Channels**: Stereo loudspeaker and earpiece beep tone tests.
* 🎙️ **Microphone**: Live ambient sound pressure spectrum meter (dB).
* 📡 **Sensors**: Proximity sensor, ambient light lux meter, and 2D accelerometer bubble level.
* 🔌 **Charging & Power**: Real-time USB power plug detector.
* 📳 **Haptics**: Vibration motor test.
* 📻 **Radios & Connectivity**: Bluetooth radio check, dual-SIM carrier detection, and GPS/Location satellite fix.
* 🔘 **Hardware Keys**: Volume Up/Down key event listener.

### 🔍 3. Deep System Telemetry Screens
* **Device**: Identity, model, board, build fingerprint, device type, eSIM support, and **Dual SIM Carrier Names** (SIM 1 & SIM 2 via `SubscriptionManager`).
* **System**: Android OS version, API level, security patch, bootloader, Widevine DRM level (L1/L3), and live system uptime.
* **CPU**: Processor architecture, core counts, live per-core frequencies, max frequencies, and GPU details.
* **Battery**: Charge level, health state, voltage (V), current (mA), power (W), temperature (°C), and sparkline history.
* **Memory & Storage**: Volatile RAM, **zRAM (Compressed Swap)** with real-time compression ratios (`X.XSaved`), `/system` storage, and `/data` internal storage.
* **Network & Connectivity**: IP address (IPv4/IPv6), Wi-Fi link speed, signal strength (dBm), cellular operator, Wi-Fi 6E, Bluetooth, NFC, UWB, and USB capabilities.
* **Display**: Resolution, refresh rates (120 Hz), physical size, pixel density, orientation, brightness %, and HDR support.
* **Camera**: Front & back camera resolutions, apertures, focal lengths, AF modes, and OIS support.
* **Thermal**: Thermal zone temperatures (°C) and throttling headroom status.
* **Sensors**: Hardware sensor inventory, power consumption (mA), and vendor specifications.
* **Apps**: Installed user and system applications directory with instant search and category filtering.

---

## 🛠️ Tech Stack & Architecture

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 & Monet dynamic theming
* **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
* **Asynchronous Streams**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
* **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with [KSP](https://developer.android.com/build/migrate-to-ksp)
* **Storage & Persistence**: `SharedPreferences` for test report persistence, `DataStore` Preferences, and `Room`
* **Navigation**: Material 3 `HorizontalPager` with predictive back-press handling (`BackHandler`)

---

## 🚀 Building & Running

### Prerequisites
* **Android Studio**: Ladybug / 2024.2+ or Android Studio Jellyfish / Koala
* **Android SDK**: `compileSdk = 37`, `minSdk = 30` (Android 11+), `targetSdk = 36`
* **JDK**: Java 11 or higher

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Hamraj37/DevPulse.git
   cd DevPulse
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

3. **Build Signed Release APK**:
   Configure your signing credentials in `local.properties`:
   ```properties
   KEYSTORE_BASE64=<your_base64_encoded_keystore>
   KEYSTORE_PASSWORD=<your_keystore_password>
   KEY_ALIAS=<your_key_alias>
   KEY_PASSWORD=<your_key_password>
   ```
   Then run:
   ```bash
   ./gradlew app:assembleRelease
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew app:testDebugUnitTest
   ```

5. **Automated CI/CD & Telegram Release Announcements**:
   The GitHub Actions workflow (`.github/workflows/release.yml`) automatically builds release APKs, calculates SHA-256 checksums, extracts commit changelogs, creates GitHub Releases, and sends release announcements to the [DevPulse Telegram Channel](https://t.me/DevPulseApp).
   To enable Telegram release notifications, configure these secrets in **Repository Settings → Secrets and variables → Actions**:
   * `TELEGRAM_BOT_TOKEN`: Bot token from Telegram's `@BotFather`.
   * `TELEGRAM_CHAT_ID`: Telegram channel or group ID (e.g. `@DevPulseApp`).


---

## 📱 App Navigation & Navigation Back-Press Strategy

* **Top Bar**: Displays the DevPulse logo, app title, active device name badge, and refresh button.
* **Tab Row**: Scrollable tab navigation bar accessing 14 telemetry & diagnostic categories.
* **Predictive Back Navigation**: Pressing the Android hardware back button/gesture on any sub-screen smoothly animates back to the **Dashboard** page before exiting the app.

---

## 💖 Support & Donate

DevPulse is 100% ad-free, open-source, and free to use. If you find this project useful, consider supporting development:

* **UPI ID**: `hamraj37@ybl`

---

## 📄 License

```text
Copyright (c) 2026 Hamraj37

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.
```
