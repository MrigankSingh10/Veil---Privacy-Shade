package com.veiltype.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowRefreshSchedulerTest {
    private class Clock {
        var now = 0L
        val tasks = mutableListOf<Pair<Long, Runnable>>()
        fun advance(to: Long) {
            while (tasks.any { it.first <= to }) {
                val next = tasks.minBy { it.first }
                tasks.remove(next)
                now = next.first
                next.second.run()
            }
            now = to
        }
        fun scheduler(refresh: () -> Unit) = WindowRefreshScheduler(
            post = { task, delay -> tasks.add(now + delay to task) },
            remove = { task -> tasks.removeAll { it.second === task } },
            refresh = refresh,
        )
    }

    @Test fun detectsKeyboardThatBecomesAvailableAfterTheFirstScanWithoutAnotherEvent() {
        val clock = Clock()
        var keyboardSeen = false
        val scheduler = clock.scheduler { keyboardSeen = clock.now >= 250 }
        scheduler.transition()
        clock.advance(150)
        assertEquals(false, keyboardSeen)
        clock.advance(350)
        assertTrue(keyboardSeen)
        clock.advance(1000)
        assertTrue(clock.tasks.isEmpty())
    }

    @Test fun duplicateEventsDoNotMultiplyOrPostponeSettlingChecks() {
        val clock = Clock()
        val scans = mutableListOf<Long>()
        val scheduler = clock.scheduler { scans.add(clock.now) }
        repeat(20) { scheduler.transition() }
        clock.advance(700)
        assertEquals(listOf(50L, 150L, 350L, 700L), scans)
    }

    @Test fun protectedScreenCanRefreshImmediatelyAndShutdownCancelsRecovery() {
        val clock = Clock()
        var scans = 0
        val scheduler = clock.scheduler { scans++ }
        scheduler.transition()
        scheduler.immediate()
        assertEquals(1, scans)
        scheduler.cancel()
        clock.advance(1000)
        assertEquals(1, scans)
        scheduler.transition()
        clock.advance(1350)
        assertEquals(4, scans)
    }
}
