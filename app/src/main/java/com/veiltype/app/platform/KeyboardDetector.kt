package com.veiltype.app.platform

import android.graphics.Rect
import android.view.accessibility.AccessibilityWindowInfo

data class KeyboardState(val visible: Boolean, val bounds: Rect? = null)

interface KeyboardDetector {
    fun detect(windows: List<AccessibilityWindowInfo>): KeyboardState
}

class AccessibilityKeyboardDetector : KeyboardDetector {
    override fun detect(windows: List<AccessibilityWindowInfo>): KeyboardState {
        val ime = windows
            .asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
            .map { window -> Rect().also(window::getBoundsInScreen) }
            .filter { it.width() > 0 && it.height() > 0 }
            .maxByOrNull { it.width() * it.height() }
        return KeyboardState(visible = ime != null, bounds = ime)
    }
}
