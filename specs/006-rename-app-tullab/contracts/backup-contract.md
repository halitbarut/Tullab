# Backup & Data Archive Contract: Application Rebranding to "Tullab"

**Feature**: `006-rename-app-tullab` | **Date**: 2026-09-05

## 1. Export Filename Contract

When generating an export archive via the user interface (e.g. `SettingsScreen.kt`), the suggested filename must satisfy the following specification:

```kotlin
// Required pattern:
val fileName = "tullab_backup_${LocalDateTime.now().format(backupFileNameFormatter)}.csv"
```

- **Prefix**: `tullab_backup_` (lowercase)
- **Timestamp Format**: `yyyyMMdd_HHmmss`
- **File Extension**: `.csv`

---

## 2. Import Compatibility Contract

The backup restoration component (`DataBackupManager.kt`) must adhere to the following contract:

1. **File Selection**: The application MUST NOT restrict user selection via SAF exclusively to filenames matching `tullab_backup_*`. It must accept any `.csv` file selected by the user.
2. **Schema & Header Verification**:
   - The backup manager validates file headers and column counts.
   - Any valid backup exported from earlier versions (e.g. `kora_backup_20260901_120000.csv`) MUST be accepted and parsed identically to files starting with `tullab_backup_`.
3. **Restoration Outcome**: All students, lessons, and payment records in valid legacy or new backup files must restore without failure or rejection.
