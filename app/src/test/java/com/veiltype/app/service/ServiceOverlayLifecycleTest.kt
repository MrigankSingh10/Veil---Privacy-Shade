package com.veiltype.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceOverlayLifecycleTest {
    @Test fun interruptAndDestroyAlwaysRemoveOverlays() {
        var cleanups = 0
        val lifecycle = ServiceOverlayLifecycle { cleanups++ }

        lifecycle.onConnected()
        assertTrue(lifecycle.connected)
        lifecycle.onInterrupted()
        assertEquals(1, cleanups)
        assertTrue(lifecycle.connected)

        lifecycle.onDestroyed()
        assertEquals(2, cleanups)
        assertFalse(lifecycle.connected)
    }
}
