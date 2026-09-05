# Data Model & Configuration Specifications: Application Rebranding to "Tullab"

**Feature**: `006-rename-app-tullab` | **Date**: 2026-09-05

## Overview

Because this feature focuses on application identity, package namespace, launcher iconography, and string rebranding, no existing domain business entities (such as `Student`, `Lesson`, `Homework`, `PaymentRecord`) are altered. This document details the operational configuration models, branding profiles, and storage artifacts defined for "Tullab".

---

## 1. Application Branding Profile

Represents the identity presented across the Android OS, system settings, and in-app screens.

| Property | Value / Source | Description |
|---|---|---|
| **App Display Name** | `Tullab` (`@string/app_name`) | Visible label under the launcher icon, in task switcher, and in app settings |
| **Application ID** | `com.barutdev.tullab` | Unique Android package identifier in `app/build.gradle.kts` |
| **Package Namespace** | `com.barutdev.tullab` | Kotlin package hierarchy root across `src/main`, `src/test`, and `src/androidTest` |
| **Theme Style** | `@style/Theme.Tullab` | System theme defined in `res/values/themes.xml` and referenced in `AndroidManifest.xml` |
| **Compose Theme** | `TullabTheme` | Root Compose theme wrapper in `ui/theme/Theme.kt` |
| **Application Class** | `com.barutdev.tullab.TullabApp` | Custom `Application` subclass annotated with `@HiltAndroidApp` |
| **Root Nav Graph** | `TullabNavGraph` | Navigation controller and graph composable |

---

## 2. Adaptive Launcher Icon Asset Model

Defines the structure and layered components of the launcher icon.

```text
mipmap-anydpi-v26/ic_launcher.xml
├── Background: drawable/ic_launcher_background.xml (Solid White: #FFFFFF)
├── Foreground: drawable/ic_launcher_tullab_foreground.xml
│   └── Inset: InsetDrawable (18% margin) wrapping drawable/tullab.xml
└── Monochrome: drawable/ic_launcher_tullab_foreground.xml
```

### Component Details:
- **`drawable/tullab.xml`**: Vector drawable containing the primary visual emblem (viewport: `225.3` × `268.45`, fill colors `#A38856` gold and `#152640` navy).
- **`drawable/ic_launcher_background.xml`**: Solid white (`#FFFFFF`) 108×108dp canvas.
- **`drawable/ic_launcher_tullab_foreground.xml`**: Safe-zone wrapper applying inset padding so the graphic never clips on round or squircle device masks.

---

## 3. Storage & Database Specification

Defines the local Room database configuration.

| Property | Value | Description |
|---|---|---|
| **Database Class** | `TullabDatabase` | Room database class annotated with `@Database` |
| **Database File** | `tullab.db` | SQLite database filename passed to `Room.databaseBuilder` |
| **Schema Directory** | `app/schemas/` | Room JSON schema repository |
| **Export Schema** | `true` | Continues strict schema change tracking |

---

## 4. Backup Archive Model

Defines exported file specifications and compatibility rules.

### Export Format
- **Default Filename Pattern**: `tullab_backup_yyyyMMdd_HHmmss.csv`
- **MIME Type**: `text/csv` / `application/octet-stream`
- **Content**: Comma-separated or structured table of students, lessons, and payment records.

### Import Acceptance Criteria
- Files matching `tullab_backup_*.csv` MUST be accepted and processed.
- Legacy files matching `kora_backup_*.csv` MUST continue to be accepted and processed identically.
- Any valid CSV backup export selected via the Android Storage Access Framework (SAF) file picker MUST be verified by header content, not rejected based on filename.
