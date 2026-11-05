package com.veiltype.app.platform

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundEventFilterTest {
    private fun track(pkg: String, kind: WindowKind, protected: Boolean = false) =
        ForegroundEventFilter.shouldTrack(pkg, "com.veiltype.app", "com.keyboard", kind, protected)

    @Test fun keyboardEventsNeverReplaceForegroundApp() {
        assertFalse(track("com.keyboard", WindowKind.APPLICATION))
        assertFalse(track("android", WindowKind.KEYBOARD, protected = true))
    }

    @Test fun appAndProtectedSystemEventsAreTracked() {
        assertTrue(track("com.example", WindowKind.APPLICATION))
        assertTrue(track("com.android.systemui", WindowKind.OTHER, protected = true))
        assertFalse(track("com.android.systemui", WindowKind.OTHER, protected = false))
        assertTrue(track("com.example", WindowKind.UNKNOWN))
        assertFalse(track("com.veiltype.app", WindowKind.UNKNOWN))
    }
}
