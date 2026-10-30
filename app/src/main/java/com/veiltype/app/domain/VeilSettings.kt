package com.veiltype.app.domain

enum class ActivationMode { AUTOMATIC, MANUAL }

enum class AppRule { DEFAULT, AUTO_VEIL, FULL_SCREEN, SKIP }
enum class OverlayMode { HIDDEN, KEYBOARD, FULL_SCREEN }
enum class MotionSpeed(val durationMs: Long) { INSTANT(0), FAST(120), NORMAL(250), SLOW(500) }
enum class ControlPosition { START, CENTER, END }
enum class ControlShape { CAPSULE, SOFT, SHARP }

data class Schedule(
    val enabled: Boolean = false,
    val days: Set<Int> = (1..7).toSet(), // java.time.DayOfWeek values
    val startMinute: Int = 0,
    val endMinute: Int = 0,
) {
    fun includes(day: Int, minute: Int): Boolean {
        if (!enabled) return true
        val previous = if (day == 1) 7 else day - 1
        return when {
            startMinute == endMinute -> day in days
            startMinute < endMinute -> day in days && minute in startMinute until endMinute
            minute >= startMinute -> day in days
            minute < endMinute -> previous in days
            else -> false
        }
    }
}

data class OverlayInputs(
    val armed: Boolean,
    val permissions: Boolean,
    val safe: Boolean,
    val keyboardVisible: Boolean,
    val rule: AppRule,
    val scheduled: Boolean,
    val manualKeyboard: Boolean,
    val manualFullScreen: Boolean,
    val controlDismissed: Boolean,
    val peeking: Boolean,
    val automaticDismissed: Boolean = false,
    val autoDefault: Boolean = false,
)

data class OverlayDecision(val mode: OverlayMode, val showControl: Boolean)

object OverlayReducer {
    fun decide(i: OverlayInputs): OverlayDecision {
        if (!i.armed || !i.permissions || !i.safe) return OverlayDecision(OverlayMode.HIDDEN, false)
        val skipped = i.rule == AppRule.SKIP
        val mode = when {
            i.peeking -> OverlayMode.HIDDEN
            i.manualFullScreen -> OverlayMode.FULL_SCREEN
            skipped -> OverlayMode.HIDDEN
            i.scheduled && !i.automaticDismissed && i.rule == AppRule.FULL_SCREEN -> OverlayMode.FULL_SCREEN
            i.keyboardVisible && (i.manualKeyboard || (i.scheduled && !i.automaticDismissed && (i.rule == AppRule.AUTO_VEIL || i.rule == AppRule.DEFAULT && i.autoDefault))) -> OverlayMode.KEYBOARD
            else -> OverlayMode.HIDDEN
        }
        return OverlayDecision(mode, !i.controlDismissed && ((i.keyboardVisible && !skipped) || mode == OverlayMode.FULL_SCREEN))
    }
}

enum class ShadeColor(val argb: Long, val label: String) {
    BLACK(0xFF000000, "Black"),
    DARK_GRAY(0xFF25232A, "Dark gray"),
    DEEP_PURPLE(0xFF241B35, "Deep purple"),
}

data class VeilSettings(
    val enabled: Boolean = false,
    val activationMode: ActivationMode = ActivationMode.AUTOMATIC,
    val opacity: Float = 0.72f,
    val screenOpacity: Float = 0.72f,
    val shadeColor: ShadeColor = ShadeColor.BLACK,
    val floatingControl: Boolean = true,
    val disclosureAccepted: Boolean = false,
    val onboardingComplete: Boolean = false,
    val bubbleX: Int = 24,
    val bubbleY: Int = 240,
    val bubblePositionSaved: Boolean = false,
    val photoUri: String = "",
    val touchThrough: Boolean = false,
    val keyboardOffsetDp: Int = 0,
    val motionSpeed: MotionSpeed = MotionSpeed.NORMAL,
    val controlPosition: ControlPosition = ControlPosition.END,
    val controlShape: ControlShape = ControlShape.CAPSULE,
    val controlSize: Int = 100,
    val controlOpacity: Float = 1f,
    val controlColor: Int = 0xFFB7A4E8.toInt(),
    val startOnBoot: Boolean = false,
    val autoDefault: Boolean = false,
    val appRules: Map<String, AppRule> = emptyMap(),
    val schedule: Schedule = Schedule(),
)
