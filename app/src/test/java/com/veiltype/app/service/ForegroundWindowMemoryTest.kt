package com.veiltype.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForegroundWindowMemoryTest {
    @Test fun restoresKnownAppAfterSystemUiWithoutAnotherAppEvent() {
        val memory = ForegroundWindowMemory()
        memory.remember(10, "app.chat")
        assertNull(memory.resolve(10, protectedSurfaceActive = true))
        assertEquals("app.chat", memory.resolve(10, protectedSurfaceActive = false))
    }

    @Test fun switchingToAnotherKnownAppPreservesItsOwnRuleIdentity() {
        val memory = ForegroundWindowMemory()
        memory.remember(10, "app.chat")
        memory.remember(20, "app.skip")
        assertEquals("app.skip", memory.resolve(20, false))
        assertNull(memory.resolve(30, false))
        memory.clear()
        assertNull(memory.resolve(10, false))
    }

    @Test fun permissionApplicationIsNotMistakenForUnderlyingApp() {
        val memory = ForegroundWindowMemory()
        memory.remember(10, "app.chat")
        memory.remember(20, "com.android.permissioncontroller")
        assertEquals("com.android.permissioncontroller", memory.resolve(20, false))
        assertEquals("app.chat", memory.resolve(10, false))
    }
}
