# NucleusFS 📁⚡

**NucleusFS** is a high-performance Android storage analyzer, remote cloud file manager, and AI directory organizer built with modern **Jetpack Compose** and Material 3 design.

---

## ✨ Features

### 📊 Storage Analytics & Overview
- **Visual Storage Breakdown**: Interactive donut charts and category distributions (Videos, Images, Documents, Audio, Apps, Archives, System).
- **Storage Diagnostics**: Dynamic usage tracking and capacity health indicators (**Optimal**, **Warning**, **Critical**).
- **Largest Indexed Files**: Detects storage-hogging files across local and connected storage nodes.

### 🌐 Remote Cloud Storage Integration
- **Multi-Protocol Support**: Native support for **SFTP**, **FTP**, and **SMB** (Windows Network Share) remote protocols.
- **Encrypted Credential Storage**: Secure credential persistence powered by **DataStore** and **Android KeyStore**.
- **Live Status & Connectivity Checks**: Real-time status monitoring (**Online**, **Connecting**, **Offline**) with explicit protocol timeouts (10s) and error retry views.
- **Connection Management**: Manage, test, edit, and create connections seamlessly.

### 🎯 Multi-Selection & Batch Operations
- **Multi-Selection Mode**: Select files and folders via the long-press context menu (*"Select"*).
- **Hierarchical Sub-Item Selection**: Selecting a folder automatically includes all nested files and sub-folders (including hidden items).
- **Optimized Background Operations**: Batch operations (Copy, Move, Delete) process top-level parent folders directly to optimize file system performance.
- **Interactive Folder Browser**: Visual expandable directory tree browser for choosing Copy To and Move To destinations.
- **Validation & Safety Rules**: Built-in protection against moving files into the same directory or moving/copying folders into themselves or their subfolders.

### 🤖 AI Directory Assistant
- **Smart Folder Cleanup**: Categorizes local downloads and unorganized folders using Google Gemini AI.

---

## 🛠 Tech Stack & Libraries

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design system
- **Language**: 100% [Kotlin](https://kotlinlang.org/) with Coroutines & StateFlow
- **Encrypted Storage**: AndroidX DataStore + KeyStore encryption
- **SFTP Protocol**: [JSch](http://www.jcraft.com/jsch/)
- **FTP Protocol**: [Apache Commons Net](https://commons.apache.org/proper/commons-net/)
- **SMB Protocol**: [SMBJ](https://github.com/hierynomus/smbj)
- **AI Integration**: [Google Gemini API](https://ai.google.dev/)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Version 17+
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35 (Android 15)

### Building & Running

1. Clone the repository and open the project in Android Studio.
2. Build the debug APK:
   ```bash
   ./gradlew app:assembleDebug
   ```
3. Run unit tests:
   ```bash
   ./gradlew app:testDebugUnitTest
   ```

---

## 📄 License & Privacy
- **100% On-Device Processing**: Local file indexing and operations run completely on-device.
- **Ad-Free**: Zero third-party ad networks or tracking SDKs.
