# Feature Specification: Haptic Feedback Toggle

**Feature Branch**: `014-haptic-feedback-toggle`

**Created**: 2026-09-09

**Status**: Draft

**Input**: User description: "Please add a user preference toggle for haptic feedback in Settings adhering to Material 3 standards: first, introduce a boolean setting hapticFeedbackEnabled (defaulting to true) persisted via the existing UserPreferences DataStore and repository flow; second, add an interactive Switch row for "Haptic Feedback" in SettingsScreen under the General section (using a dedicated vibration/haptic icon) with full linguistic parity across English, Turkish, and German; and third, update TullabHaptics.kt to check this preference state so that all tactile pulses and motor vibrations are completely silenced whenever the user disables haptic feedback in Settings."

## Clarifications

### Session 2026-09-09
- Q: What icon from `androidx.compose.material.icons` should be used for the "Haptic Feedback" row in the Settings screen? → A: `Icons.Outlined.Vibration` (standard device vibration symbol)
- Q: Should toggling the "Haptic Feedback" switch itself trigger a tactile click pulse when enabling or disabling the setting? → A: No haptic feedback on the switch itself (silent toggle, relies on visual feedback)
- Q: Where should the "Haptic Feedback" switch row be ordered inside the Settings screen's General section? → A: After Dark Mode, before Language (groups switch toggles together at top of General)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Persisted Haptic Preference with Sensible Default (Priority: P1)

As a tutor using Tullab, I want haptic feedback to be enabled by default but have my choice remembered, so that the app feels responsive on first use while respecting my preference across restarts.

**Why this priority**: Persistence is the foundation — without a stored, default-on preference, the Settings toggle and silencing behavior have no source of truth. This delivers standalone value (consistent behavior for fresh installs and upgrades).

**Independent Test**: Can be fully tested by fresh-installing the app (preference reports enabled), changing the value, force-stopping and relaunching the app, and verifying the stored value is retained and observable through the existing user-preferences flow.

**Acceptance Scenarios**:

1. **Given** a fresh install with no stored preference, **When** the app reads user preferences, **Then** haptic feedback is reported as enabled.
2. **Given** an existing install from before this feature (no stored key), **When** the app reads user preferences, **Then** haptic feedback is reported as enabled (missing key falls back to enabled).
3. **Given** any stored haptic preference value, **When** the device restarts or the app process is killed and relaunched, **Then** the previously stored value is restored.

---

### User Story 2 - Haptic Feedback Switch in Settings General Section (Priority: P1)

As a tutor sensitive to vibrations, I want a clearly labeled on/off switch for "Haptic Feedback" in the General section of Settings, so that I can silence all vibrations with one tap using a familiar, accessible control.

**Why this priority**: This is the primary user-visible slice — the control users interact with. It is independently demonstrable: open Settings → General → toggle the switch and see state persist.

**Independent Test**: Can be fully tested by opening Settings, locating the "Haptic Feedback" row with vibration icon in the General section, toggling it off and on, and verifying the switch thumb/track state updates immediately, persists after leaving and returning to Settings, and is announced correctly by accessibility services.

**Acceptance Scenarios**:

1. **Given** the user is on the Settings screen, **When** they view the General section, **Then** a "Haptic Feedback" row with a dedicated vibration/haptic icon and an interactive on/off switch is visible alongside other general preferences.
2. **Given** haptic feedback is currently enabled, **When** the user taps the "Haptic Feedback" switch (or the row), **Then** the switch moves to off, the new value is persisted, and the row reflects the disabled state without requiring an app restart.
3. **Given** haptic feedback is currently disabled, **When** the user taps the switch again, **Then** the switch moves to on, the new value is persisted, and the row reflects the enabled state.

---

### User Story 3 - Complete Silencing When Disabled (Priority: P2)

As a tutor who disabled haptic feedback, I want every tactile pulse and motor vibration in the app to stay silent, so that marking lessons paid, completing homework, selecting segments, or confirming deletions never vibrates unexpectedly.

**Why this priority**: This is the payoff of the toggle — respecting the user's choice everywhere. Independently testable by disabling the toggle and exercising all previously haptic actions.

**Independent Test**: Can be fully tested by disabling "Haptic Feedback" in Settings, then performing each haptic-triggering action (mark lesson paid, toggle homework completed, select segmented control, confirm deletion) and verifying no tactile pulse occurs; re-enabling restores the prior tactile behaviors unchanged.

**Acceptance Scenarios**:

1. **Given** haptic feedback is disabled in Settings, **When** the user marks a lesson as paid, toggles homework completion, selects a segmented option, or confirms a deletion, **Then** no tactile pulse or vibration is emitted for any of these actions.
2. **Given** haptic feedback is re-enabled in Settings, **When** the user performs the same actions, **Then** the established tactile behaviors (confirmation pulse, crisp click, selection pulse, warning haptic) occur as before.
3. **Given** haptic feedback is disabled, **When** the user performs any action, **Then** all non-haptic feedback (visual state changes, snackbars, undo actions) continues to work exactly as before.

---

### User Story 4 - Trilingual Label Parity (Priority: P3)

As a tutor using Tullab in English, Turkish, or German, I want the "Haptic Feedback" label to appear correctly translated in my language, so that the setting is clear and professional regardless of locale.

**Why this priority**: Tullab constitution mandates complete EN/TR/DE parity. Independently testable via locale switching without touching haptic logic.

**Independent Test**: Can be tested by switching the app language among English, Turkish, and German and verifying the Settings General section shows the correctly translated "Haptic Feedback" label with no hardcoded or missing strings.

**Acceptance Scenarios**:

1. **Given** the app language is English, **When** the user views Settings → General, **Then** the row reads "Haptic Feedback".
2. **Given** the app language is Turkish, **When** the user views Settings → General, **Then** the row reads the natural Turkish equivalent ("Dokunsal Geri Bildirim").
3. **Given** the app language is German, **When** the user views Settings → General, **Then** the row reads the natural German equivalent ("Haptisches Feedback").

---

### Edge Cases

- **Upgrade with missing key**: Users updating from a version without this setting have no stored key — the system treats the missing value as enabled rather than disabled or crashing.
- **Rapid toggling**: Toggling the switch repeatedly in quick succession results in the last state being the persisted state, with no stale-state flicker after navigation.
- **Devices without haptic hardware / OS-level haptics off**: Behavior remains silent and crash-free regardless of the in-app toggle; the toggle only gates app-requested pulses and never attempts to override system settings.
- **Locale switch after toggle**: Changing app language preserves the stored on/off value and only swaps the displayed label.
- **Accessibility**: The switch row meets the 48×48 dp minimum touch target, exposes meaningful switch semantics/state for screen readers, and remains legible at 200% system font scaling without clipping.
- **Visual feedback unaffected**: Disabling haptics never suppresses snackbars, undo actions, or visual state changes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST introduce a persisted boolean user preference for haptic feedback that defaults to enabled when no value has been stored.
- **FR-002**: The preference MUST be exposed through the existing user-preferences flow (persisted store plus observable repository stream) so Settings and haptic-triggering code observe the same source of truth, surviving process death and device restart.
- **FR-003**: The system MUST provide a dedicated update operation for the haptic preference so the Settings toggle can persist changes immediately.
- **FR-004**: The Settings screen MUST display an interactive "Haptic Feedback" switch row inside the General section positioned immediately after Dark Mode and before Language, using the `Icons.Outlined.Vibration` icon distinct from theme/language/currency icons, built with Material 3 switch components and following the existing settings-row visual pattern. Toggling the switch MUST be silent (emitting no tactile click or vibration on the switch itself).
- **FR-005**: The Settings switch MUST always reflect the current stored value and MUST update its visual state immediately upon user interaction without requiring an app restart or manual refresh.
- **FR-006**: All app-emitted tactile pulses and motor vibrations routed through the central haptics helper MUST consult the stored preference and MUST emit nothing when the preference is disabled.
- **FR-007**: When the preference is enabled, all established tactile behaviors (payment confirmation, homework click, segmented-selection pulse, deletion warning) MUST remain unchanged in type and intensity.
- **FR-008**: Disabling haptic feedback MUST NOT alter any visual feedback, snackbar, undo behavior, navigation, or data persistence — only tactile output is silenced.
- **FR-009**: The "Haptic Feedback" label MUST be externalized with complete parity across English (`values`), Turkish (`values-tr`), and German (`values-de`); hardcoded label strings in UI code are prohibited.
- **FR-010**: The switch row MUST meet accessibility requirements: minimum 48×48 dp touch target, proper switch role/state semantics for screen readers, and graceful layout at up to 200% system font scaling.
- **FR-011**: The feature MUST operate fully offline with no network access and MUST NOT introduce analytics, telemetry, or new permissions.

### Key Entities

- **HapticFeedbackPreference**: A boolean user preference (enabled by default) representing whether app-requested tactile feedback is allowed; persisted per user installation and observed as part of the overall user-preferences state.
- **Settings Haptic Row**: The General-section settings entry coupling the localized "Haptic Feedback" label, vibration icon, and on/off switch state bound to the stored preference.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can locate and flip the "Haptic Feedback" switch in Settings → General in under 15 seconds from opening Settings, with the new state visible immediately and retained after leaving Settings, killing the app, and relaunching.
- **SC-002**: With the setting disabled, 0 out of all haptic-triggering interactions (mark paid, homework toggle, segmented selection, deletion confirm) produce any vibration, while all visual confirmations and undo actions still appear.
- **SC-003**: With the setting enabled, 100% of the established tactile interactions produce feedback as before, confirming no regression.
- **SC-004**: 100% of users across English, Turkish, and German locales see a correctly translated, non-truncated "Haptic Feedback" label in Settings with zero hardcoded strings.

## Assumptions

- The existing user-preferences persistence (DataStore) and repository/ViewModel wiring are reused; no new storage mechanism or account system is introduced.
- The missing-key default of enabled applies both to fresh installs and to upgrades from versions predating this setting.
- `Icons.Outlined.Vibration` is used as the standard Material icon for the haptic feedback settings row.
- The central haptics helper is the single choke point for app-requested tactile output, so gating it covers all current and near-future call sites.
- This setting gates only in-app tactile requests; it does not change OS-level haptic settings, keyboard vibration, or other apps' behavior.
- Resetting app data / clearing preferences restores the default (enabled) state.
