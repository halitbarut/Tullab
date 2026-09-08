package com.barutdev.tullab.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Enum of semantic haptic feedback types used in Tullab.
 * Each type maps to the most appropriate haptic constant available on the device's API level.
 */
enum class TullabHapticFeedbackType {
    /** Confirmation pulse — used when marking a lesson as paid. */
    CONFIRMATION,

    /** Crisp click — used when toggling homework completion. */
    CLICK,

    /** Subtle tick — used on segmented button selection changes. */
    SEGMENT_PULSE,

    /** Warning pulse — used on destructive actions (delete lesson / homework). */
    WARNING
}

/**
 * Haptic feedback helper that wraps Android's [HapticFeedbackConstants] with graceful
 * API-level fallbacks and direct [Vibrator] backup when standard [View.performHapticFeedback]
 * is unhandled or silent on legacy OEM devices (such as Android 11 Samsung Galaxy devices).
 *
 * Obtain via [rememberTullabHapticFeedback] inside a Composable.
 */
@Stable
class TullabHapticFeedback(
    private val view: View,
    private val composeHaptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val context: Context = view.context
) {
    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Performs haptic feedback for the given [type], using the best available constant
     * for the current Android version, with a fallback to the system [Vibrator] if unhandled.
     */
    fun perform(type: TullabHapticFeedbackType) {
        val handled = when (type) {
            TullabHapticFeedbackType.CONFIRMATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30+: CONFIRM gives a satisfying double-tap confirm sensation
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    true
                }
            }
            TullabHapticFeedbackType.CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    // API 27+: KEYBOARD_TAP gives a crisp physical click feel
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } else {
                    composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    true
                }
            }
            TullabHapticFeedbackType.SEGMENT_PULSE -> {
                if (Build.VERSION.SDK_INT >= 34 /* API 34 (UPSIDE_DOWN_CAKE) */) {
                    // API 34+: SEGMENT_TICK is purpose-built for segmented button selection
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } else {
                    composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    true
                }
            }
            TullabHapticFeedbackType.WARNING -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30+: REJECT gives a strong warning vibration
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                } else {
                    composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    true
                }
            }
        }

        if (!handled) {
            performVibratorFallback(type)
        }
    }

    private fun performVibratorFallback(type: TullabHapticFeedbackType) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            when (type) {
                TullabHapticFeedbackType.CONFIRMATION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Predefined click or dual pulse
                        val effect = VibrationEffect.createWaveform(longArrayOf(0, 35, 60, 45), -1)
                        vib.vibrate(effect)
                    } else {
                        vib.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                TullabHapticFeedbackType.CLICK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } catch (_: Throwable) {
                            vib.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        vib.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                TullabHapticFeedbackType.SEGMENT_PULSE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        } catch (_: Throwable) {
                            vib.vibrate(VibrationEffect.createOneShot(10L, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        vib.vibrate(VibrationEffect.createOneShot(10L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                TullabHapticFeedbackType.WARNING -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                        } catch (_: Throwable) {
                            vib.vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        vib.vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
        } catch (_: Throwable) {
            // Ignored if device does not permit vibration or effect fails
        }
    }
}

/**
 * Creates and remembers a [TullabHapticFeedback] instance bound to the current Compose context.
 */
@Composable
fun rememberTullabHapticFeedback(): TullabHapticFeedback {
    val view = LocalView.current
    val composeHaptics = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(view, composeHaptics, context) {
        TullabHapticFeedback(view, composeHaptics, context)
    }
}
