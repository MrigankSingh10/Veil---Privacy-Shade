package com.veiltype.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundEventQueueTest {
    @Test fun coalescesRepeatedEventsButPreservesAppAndKeyboardTransitions() {
        val queue = ForegroundEventQueue()
        queue.add("app.one", 1)
        queue.add("app.one", 1)
        queue.add("keyboard", 2)
        queue.add("app.two", 3)
        queue.add("app.one", 1)
        assertEquals(listOf("app.one" to 1, "keyboard" to 2, "app.two" to 3, "app.one" to 1), queue.drain())
        assertTrue(queue.drain().isEmpty())
    }

    @Test fun interruptionDropsPendingEvents() {
        val queue = ForegroundEventQueue()
        queue.add("app.one", 1)
        queue.clear()
        assertTrue(queue.drain().isEmpty())
    }
}
