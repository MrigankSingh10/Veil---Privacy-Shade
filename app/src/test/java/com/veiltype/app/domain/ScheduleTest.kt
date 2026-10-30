package com.veiltype.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleTest {
    @Test fun normalWindowHasInclusiveStartAndExclusiveEnd() {
        val schedule = Schedule(true, setOf(1), 9 * 60, 17 * 60)
        assertFalse(schedule.includes(1, 539)); assertTrue(schedule.includes(1, 540)); assertFalse(schedule.includes(1, 1020)); assertFalse(schedule.includes(2, 600))
    }
    @Test fun overnightBelongsToStartingDayIncludingWeekBoundary() {
        val schedule = Schedule(true, setOf(7), 22 * 60, 6 * 60)
        assertTrue(schedule.includes(7, 23 * 60)); assertTrue(schedule.includes(1, 5 * 60)); assertFalse(schedule.includes(1, 6 * 60)); assertFalse(schedule.includes(1, 23 * 60))
    }
    @Test fun allDayEmptyAndDisabled() {
        assertTrue(Schedule(true, setOf(2), 0, 0).includes(2, 1439))
        assertFalse(Schedule(true, emptySet(), 0, 0).includes(2, 0))
        assertTrue(Schedule(false, emptySet(), 0, 0).includes(2, 0))
    }
}
