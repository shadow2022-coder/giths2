# 🎨 Hasta-Kala Shop

> A modern Android inventory and billing app built for small handcraft businesses. Manage products, track sales, get AI-powered business insights, and sell faster — all from your phone.

---

## 📋 Table of Contents

- [Problem Statement](#-problem-statement)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Screenshots](#-screenshots)
- [Project Structure](#-project-structure)
- [Installation & Setup](#-installation--setup)
- [How to Run](#-how-to-run)
- [Build APK](#-build-apk)
- [Architecture](#-architecture)
- [AI Integration](#-ai-integration)
- [Multi-Language Support](#-multi-language-support)
- [Future Improvements](#-future-improvements)

---

## 🧩 Problem Statement

Small handcraft shop owners in India often manage their inventory and billing using paper registers or basic calculators. This leads to:

- **Lost sales data** — no way to track what sold and when
- **Stock mismanagement** — products run out without warning
- **No business insights** — owners cannot identify trends or optimize pricing
- **Language barriers** — most existing tools are English-only

**Hasta-Kala Shop** solves this by providing a simple, offline-first Android app that handles product management, fast billing, low-stock alerts, and optional AI-powered business insights — all in the user's preferred language.

---

## ✨ Features

### Core Features
| Feature | Description |
|---------|-------------|
| 🏠 **Dashboard** | At-a-glance view of today's income, units sold, total products, and low-stock alerts |
| 🛒 **Quick Sell** | Fast billing with quantity input via typing or `+`/`-` buttons, searchable product grid |
| 📦 **Inventory Management** | Add, edit, and delete products with color variants, stock levels, and pricing |
| 📊 **AI Insights** | Get AI-generated business recommendations, pricing suggestions, and sales analysis |
| ⚙️ **Settings** | Configure AI provider, API key (securely stored), language, and low-stock thresholds |

### Additional Features
- 🔍 **Search** — Search bars on Home (low-stock), Sell, and Inventory screens
- 🌐 **Multi-language** — Full UI in English, Hindi (हिन्दी), Kannada (ಕನ್ನಡ), and Telugu (తెలుగు)
- 🔊 **Text-to-Speech** — AI responses can be read aloud in the selected language
- 🚀 **Launch Animation** — Smooth startup overlay animation
- 🔒 **Security** — Encrypted API key storage, no cloud backup, no HTTP logging of secrets
- 📱 **Offline-First** — All data stored locally using Room database, works without internet
- 📤 **Manual Export** — Export data manually, no automatic cloud sync

---

## 🛠 Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | Modern declarative UI framework |
| **Material 3** | Design system and theming |
| **Room Database** | Local SQLite persistence |
| **Hilt (Dagger)** | Dependency injection |
| **Retrofit + Moshi** | HTTP networking and JSON parsing |
| **DataStore** | Preferences storage |
| **AndroidX Security** | Encrypted shared preferences for API keys |
| **Coil** | Image loading |
| **MPAndroidChart** | Business analytics charts |
| **Navigation Compose** | Screen navigation |
| **Coroutines + Flow** | Asynchronous programming |

---

## 📸 Screenshots

> Screenshots will be added after the first build. To see the app in action, build and run it using the instructions below.

<!-- Add screenshots here:
![Home Screen](screenshots/home.png)
![Sell Screen](screenshots/sell.png)
![Inventory Screen](screenshots/inventory.png)
![AI Insights](screenshots/insights.png)
![Settings](screenshots/settings.png)
-->

---

## 📁 Project Structure

```
giths2/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hastakala/shop/
│   │   │   │   ├── data/                    # Data layer
│   │   │   │   │   ├── local/               # Room entities, DAOs, database
│   │   │   │   │   │   ├── dao/             # ProductDao, SaleDao, VariantDao
│   │   │   │   │   │   ├── model/           # Query result models
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── Product.kt
│   │   │   │   │   │   ├── Sale.kt
│   │   │   │   │   │   └── Variant.kt
│   │   │   │   │   └── repository/          # ShopRepository, SettingsRepository
│   │   │   │   ├── di/                      # Hilt dependency injection module
│   │   │   │   ├── network/                 # AI networking layer
│   │   │   │   │   └── ai/
│   │   │   │   │       ├── provider/        # Gemini, OpenAI, Claude, Mistral adapters
│   │   │   │   │       ├── model/           # AI request/response models
│   │   │   │   │       ├── AiManager.kt
│   │   │   │   │       ├── AiManagerImpl.kt
│   │   │   │   │       └── AiProviderAdapter.kt
│   │   │   │   ├── ui/                      # Presentation layer
│   │   │   │   │   ├── components/          # Reusable Compose components
│   │   │   │   │   ├── home/               # Dashboard screen + ViewModel
│   │   │   │   │   ├── sell/               # Sell/billing screen + ViewModel
│   │   │   │   │   ├── inventory/          # Inventory screen + ViewModel
│   │   │   │   │   ├── insights/           # AI insights screen + ViewModel
│   │   │   │   │   ├── settings/           # Settings screen + ViewModel
│   │   │   │   │   ├── navigation/         # App navigation + launch overlay
│   │   │   │   │   └── theme/              # Colors, typography, Material 3 theme
│   │   │   │   ├── util/                   # Currency and time utility functions
│   │   │   │   ├── HastaKalaApplication.kt # Hilt application class
│   │   │   │   └── MainActivity.kt         # Single-activity entry point
│   │   │   ├── res/                         # Android resources
│   │   │   │   ├── drawable/               # App icon vectors
│   │   │   │   ├── mipmap-anydpi-v26/      # Adaptive launcher icons
│   │   │   │   ├── values/                 # English strings, themes
│   │   │   │   ├── values-hi/              # Hindi strings
│   │   │   │   ├── values-kn/              # Kannada strings
│   │   │   │   ├── values-te/              # Telugu strings
│   │   │   │   └── xml/                    # Backup rules
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                            # Unit tests (Room, Repository)
│   │   └── androidTest/                     # Instrumentation tests
│   ├── build.gradle.kts                     # App-level Gradle config
│   └── proguard-rules.pro                   # ProGuard rules for release
├── gradle/wrapper/                          # Gradle wrapper (auto-downloads Gradle)
├── build.gradle.kts                         # Root Gradle config (plugin versions)
├── settings.gradle.kts                      # Project settings
├── gradle.properties                        # Gradle JVM arguments
├── keystore.properties.example              # Template for release signing
├── gradlew                                  # Gradle wrapper script (macOS/Linux)
├── gradlew.bat                              # Gradle wrapper script (Windows)
├── .gitignore                               # Git exclusions
└── README.md                                # This file
```

---

## 🚀 Installation & Setup

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** (bundled with Android Studio)
- **Android SDK** with API level 35 (compileSdk) and minimum API 26
- **Git** installed on your system

### Step-by-step Setup

**1. Clone the repository**

```bash
git clone https://github.com/<your-username>/giths2.git
cd giths2
```

**2. Open in Android Studio**

- Open Android Studio
- Click **File → Open**
- Select the cloned `giths2` folder
- Wait for Gradle sync to complete (this downloads all dependencies automatically)

**3. Create `local.properties`** (if not auto-generated)

Android Studio usually creates this automatically. If not, create it in the project root:

```properties
sdk.dir=/path/to/your/Android/sdk
```

Common paths:
- **macOS**: `sdk.dir=/Users/<username>/Library/Android/sdk`
- **Windows**: `sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk`
- **Linux**: `sdk.dir=/home/<username>/Android/Sdk`

**4. (Optional) Set up release signing**

Only needed if you want to build a signed release APK:

```bash
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties` with your own keystore values.

---

## ▶️ How to Run

### Option A: Run in Android Studio (Recommended)

1. Open the project in Android Studio
2. Select a device:
   - **Emulator**: Click **Device Manager → Create Device** → Choose Pixel 7 or similar → Select API 35 system image → Finish
   - **Physical device**: Enable USB debugging and connect via USB
3. Click the **▶ Run** button (or press `Shift + F10`)
4. The app will build, install, and launch automatically

### Option B: Run from Terminal

```bash
# macOS / Linux
./gradlew installDebug

# Windows
gradlew.bat installDebug
```

This installs the debug APK on a connected device or running emulator.

---

## 📦 Build APK

### Debug APK (for testing)

```bash
# macOS / Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (for distribution)

First set up `keystore.properties` (see Installation step 4), then:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Install APK on Emulator

1. Start an Android emulator from Android Studio
2. Drag and drop the `.apk` file onto the emulator window, **OR**:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏗 Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)             │
│  HomeScreen · SellScreen · InventoryScreen  │
│  InsightsScreen · SettingsScreen            │
└─────────────────┬───────────────────────────┘
                  │ observes StateFlow
┌─────────────────▼───────────────────────────┐
│           ViewModel Layer                    │
│  HomeVM · SellVM · InventoryVM              │
│  InsightsVM · SettingsVM                    │
└─────────────────┬───────────────────────────┘
                  │ calls repository
┌─────────────────▼───────────────────────────┐
│           Data Layer                         │
│  ShopRepository · SettingsRepository        │
│  AiManager · AiProviderAdapter              │
└─────────┬───────────────────┬───────────────┘
          │                   │
┌─────────▼─────────┐ ┌──────▼──────────────┐
│   Room Database    │ │   Retrofit (AI)     │
│ Product · Variant  │ │ Gemini · OpenAI     │
│ Sale · DAOs        │ │ Claude · Mistral    │
└───────────────────┘ └─────────────────────┘
```

**Key architectural decisions:**
- **Single Activity** with Jetpack Navigation Compose
- **Hilt** for dependency injection across all layers
- **Room** for type-safe SQLite operations with Flow-based reactive queries
- **DataStore + EncryptedSharedPreferences** for settings and API key storage
- **Adapter pattern** for swappable AI providers

---

## 🤖 AI Integration

The app supports **4 AI providers** through a pluggable adapter system:

| Provider | Model | Use Case |
|----------|-------|----------|
| **Google Gemini** | gemini-2.0-flash | Business insights, pricing suggestions |
| **OpenAI** | gpt-4o-mini | Sales analysis, inventory recommendations |
| **Anthropic Claude** | claude-3-haiku | Product descriptions, trend analysis |
| **Mistral** | mistral-small-latest | Cost-effective business advice |

### How it works:
1. User enters their own API key in Settings (BYOK — Bring Your Own Key)
2. Keys are encrypted locally using AndroidX Security library
3. AI requests are triggered manually (no background data collection)
4. Responses support Text-to-Speech playback in the selected language
5. All AI communication goes through official provider endpoints only

---

## 🌐 Multi-Language Support

| Language | Code | Coverage |
|----------|------|----------|
| English | `en` | Full UI + AI responses |
| Hindi (हिन्दी) | `hi` | Full UI + AI responses |
| Kannada (ಕನ್ನಡ) | `kn` | Full UI + AI responses |
| Telugu (తెలుగు) | `te` | Full UI + AI responses |

The language can be changed from the Settings screen. AI responses are also generated in the selected language.

---

## 🧪 Testing

Run unit tests:

```bash
./gradlew test
```

Tests cover:
- `AppDatabaseTest` — Room database operations and migrations
- `ShopRepositoryTest` — Repository layer business logic

---

## 🔮 Future Improvements

- [ ] Add barcode scanning for quick product lookup
- [ ] Implement sales reports with date range filtering
- [ ] Add product image capture from camera
- [ ] Support data backup/restore via file export
- [ ] Add more Indian languages (Tamil, Malayalam, Bengali)
- [ ] Implement invoice generation and PDF export
- [ ] Add dark mode theme toggle
- [ ] Support multiple shop/store profiles

---

## 📄 License

This project is developed for educational purposes as part of a college submission.

---

## 👤 Author

Developed with ❤️ for Indian handcraft shop owners.
