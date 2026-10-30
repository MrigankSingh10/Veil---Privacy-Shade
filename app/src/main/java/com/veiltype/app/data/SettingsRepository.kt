package com.veiltype.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.veiltype.app.domain.ActivationMode
import com.veiltype.app.domain.ShadeColor
import com.veiltype.app.domain.VeilSettings
import com.veiltype.app.domain.AppRule
import com.veiltype.app.domain.Schedule
import com.veiltype.app.domain.MotionSpeed
import com.veiltype.app.domain.ControlPosition
import com.veiltype.app.domain.ControlShape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore("veil_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val enabled = booleanPreferencesKey("enabled")
        val mode = stringPreferencesKey("activation_mode")
        val opacity = floatPreferencesKey("opacity")
        val screenOpacity = floatPreferencesKey("screen_opacity")
        val color = stringPreferencesKey("shade_color")
        val floatingControl = booleanPreferencesKey("floating_control")
        val disclosureAccepted = booleanPreferencesKey("disclosure_accepted")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val bubbleX = intPreferencesKey("bubble_x")
        val bubbleY = intPreferencesKey("bubble_y")
        val photo = stringPreferencesKey("photo_uri")
        val touch = booleanPreferencesKey("touch_through")
        val offset = intPreferencesKey("keyboard_offset")
        val motion = stringPreferencesKey("motion_speed")
        val position = stringPreferencesKey("control_position")
        val shape = stringPreferencesKey("control_shape")
        val size = intPreferencesKey("control_size")
        val controlOpacity = floatPreferencesKey("control_opacity")
        val controlColor = intPreferencesKey("control_color")
        val boot = booleanPreferencesKey("start_on_boot")
        val autoDefault = booleanPreferencesKey("auto_default")
        val rules = stringPreferencesKey("app_rules")
        val scheduleEnabled = booleanPreferencesKey("schedule_enabled")
        val scheduleDays = stringPreferencesKey("schedule_days")
        val scheduleStart = intPreferencesKey("schedule_start")
        val scheduleEnd = intPreferencesKey("schedule_end")
    }

    val settings: Flow<VeilSettings> = context.settingsDataStore.data
        .catch { error -> if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error }
        .map { p ->
            VeilSettings(
                enabled = p[Keys.enabled] ?: false,
                activationMode = p[Keys.mode].enumOr(ActivationMode.AUTOMATIC),
                opacity = (p[Keys.opacity] ?: 0.72f).coerceIn(0.2f, 0.95f),
                screenOpacity = (p[Keys.screenOpacity] ?: p[Keys.opacity] ?: 0.72f).coerceIn(0.2f, 0.95f),
                shadeColor = p[Keys.color].enumOr(ShadeColor.BLACK),
                floatingControl = p[Keys.floatingControl] ?: true,
                disclosureAccepted = p[Keys.disclosureAccepted] ?: false,
                onboardingComplete = p[Keys.onboardingComplete] ?: false,
                bubbleX = p[Keys.bubbleX] ?: 24,
                bubbleY = p[Keys.bubbleY] ?: 240,
                bubblePositionSaved = p[Keys.bubbleX] != null && p[Keys.bubbleY] != null,
                photoUri = p[Keys.photo] ?: "",
                touchThrough = p[Keys.touch] ?: false,
                keyboardOffsetDp = (p[Keys.offset] ?: 0).coerceIn(0, 160),
                motionSpeed = p[Keys.motion].enumOr(MotionSpeed.NORMAL),
                controlPosition = p[Keys.position].enumOr(ControlPosition.END),
                controlShape = p[Keys.shape].enumOr(ControlShape.CAPSULE),
                controlSize = (p[Keys.size] ?: 100).coerceIn(75, 150),
                controlOpacity = (p[Keys.controlOpacity] ?: 1f).coerceIn(.3f, 1f),
                controlColor = p[Keys.controlColor] ?: 0xFFB7A4E8.toInt(),
                startOnBoot = p[Keys.boot] ?: false,
                autoDefault = p[Keys.autoDefault] ?: (p[Keys.mode] == ActivationMode.AUTOMATIC.name),
                appRules = (p[Keys.rules] ?: "").split(';').mapNotNull { entry ->
                    val pair = entry.split('=', limit = 2)
                    if (pair.size != 2 || pair[0].isBlank()) null else pair[0] to pair[1].enumOr(AppRule.DEFAULT)
                }.toMap(),
                schedule = Schedule(
                    enabled = p[Keys.scheduleEnabled] ?: false,
                    days = p[Keys.scheduleDays]?.split(',')?.mapNotNull(String::toIntOrNull)?.filter { it in 1..7 }?.toSet() ?: (1..7).toSet(),
                    startMinute = (p[Keys.scheduleStart] ?: 0).coerceIn(0, 1439),
                    endMinute = (p[Keys.scheduleEnd] ?: 0).coerceIn(0, 1439),
                ),
            )
        }

    suspend fun setEnabled(value: Boolean) = update(Keys.enabled, value)
    suspend fun setMode(value: ActivationMode) = update(Keys.mode, value.name)
    suspend fun setOpacity(value: Float) = update(Keys.opacity, value.coerceIn(0.2f, 0.95f))
    suspend fun setScreenOpacity(value: Float) = update(Keys.screenOpacity, value.coerceIn(0.2f, 0.95f))
    suspend fun setColor(value: ShadeColor) = update(Keys.color, value.name)
    suspend fun setFloatingControl(value: Boolean) = update(Keys.floatingControl, value)
    suspend fun setPhoto(value: String) = update(Keys.photo, value)
    suspend fun setTouchThrough(value: Boolean) = update(Keys.touch, value)
    suspend fun setOffset(value: Int) = update(Keys.offset, value.coerceIn(0, 160))
    suspend fun setMotion(value: MotionSpeed) = update(Keys.motion, value.name)
    suspend fun setPosition(value: ControlPosition) = context.settingsDataStore.edit {
        it[Keys.position] = value.name
        it.remove(Keys.bubbleX)
        it.remove(Keys.bubbleY)
    }
    suspend fun setShape(value: ControlShape) = update(Keys.shape, value.name)
    suspend fun setControlSize(value: Int) = update(Keys.size, value.coerceIn(75, 150))
    suspend fun setControlOpacity(value: Float) = update(Keys.controlOpacity, value.coerceIn(.3f, 1f))
    suspend fun setControlColor(value: Int) = update(Keys.controlColor, value)
    suspend fun setBoot(value: Boolean) = update(Keys.boot, value)
    suspend fun setAutoDefault(value: Boolean) = update(Keys.autoDefault, value)
    suspend fun setRule(packageName: String, rule: AppRule) = context.settingsDataStore.edit { p ->
        val rules = (p[Keys.rules] ?: "").split(';').mapNotNull { entry ->
            val pair = entry.split('=', limit = 2)
            if (pair.size == 2 && pair[0].isNotBlank()) pair[0] to pair[1] else null
        }.toMap().toMutableMap()
        if (rule == AppRule.DEFAULT) rules.remove(packageName) else rules[packageName] = rule.name
        p[Keys.rules] = rules.entries.joinToString(";") { "${it.key}=${it.value}" }
    }
    suspend fun setSchedule(value: Schedule) = context.settingsDataStore.edit {
        it[Keys.scheduleEnabled] = value.enabled
        it[Keys.scheduleDays] = value.days.filter { day -> day in 1..7 }.sorted().joinToString(",")
        it[Keys.scheduleStart] = value.startMinute.coerceIn(0, 1439)
        it[Keys.scheduleEnd] = value.endMinute.coerceIn(0, 1439)
    }
    suspend fun replayOnboarding() = update(Keys.onboardingComplete, false)
    suspend fun acceptDisclosure() = context.settingsDataStore.edit {
        it[Keys.disclosureAccepted] = true
        it[Keys.onboardingComplete] = true
    }
    suspend fun saveBubblePosition(x: Int, y: Int) = context.settingsDataStore.edit {
        it[Keys.bubbleX] = x
        it[Keys.bubbleY] = y
    }

    private suspend fun <T> update(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { it[key] = value }
    }
}

private inline fun <reified T : Enum<T>> String?.enumOr(default: T): T =
    this?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
