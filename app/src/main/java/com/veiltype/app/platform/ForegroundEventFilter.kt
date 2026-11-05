package com.veiltype.app.platform

/** Decides which window-state events can change the app used for privacy rules. */
object ForegroundEventFilter {
    fun shouldTrack(
        packageName: String,
        ownPackage: String,
        keyboardPackage: String?,
        windowKind: WindowKind,
        protectedPackage: Boolean,
    ): Boolean {
        if (packageName == keyboardPackage || windowKind == WindowKind.KEYBOARD) return false
        return windowKind == WindowKind.APPLICATION || protectedPackage ||
            (windowKind == WindowKind.UNKNOWN && packageName != ownPackage)
    }
}

enum class WindowKind { APPLICATION, KEYBOARD, OTHER, UNKNOWN }
