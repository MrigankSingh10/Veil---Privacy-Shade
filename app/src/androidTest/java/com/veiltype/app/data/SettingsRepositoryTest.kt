package com.veiltype.app.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veiltype.app.domain.ShadeColor
import com.veiltype.app.domain.AppRule
import com.veiltype.app.domain.Schedule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {
    @Test fun writesAndReadsClampedSettings() = runBlocking {
        val repository = SettingsRepository(ApplicationProvider.getApplicationContext())
        repository.setOpacity(2f)
        repository.setColor(ShadeColor.DEEP_PURPLE)
        val settings = repository.settings.first()
        assertEquals(0.95f, settings.opacity)
        assertEquals(ShadeColor.DEEP_PURPLE, settings.shadeColor)
    }

    @Test fun appRulesAndSchedulePersistAndCanBeCleared() = runBlocking {
        val repository = SettingsRepository(ApplicationProvider.getApplicationContext())
        val schedule = Schedule(enabled = true, days = setOf(1, 7), startMinute = 1320, endMinute = 360)
        repository.setRule("com.example.test", AppRule.AUTO_VEIL)
        repository.setSchedule(schedule)
        val stored = repository.settings.first()
        assertEquals(AppRule.AUTO_VEIL, stored.appRules["com.example.test"])
        assertEquals(schedule, stored.schedule)
        repository.setRule("com.example.test", AppRule.DEFAULT)
        repository.setSchedule(Schedule())
        assertEquals(null, repository.settings.first().appRules["com.example.test"])
    }
}
