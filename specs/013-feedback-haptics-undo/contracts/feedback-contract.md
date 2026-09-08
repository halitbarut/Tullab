# Interface Contracts: Feedback & Haptic Utilities

**Feature**: Comprehensive Haptic & Visual Feedback with Undo (`013-feedback-haptics-undo`)
**Date**: 2026-09-08

## 1. Haptic Feedback Interface Contract

### `HapticFeedbackHelper`
Located in `ui/theme/` or `util/`:

```kotlin
enum class TullabHapticFeedbackType {
    CONFIRMATION,   // Mark as paid
    CLICK,          // Homework completion toggle
    SEGMENT_PULSE,  // Segmented button selection
    WARNING         // Destructive action confirmation (delete)
}

interface TullabHapticFeedback {
    fun perform(type: TullabHapticFeedbackType)
}
```

Implementation will bind to Android `LocalHapticFeedback` and `View.performHapticFeedback` to leverage standard API 26-36 haptics cleanly with graceful fallback.

---

## 2. Scaffold Visual Feedback Contract

### `TullabScaffoldController` Extensions

```kotlin
class TullabScaffoldController internal constructor(
    val snackbarHostState: SnackbarHostState
) {
    // Existing chrome functions...

    /**
     * Displays a transient snackbar with an actionable Undo callback.
     * If a prior undo action is active, it is committed before displaying the new one.
     */
    suspend fun showUndoSnackbar(
        message: String,
        actionLabel: String,
        onUndo: suspend () -> Unit
    )

    /**
     * Displays a standard informational message (e.g. backup status).
     */
    suspend fun showMessage(message: String)
}
```
