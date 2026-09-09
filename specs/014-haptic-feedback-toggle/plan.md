# Implementation Plan: Haptic Feedback Toggle

**Branch**: `014-haptic-feedback-toggle` | **Date**: 2026-09-09 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/014-haptic-feedback-toggle/spec.md`

## Summary

Introduce a user preference toggle for haptic feedback in Tullab Settings adhering to Material 3 standards:
1. Extend the domain `UserPreferences` model and DataStore repository with a `hapticFeedbackEnabled: Boolean` property (defaulting to `true`).
2. Add an interactive `SettingSwitchRow` for "Haptic Feedback" in `SettingsScreen.kt` under the `General` section (between Dark Mode and Language) using `Icons.Outlined.Vibration` and supporting full English, Turkish, and German localization.
3. Update `TullabHaptics.kt` to gate tactile pulses through this preference via `rememberTullabHapticFeedback(enabled: Boolean = LocalUserPreferences.current.hapticFeedbackEnabled)`, silencing all tactile pulses and motor vibrations when disabled while preserving all visual and undo feedback.

## Technical Context

**Language/Version**: Kotlin 2.0.21, JVM target 17  
**Primary Dependencies**: Jetpack Compose + Material 3 (`androidx.compose.material3`), Jetpack DataStore Preferences, Hilt, Coroutines & Flow  
**Storage**: Jetpack DataStore (`UserPreferencesRepository`)  
**Testing**: JUnit 4, Cash App Turbine (`app.cash.turbine`), `kotlinx-coroutines-test`, MockK  
**Target Platform**: Android (Min SDK 26, Compile/Target SDK 36)  
**Project Type**: Android Mobile App (Clean Architecture: `ui → domain ← data`)  
**Performance Goals**: Instant toggle response (<16ms frame dispatch), zero overhead in haptic suppression check (<1ms)  
**Constraints**: 100% offline, no internet permission, strict localization parity (EN/TR/DE), 48x48 dp touch target  
**Scale/Scope**: 1 preference key, 1 settings UI row, 1 helper gating condition, 3 localization files  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check | Status | Notes |
|---|---|---|---|
| **I. Offline-First & Privacy-First** | No internet permission, local persistence only | **PASS** | Stored exclusively in local Jetpack DataStore Preferences. |
| **II. Clean Architecture & Modularity** | Inward dependency flow `ui → domain ← data` | **PASS** | Domain defines `UserPreferences`, data implements DataStore persistence, UI consumes via ViewModel and `LocalUserPreferences`. |
| **III. Hilt Dependency Injection** | No service locators or global singletons | **PASS** | `UserPreferencesRepository` injected via Hilt; SettingsViewModel uses `@HiltViewModel`. |
| **IV. Compose-Only UI & Accessibility** | 100% Compose/M3, 48x48dp touch targets, a11y descriptions | **PASS** | `SettingSwitchRow` with `Icons.Outlined.Vibration` meets 48x48dp target, has proper semantics, supports 200% font scaling. |
| **V. MVVM & UDF** | Immutable StateFlow, single source of truth | **PASS** | `userPreferences` exposed as `StateFlow<UserPreferences>`, toggle dispatches `viewModel.updateHapticFeedbackEnabled(Boolean)`. |
| **VI. Error Handling** | Typed results, graceful degradation | **PASS** | Fallback to `true` on missing key or DataStore read failure. |
| **VII. Localization (i18n)** | Full EN, TR, DE parity, no hardcoded strings | **PASS** | Strings externalized to `values`, `values-tr`, and `values-de`. |
| **VIII. Security & Hardening** | No sensitive data logged | **PASS** | Simple boolean preference, no PII. |

## Project Structure

### Documentation (this feature)

```text
specs/014-haptic-feedback-toggle/
├── spec.md              # Feature specification
├── plan.md              # This file (Implementation Plan)
├── research.md          # Phase 0 research & decisions
├── data-model.md        # Phase 1 data model & state lifecycle
├── quickstart.md        # Phase 1 quickstart & verification guide
├── contracts/           # Phase 1 UI and component contracts
│   ├── ui-settings-contract.md
│   └── haptics-helper-contract.md
└── checklists/
    └── requirements.md  # Spec quality checklist
```

### Source Code (repository root)

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── domain/
│   │   ├── model/
│   │   │   └── UserPreferences.kt              # Add hapticFeedbackEnabled: Boolean = true
│   │   └── repository/
│   │       └── UserPreferencesRepository.kt    # Add updateHapticFeedbackEnabled(Boolean)
│   ├── data/
│   │   └── repository/
│   │       └── UserPreferencesRepository.kt    # DataStore persistence mapping & edit
│   └── ui/
│       ├── components/
│       │   └── TullabHaptics.kt                # Gating condition in perform() & rememberTullabHapticFeedback()
│       ├── preferences/
│       │   └── UserPreferencesProvider.kt      # Update default LocalUserPreferences value
│       ├── AppViewModel.kt                     # Update defaultPreferences
│       └── screens/settings/
│           ├── SettingsViewModel.kt            # Add updateHapticFeedbackEnabled()
│           └── SettingsScreen.kt               # Add SettingSwitchRow in General section
└── res/
    ├── values/strings.xml                      # EN: settings_haptic_feedback_label & description
    ├── values-tr/strings.xml                   # TR: settings_haptic_feedback_label & description
    └── values-de/strings.xml                   # DE: settings_haptic_feedback_label & description

app/src/test/java/com/barutdev/tullab/
├── ui/components/
│   └── TullabHapticsTest.kt                    # Unit tests for haptic silencing logic
└── ui/screens/settings/
    └── SettingsViewModelTest.kt                # Unit tests for haptic toggle updating
```
