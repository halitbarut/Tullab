package com.barutdev.tullab.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.barutdev.tullab.ui.preferences.LocalUserPreferences

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
 * Haptic feedback helper that guarantees physical vibration delivery on legacy and
 * budget hardware with high-inertia ERM/rotor vibrators (e.g. Samsung Galaxy A50 on
 * Android 11).
 *
 * Strategy:
 * 1. Route through [View.performHapticFeedback] with
 *    [HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING] and
 *    [HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING] so OEM system-level toggles
 *    do not silently drop feedback.
 * 2. When [View.performHapticFeedback] returns false (unhandled / silent on legacy OEM
 *    devices), drive the system [Vibrator] directly (via [VibratorManager] on API 31+)
 *    with [AudioAttributes.USAGE_ASSISTANCE_SONIFICATION] and hardware-calibrated
 *    durations/amplitudes capable of spinning up physical ERM motors. Sub-20ms linear
 *    ticks (EFFECT_TICK / 10ms one-shots) fail silently on high-inertia rotors, so the
 *    fallback uses at least 40ms @ 200 for clicks/segments, 70ms @ 255 for payment
 *    confirmation, and a dual 50ms-burst + 50ms-pause pattern for warnings.
 * 3. All vibrator access is guarded by [Vibrator.hasVibrator] and try/catch (including
 *    [SecurityException] when the VIBRATE permission is missing) and degrades gracefully
 *    across API 26-36 (requires `<uses-permission android:name="android.permission.VIBRATE" />`).
 *
 * Obtain via [rememberTullabHapticFeedback] inside a Composable.
 */
@Stable
class TullabHapticFeedback(
    private val view: View,
    @Suppress("unused")
    private val composeHaptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    private val context: Context = view.context,
    val enabled: Boolean = true
) {
    companion object {
        // ERM-calibrated fallback parameters. Do not reduce below these values:
        // high-inertia rotor motors need ~30-40ms just to spin up.
        private const val CLICK_DURATION_MS = 40L
        private const val CLICK_AMPLITUDE = 200
        private const val SEGMENT_DURATION_MS = 40L
        private const val SEGMENT_AMPLITUDE = 200
        private const val CONFIRMATION_DURATION_MS = 70L
        private const val CONFIRMATION_AMPLITUDE = 255
        private const val WARNING_BURST_MS = 50L
        private const val WARNING_GAP_MS = 50L
        private const val WARNING_AMPLITUDE = 255

        private const val HAPTIC_FLAGS =
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
    }

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private val sonificationAudioAttributes: AudioAttributes? by lazy {
        try {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Performs haptic feedback for the given [type], using the best available constant
     * for the current Android version, with a fallback to the system [Vibrator] if unhandled.
     *
     * All branches route through [performViewHaptic] so OEM toggles are ignored via flags
     * and its boolean result can be observed: when it returns false (silent on Android 11 /
     * legacy OEM devices such as Samsung Galaxy), [performVibratorFallback] drives the system
     * [Vibrator] directly with ERM-calibrated [VibrationEffect]s (requires VIBRATE permission).
     */
    fun perform(type: TullabHapticFeedbackType) {
        if (!enabled) return
        val handled = when (type) {
            TullabHapticFeedbackType.CONFIRMATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30+: CONFIRM gives a satisfying double-tap confirm sensation
                    performViewHaptic(HapticFeedbackConstants.CONFIRM)
                } else {
                    // Legacy: LONG_PRESS is widely supported; false triggers Vibrator fallback.
                    performViewHaptic(HapticFeedbackConstants.LONG_PRESS)
                }
            }
            TullabHapticFeedbackType.CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    // API 27+: KEYBOARD_TAP gives a crisp physical click feel
                    performViewHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                } else {
                    performViewHaptic(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
            TullabHapticFeedbackType.SEGMENT_PULSE -> {
                if (Build.VERSION.SDK_INT >= 34 /* API 34 (UPSIDE_DOWN_CAKE) */) {
                    // API 34+: SEGMENT_TICK is purpose-built for segmented button selection
                    performViewHaptic(HapticFeedbackConstants.SEGMENT_TICK)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    performViewHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                } else {
                    performViewHaptic(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            TullabHapticFeedbackType.WARNING -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30+: REJECT gives a strong warning vibration
                    performViewHaptic(HapticFeedbackConstants.REJECT)
                } else {
                    performViewHaptic(HapticFeedbackConstants.LONG_PRESS)
                }
            }
        }

        if (!handled) {
            performVibratorFallback(type)
        }
    }

    /**
     * Wraps [View.performHapticFeedback] with flags that bypass OEM / system-level and
     * view-level haptic toggles. Returns false when the platform drops the request so the
     * caller can fall back to [Vibrator].
     */
    private fun performViewHaptic(feedbackConstant: Int): Boolean {
        return try {
            view.performHapticFeedback(feedbackConstant, HAPTIC_FLAGS)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * ERM-calibrated [Vibrator] fallback. Uses durations/amplitudes proven to spin up
     * high-inertia rotor motors where sub-20ms ticks are physically imperceptible:
     * - CLICK / SEGMENT_PULSE: 40ms @ 200
     * - CONFIRMATION: 70ms @ 255
     * - WARNING: dual 50ms bursts @ 255 separated by a 50ms pause.
     */
    private fun performVibratorFallback(type: TullabHapticFeedbackType) {
        val vib = vibrator ?: return
        try {
            if (!vib.hasVibrator()) return
        } catch (_: Throwable) {
            return
        }

        try {
            when (type) {
                TullabHapticFeedbackType.CONFIRMATION -> {
                    vibrateOneShot(
                        vib,
                        CONFIRMATION_DURATION_MS,
                        CONFIRMATION_AMPLITUDE
                    )
                }
                TullabHapticFeedbackType.CLICK -> {
                    vibrateOneShot(vib, CLICK_DURATION_MS, CLICK_AMPLITUDE)
                }
                TullabHapticFeedbackType.SEGMENT_PULSE -> {
                    vibrateOneShot(vib, SEGMENT_DURATION_MS, SEGMENT_AMPLITUDE)
                }
                TullabHapticFeedbackType.WARNING -> {
                    vibrateWarningPattern(vib)
                }
            }
        } catch (_: Throwable) {
            // Ignored if device does not permit vibration or effect fails
        }
    }

    private fun vibrateOneShot(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        try {
            if (!vibrator.hasVibrator()) return
        } catch (_: Throwable) {
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                vibrateEffect(vibrator, effect, durationMs)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: SecurityException) {
            // Missing VIBRATE permission — fail silently.
        } catch (_: Throwable) {
            // Effect unsupported on this device — last-resort legacy pulse.
            try {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            } catch (_: Throwable) {
                // Ignore.
            }
        }
    }

    private fun vibrateWarningPattern(vibrator: Vibrator) {
        try {
            if (!vibrator.hasVibrator()) return
        } catch (_: Throwable) {
            return
        }
        val timings = longArrayOf(0, WARNING_BURST_MS, WARNING_GAP_MS, WARNING_BURST_MS)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = try {
                    VibrationEffect.createWaveform(
                        timings,
                        intArrayOf(0, WARNING_AMPLITUDE, 0, WARNING_AMPLITUDE),
                        -1
                    )
                } catch (_: Throwable) {
                    // Some legacy drivers reject per-segment amplitudes.
                    VibrationEffect.createWaveform(timings, -1)
                }
                vibrateEffect(vibrator, effect, WARNING_BURST_MS + WARNING_GAP_MS + WARNING_BURST_MS)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: SecurityException) {
            // Missing VIBRATE permission — fail silently.
        } catch (_: Throwable) {
            // Last resort: single strong burst so at least something is felt.
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrateEffect(
                        vibrator,
                        VibrationEffect.createOneShot(WARNING_BURST_MS, WARNING_AMPLITUDE),
                        WARNING_BURST_MS
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(WARNING_BURST_MS)
                }
            } catch (_: Throwable) {
                // Ignore.
            }
        }
    }

    /**
     * Vibrates with [AudioAttributes.USAGE_ASSISTANCE_SONIFICATION] so the pulse is routed
     * as UI feedback, falling back to the plain [Vibrator.vibrate] overloads and finally to
     * the deprecated `vibrate(long)` on drivers that reject [VibrationEffect]s.
     */
    private fun vibrateEffect(vibrator: Vibrator, effect: VibrationEffect, legacyDurationMs: Long) {
        try {
            if (!vibrator.hasVibrator()) return
        } catch (_: Throwable) {
            return
        }
        val audioAttributes = sonificationAudioAttributes
        try {
            if (audioAttributes != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    vibrator.vibrate(effect, audioAttributes)
                    return
                } catch (_: Throwable) {
                    // Fall through to plain overload.
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(legacyDurationMs)
            }
        } catch (_: SecurityException) {
            // Missing VIBRATE permission — fail silently.
        } catch (_: Throwable) {
            try {
                @Suppress("DEPRECATION")
                vibrator.vibrate(legacyDurationMs)
            } catch (_: Throwable) {
                // Ignore — device cannot vibrate.
            }
        }
    }
}

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
