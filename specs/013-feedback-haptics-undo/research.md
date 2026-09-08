# Phase 0: Research & Technical Analysis

**Feature**: Comprehensive Haptic & Visual Feedback with Undo (`013-feedback-haptics-undo`)
**Date**: 2026-09-08

## 1. Tactile Feedback via `LocalHapticFeedback`

### Decision
Use `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType)` in Jetpack Compose:
- **Confirmation Haptic (Payment recorded)**: `HapticFeedbackType.Confirm` (or fallback to `HapticFeedbackType.LongPress` where API level requires compatibility).
- **Crisp Tactile Click (Homework completed)**: `HapticFeedbackType.TextHandleMove` or `HapticFeedbackType.Confirm` for a distinct click, or `HapticFeedbackType.LongPress` / `LocalView.current.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`. In Compose's standard `HapticFeedbackType`, `Confirm` and `LongPress` are available. Alternatively, provide a helper utility `AppHaptics.perform(HapticType)` that gracefully checks Android SDK level and executes `ViewCompat`/`HapticFeedbackConstants` (e.g. `CONFIRM`, `REJECT`, `KEYBOARD_TAP`, `SEGMENT_TICK` on API 34+ and fallback on earlier versions).
- **Segmented Button Selection**: `HapticFeedbackType.TextHandleMove` or `HapticFeedbackConstants.SEGMENT_TICK` (API 34+) / `KEYBOARD_TAP`.
- **Destructive Deletion Warning**: `HapticFeedbackConstants.REJECT` (API 34+) or `HapticFeedbackType.LongPress` as warning feedback.

### Rationale
- Jetpack Compose provides `LocalHapticFeedback`, but Android 14 (API 34) and Android 11 introduced specialized constants like `CONFIRM`, `REJECT`, `SEGMENT_TICK`, and `GESTURE_START`.
- Wrapping this in an offline-safe UI utility `HapticFeedbackManager` (or `LocalHapticFeedbackHelper`) enables high-fidelity feedback on modern devices while falling back seamlessly on API 26+ without crashes or runtime exceptions.

---

## 2. Visual Feedback & Undo Orchestration

### Decision
Extend `TullabScaffoldController` and introduce an `UndoManager` / `FeedbackController`:
1. `TullabScaffoldController` already holds the root `SnackbarHostState` rendered at `TullabNavGraph.kt` (lines 590-596).
2. Add a structured method on `TullabScaffoldController` (or via an injected `FeedbackManager` / `UiEventManager`):
   ```kotlin
   suspend fun showUndoSnackbar(
       message: String,
       actionLabel: String,
       onUndo: suspend () -> Unit
   )
   ```
3. When `showUndoSnackbar` is called:
   - Any currently active snackbar is dismissed without invoking the previous undo callback (only the latest action is undoable).
   - It calls `snackbarHostState.showSnackbar(message, actionLabel = actionLabel, duration = SnackbarDuration.Short)`.
   - If the returned `SnackbarResult == SnackbarResult.ActionPerformed`, execute `onUndo()`.
4. For plain notifications (like CSV backup export/import):
   ```kotlin
   suspend fun showSnackbar(message: String)
   ```

### Rationale
- `TullabScaffoldController` is already scoped at the root Composable and passed via `LocalTullabScaffoldController`.
- Storing the active undo action inside the controller or screen ViewModel coroutine ensures that screen navigation does NOT cancel the Snackbar or leak screen-specific ViewModels.

---

## 3. Localization & Trilingual Parity

### Decision
Define standard string resources in `strings.xml` for all 3 locales:
- English (`values/strings.xml`)
- Turkish (`values-tr/strings.xml`)
- German (`values-de/strings.xml`)

Required string keys:
- `snackbar_payment_recorded`: "Payment of %1$s recorded" / "%1$s tutarında ödeme kaydedildi" / "Zahlung von %1$s erfasst"
- `snackbar_homework_completed`: "Homework completed" / "Ödev tamamlandı" / "Hausaufgabe abgeschlossen"
- `snackbar_lesson_deleted`: "Lesson deleted" / "Ders silindi" / "Unterrichtsstunde gelöscht"
- `snackbar_homework_deleted`: "Homework deleted" / "Ödev silindi" / "Hausaufgabe gelöscht"
- `snackbar_action_undo`: "Undo" / "Geri Al" / "Rückgängig"

### Rationale
Satisfies Constitution Principle VII (Internationalization & Linguistic Parity).
