# Research: Haptic Feedback Toggle

## Research Questions & Decisions

### 1. Data Model & Persistence Extension for Haptic Feedback

- **Decision**: Add `val hapticFeedbackEnabled: Boolean = true` to `UserPreferences` data class, create `val HAPTIC_FEEDBACK_ENABLED_KEY = booleanPreferencesKey("haptic_feedback_enabled")` in `UserPreferencesRepository` data layer, and provide `suspend fun updateHapticFeedbackEnabled(isEnabled: Boolean)` on `UserPreferencesRepository`.
- **Rationale**:
  - `UserPreferences` is the single source of truth for user settings across the application.
  - Adding a default value `true` to `val hapticFeedbackEnabled: Boolean = true` ensures backward compatibility for existing callers and test mocks where not all constructor parameters are explicitly passed.
  - In `UserPreferencesRepository.kt` (data layer), reading `preferences[HAPTIC_FEEDBACK_ENABLED_KEY] ?: true` gracefully handles both fresh installs and upgrades where the preference key does not exist yet.
  - Follows existing pattern in `UserPreferencesRepository`: `updateTheme(isDarkMode)`, `updateLessonRemindersEnabled(isEnabled)`.
- **Alternatives Considered**:
  - Separate `HapticPreferences` Datastore: Overkill, violates Clean Architecture and adds multi-datastore synchronization complexity.
  - Defaulting to `false`: Rejected because Tullab delivers haptic feedback as a core polish/accessibility feature out of the box.

### 2. Gating Mechanism in `TullabHaptics`

- **Decision**: Update `TullabHapticFeedback` to accept an `enabled: Boolean = true` parameter, and supply it via `rememberTullabHapticFeedback(enabled: Boolean = LocalUserPreferences.current.hapticFeedbackEnabled)`. In `perform(type: TullabHapticFeedbackType)`, check `if (!enabled) return` at the very entry point before evaluating `performViewHaptic` or `performVibratorFallback`.
- **Rationale**:
  - `TullabHapticFeedback` is the central choke point for all tactile output in Tullab (payment confirmation, homework toggle click, segmented buttons, warning vibration).
  - Checking `if (!enabled) return` in `perform()` guarantees 100% suppression of both `View.performHapticFeedback` and direct `Vibrator` fallback.
  - Connecting `rememberTullabHapticFeedback()` to `LocalUserPreferences.current.hapticFeedbackEnabled` ensures that all existing call sites (`CalendarScreen`, `HomeworkScreen`, `DashboardScreen`, `LogLessonDialog`) automatically respect the user's preference without needing individual code rewrites or extra parameters at every call site.
  - Allows unit testability and manual override if a caller ever passes `enabled = false` explicitly.
- **Alternatives Considered**:
  - Injecting `UserPreferencesRepository` into `TullabHapticFeedback`: Violates Compose UI conventions; `TullabHapticFeedback` is a UI helper tied to `View` and `LocalContext`. `LocalUserPreferences` is already provided at the root Compose level in `MainActivity`.
  - Checking preferences at each individual call site in screen composables: Highly error-prone, duplicates logic, and breaks centralized control.

### 3. Settings Screen UI Pattern & Positioning

- **Decision**:
  - Add `SettingSwitchRow` inside the `General` section of `SettingsScreen.kt` immediately after `Dark Mode` and before `Language`.
  - Use `Icons.Outlined.Vibration` for the row icon.
  - Icon content description: `stringResource(R.string.settings_haptic_feedback_content_description)`.
  - Title: `stringResource(R.string.settings_haptic_feedback_label)`.
  - Checked: `userPreferences.hapticFeedbackEnabled`.
  - On checked change: `viewModel.updateHapticFeedbackEnabled(it)`.
  - Silent toggle: Does not trigger any haptic pulse on click/toggle; solely relies on Material 3 `Switch` visual thumb/track animation.
- **Rationale**:
  - Dark Mode and Haptic Feedback are both binary on/off switches, while Language and Currency open selection dialogs. Grouping switches together at the top of the General section provides clean visual consistency.
  - Directly fulfills the clarification agreement from Phase 0 / spec clarifications.
- **Alternatives Considered**:
  - Placing at the bottom of General section: Separates switches with dialog rows, resulting in inconsistent UI grouping.
  - Dedicated "Sound & Haptics" section: Tullab only has haptics (no audio sound effects); a single toggle does not warrant a new section.

### 4. Internationalization & Localization Parity

- **Decision**:
  - English (`values/strings.xml`):
    - `settings_haptic_feedback_label`: "Haptic Feedback"
    - `settings_haptic_feedback_content_description`: "Toggle haptic feedback"
  - Turkish (`values-tr/strings.xml`):
    - `settings_haptic_feedback_label`: "Dokunsal Geri Bildirim"
    - `settings_haptic_feedback_content_description`: "Dokunsal geri bildirimi değiştir"
  - German (`values-de/strings.xml`):
    - `settings_haptic_feedback_label`: "Haptisches Feedback"
    - `settings_haptic_feedback_content_description`: "Haptisches Feedback umschalten"
- **Rationale**:
  - Satisfies Tullab Constitution Principle VII (strict linguistic parity across English, Turkish, and German).
  - Terminology matches standard Android OS translations for haptics.
- **Alternatives Considered**:
  - "Titreşim" (Vibration) in TR: "Dokunsal Geri Bildirim" is more accurate for Material 3 haptic interactions and matches modern Android Turkish locale standards.

### 5. Testing Strategy

- **Decision**:
  - **Unit Tests**:
    - `SettingsViewModelTest`: Verify `updateHapticFeedbackEnabled` calls repository with expected boolean.
    - `UserPreferencesRepositoryTest` or mock repository test: Verify default value is `true`, updating emits updated value, and missing key resolves to `true`.
    - `TullabHapticsTest`: Verify `TullabHapticFeedback` performs nothing when `enabled = false`.
  - **UI / Preview / Static Analysis**:
    - Run `./gradlew test` to ensure all existing tests pass with the new data class property.
    - Verify `strings.xml` in all three locales.
