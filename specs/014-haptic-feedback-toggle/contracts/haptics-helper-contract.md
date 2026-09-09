# Component Contract: TullabHaptics Gating

## Overview

Central haptic helper contract governing the suppression of tactile pulses across the app.

---

## Contract: `TullabHapticFeedback`

**File**: `com.barutdev.tullab.ui.components.TullabHaptics.kt`

```kotlin
@Stable
class TullabHapticFeedback(
    private val view: View,
    private val composeHaptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val context: Context = view.context,
    val enabled: Boolean = true
) {
    /**
     * Performs haptic feedback for the given [type].
     * If [enabled] is false, this method immediately returns without performing
     * any View haptics or Vibrator fallback.
     */
    fun perform(type: TullabHapticFeedbackType) {
        if (!enabled) return
        // existing View & Vibrator dispatch...
    }
}
```

## Contract: `rememberTullabHapticFeedback`

```kotlin
/**
 * Creates and remembers a [TullabHapticFeedback] instance bound to the current Compose context
 * and current user preference state.
 */
@Composable
fun rememberTullabHapticFeedback(
    enabled: Boolean = LocalUserPreferences.current.hapticFeedbackEnabled
): TullabHapticFeedback {
    val view = LocalView.current
    val composeHaptics = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(view, composeHaptics, context, enabled) {
        TullabHapticFeedback(view, composeHaptics, context, enabled)
    }
}
```

### Guarantees
1. When `enabled == true`: All existing vibration patterns (Confirmation 70ms @ 255, Click 40ms @ 200, Segment Pulse 40ms @ 200, Warning dual 50ms @ 255) execute identically to before.
2. When `enabled == false`: 0 calls are made to `view.performHapticFeedback` and 0 calls are made to `vibrator.vibrate`.
3. Existing call sites require zero signature modifications; they automatically obtain the user's preference via `LocalUserPreferences.current.hapticFeedbackEnabled`.
