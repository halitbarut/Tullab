# Tullab 🦉

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/platform-Android-3DDC84.svg)](#)

**Tullab** is a modern, privacy-first Android application designed specifically for private tutors, coaches, and instructors. It provides a clean and reliable way to manage students, lessons, homework, and payments—all without relying on external servers or hidden syncs.

---

## Table of Contents
- [Why Tullab?](#why-tullab)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Tech Stack](#tech-stack)
- [Help & Support](#help--support)
- [Contributing & Maintainers](#contributing--maintainers)
- [License](#license)

---

## Why Tullab?

Tullab solves the chaos of scattered notes, manual payment tracking, and the lack of actionable insights for independent educators. By keeping all your data locally on your device, Tullab ensures absolute privacy while still offering a rich, frictionless Material 3 experience.

### Key Features
- **👨‍🎓 Effortless Student Management:** Track student progress, notes, and key details in one organized hub.
- **🗓️ Smart Scheduling:** Visualize lessons on a calendar with clear indicators for upcoming, completed, and payment-pending sessions.
- **💰 Automated Payment Tracking:** Automatically calculate owed amounts, log payments, and maintain an auditable history.
- **📝 Homework Management:** Assign homework and monitor completion status alongside performance notes.
- **🔒 Privacy-First Data:** All information is stored locally on your device via Room Database.
- **🌍 Global Ready:** Built-in multi-language (English, Turkish, German) and multi-currency (USD, TRY, EUR) support.

---

## Getting Started

Follow these instructions to get a local copy of Tullab up and running for development and testing purposes.

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Latest stable version recommended)
- **JDK 17** configured in your environment

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/Tullab.git
   cd Tullab
   ```

2. **Open the Project:**
   Open Android Studio, select **Open**, and navigate to the cloned `Tullab` directory. Allow Gradle to sync the dependencies.

3. **Build and Run:**
   You can build the debug APK using the Gradle wrapper from your terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   Or, simply click the **Run** button (`Shift + F10`) in Android Studio to deploy the app to an emulator or a physical device.

### Usage Example

Since Tullab is primarily a UI-driven application, standard usage revolves around interacting with the Jetpack Compose screens. Here is a quick look at how you might structure a basic composable within the app:

```kotlin
@Composable
fun StudentDashboardScreen(
    viewModel: StudentViewModel = hiltViewModel()
) {
    val students by viewModel.students.collectAsState()
    
    LazyColumn {
        items(students) { student ->
            StudentCard(
                name = student.name,
                onTap = { viewModel.navigateToDetails(student.id) }
            )
        }
    }
}
```

---

## Tech Stack

Tullab uses a modern Android development stack:
- **UI:** 100% Jetpack Compose for declarative UI
- **Language:** Kotlin (with Coroutines/Flow)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt
- **Local Storage:** Room Database

---

## Help & Support

If you run into issues, need help setting up the project, or have any questions:
- Open an [Issue](https://github.com/your-username/Tullab/issues) on GitHub.
- Reach out directly via email: **[mhbarut66@gmail.com](mailto:mhbarut66@gmail.com)**.

---

## Contributing & Maintainers

We welcome contributions from the community! Whether it's fixing a bug, suggesting a feature, or improving documentation, your help is appreciated.

- **Maintainer:** Halit Barut ([mhbarut66@gmail.com](mailto:mhbarut66@gmail.com))
- **How to Contribute:** Please read our [Contribution Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

## License

This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for more details.
