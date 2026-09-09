# Tasks: Haptic Feedback Toggle

**Branch**: `014-haptic-feedback-toggle` | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/014-haptic-feedback-toggle/spec.md) | **Plan**: [plan.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/014-haptic-feedback-toggle/plan.md)

---

## Phase 1: Setup & Foundational Infrastructure

**Purpose**: Core model and persistence prerequisites required across all user stories.

- [X] T001 [P] Add `hapticFeedbackEnabled: Boolean = true` property to `UserPreferences` domain model in `app/src/main/java/com/barutdev/tullab/domain/model/UserPreferences.kt`
- [X] T002 [P] Add `updateHapticFeedbackEnabled(isEnabled: Boolean)` method declaration to `UserPreferencesRepository` interface in `app/src/main/java/com/barutdev/tullab/domain/repository/UserPreferencesRepository.kt`
- [X] T003 Implement `HAPTIC_FEEDBACK_ENABLED_KEY`, DataStore reading with `true` default, `updateHapticFeedbackEnabled(isEnabled: Boolean)`, and reset handling in `app/src/main/java/com/barutdev/tullab/data/repository/UserPreferencesRepository.kt`
- [X] T004 [P] Update `LocalUserPreferences` static composition default with `hapticFeedbackEnabled = true` in `app/src/main/java/com/barutdev/tullab/ui/preferences/UserPreferencesProvider.kt`
- [X] T005 [P] Update `defaultPreferences` in `AppViewModel` with `hapticFeedbackEnabled = true` in `app/src/main/java/com/barutdev/tullab/ui/AppViewModel.kt`
- [X] T006 [P] Update `defaultPreferences` in `SettingsViewModel` with `hapticFeedbackEnabled = true` in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsViewModel.kt`

**Checkpoint**: Foundation ready — `hapticFeedbackEnabled` is established in the domain and DataStore layers with a non-breaking `true` default.

---

## Phase 2: User Story 4 - Trilingual Label Parity (Priority: P3)

**Goal**: Provide all localized strings for the Haptic Feedback toggle across English, Turkish, and German before UI construction.

**Independent Test**: Switch app language between English, Turkish, and German and verify `R.string.settings_haptic_feedback_label` and `R.string.settings_haptic_feedback_content_description` resolve without error.

### Implementation for User Story 4

- [X] T007 [P] [US4] Add English strings `settings_haptic_feedback_label` ("Haptic Feedback") and `settings_haptic_feedback_content_description` ("Toggle haptic feedback") to `app/src/main/res/values/strings.xml`
- [X] T008 [P] [US4] Add Turkish strings `settings_haptic_feedback_label` ("Dokunsal Geri Bildirim") and `settings_haptic_feedback_content_description` ("Dokunsal geri bildirimi değiştir") to `app/src/main/res/values-tr/strings.xml`
- [X] T009 [P] [US4] Add German strings `settings_haptic_feedback_label` ("Haptisches Feedback") and `settings_haptic_feedback_content_description` ("Haptisches Feedback umschalten") to `app/src/main/res/values-de/strings.xml`

**Checkpoint**: All three languages have full parity for the haptic toggle strings.

---

## Phase 3: User Story 1 & 2 - Settings Toggle & Persistence (Priority: P1) 🎯 MVP

**Goal**: Enable users to view and interact with the "Haptic Feedback" switch in Settings under the General section, immediately persisting state changes via DataStore.

**Independent Test**: Open Settings → General, observe the Haptic Feedback switch (default ON) between Dark Mode and Language with `Icons.Outlined.Vibration`. Tap to toggle OFF and ON; verify state changes immediately and persists across app force-stop and restart.

### Tests for User Story 1 & 2

- [X] T010 [P] [US1] Add unit test in `app/src/test/java/com/barutdev/tullab/ui/screens/settings/SettingsViewModelTest.kt` verifying `updateHapticFeedbackEnabled(Boolean)` delegates correctly to `UserPreferencesRepository`

### Implementation for User Story 1 & 2

- [X] T011 [US1] Implement `updateHapticFeedbackEnabled(isEnabled: Boolean)` in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsViewModel.kt`
- [X] T012 [US2] Add `SettingSwitchRow` for Haptic Feedback (with `Icons.Outlined.Vibration`, silent toggle interaction, and localized strings) after Dark Mode and before Language in `app/src/main/java/com/barutdev/tullab/ui/screens/settings/SettingsScreen.kt`

**Checkpoint**: Users can toggle Haptic Feedback in Settings and have their preference persisted.

---

## Phase 4: User Story 3 - Complete Silencing When Disabled (Priority: P2)

**Goal**: Gate all app-emitted tactile pulses and motor vibrations through the stored preference, silencing vibration output completely when disabled while keeping visual and undo feedback intact.

**Independent Test**: Disable Haptic Feedback in Settings, then perform actions (mark lesson paid in Calendar/Dashboard, toggle homework completion, select segments, confirm deletion). Verify zero physical vibration occurs while all visual dialogs, snackbars, and animations continue working. Re-enable to confirm all vibrations return.

### Tests for User Story 3

- [X] T013 [P] [US3] Add unit test in `app/src/test/java/com/barutdev/tullab/ui/components/TullabHapticsTest.kt` verifying `TullabHapticFeedback(enabled = false)` suppresses view haptic feedback and vibrator calls for all feedback types

### Implementation for User Story 3

- [X] T014 [US3] Update `TullabHapticFeedback` class in `app/src/main/java/com/barutdev/tullab/ui/components/TullabHaptics.kt` to accept an `enabled: Boolean = true` parameter and guard `perform(type)` with `if (!enabled) return`
- [X] T015 [US3] Update `rememberTullabHapticFeedback()` in `app/src/main/java/com/barutdev/tullab/ui/components/TullabHaptics.kt` to inject `LocalUserPreferences.current.hapticFeedbackEnabled` as the default `enabled` parameter

**Checkpoint**: Silencing is completely enforced at the centralized helper choke point; all screens immediately respect the setting without call-site rewrites.

---

## Phase 5: Polish & Regression Verification

**Purpose**: Verify end-to-end integration, run automated test suites, and ensure zero regressions across existing screens.

- [X] T016 Run automated unit tests (`./gradlew testDebugUnitTest`) to verify all existing and new ViewModel, repository, and haptics tests compile and pass
- [X] T017 [P] Verify debug build compilation and resource checks via `./gradlew assembleDebug`
- [X] T018 Execute manual validation scenarios from `specs/014-haptic-feedback-toggle/quickstart.md` (fresh install default, toggling, silencing across screens, trilingual parity, and accessibility checks including 48x48 dp touch target and 200% font scaling)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup & Foundational (Phase 1)**: No dependencies — starts immediately.
- **Trilingual Parity (Phase 2)**: Can run in parallel with or immediately after Phase 1.
- **User Story 1 & 2 (Phase 3)**: Depends on Phase 1 and Phase 2 completion.
- **User Story 3 (Phase 4)**: Depends on Phase 1 (domain preference model).
- **Polish (Phase 5)**: Depends on Phases 1 through 4 completion.

### Parallel Opportunities

- `T001`, `T002`, `T004`, `T005`, `T006` can be edited in parallel once `UserPreferences` signature is updated.
- String externalizations (`T007`, `T008`, `T009`) in `values/`, `values-tr/`, and `values-de/` can be executed completely in parallel.
- `T010` (test) and `T013` (test) can run alongside their respective implementations.
- `T014` and `T015` in `TullabHaptics.kt` can be implemented independently of `SettingsScreen.kt`.

---

## Implementation Strategy (MVP First)

1. **Foundations**: Update `UserPreferences`, `UserPreferencesRepository`, and provider defaults.
2. **Strings**: Add trilingual string keys (EN, TR, DE).
3. **MVP (US1 & US2)**: Implement ViewModel update and `SettingsScreen` switch row. Test persistence independently.
4. **Enforcement (US3)**: Add unit test for silencing and gate `TullabHaptics.kt` with `LocalUserPreferences.current.hapticFeedbackEnabled`.
5. **Verification**: Run `./gradlew testDebugUnitTest` and manual quickstart steps.
