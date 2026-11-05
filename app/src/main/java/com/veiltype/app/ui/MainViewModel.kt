package com.veiltype.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.veiltype.app.VeilTypeApplication
import com.veiltype.app.domain.ActivationMode
import com.veiltype.app.domain.ShadeColor
import com.veiltype.app.domain.VeilSettings
import com.veiltype.app.platform.AccessibilityStatus
import com.veiltype.app.service.VeilAccessibilityService
import com.veiltype.app.service.VeilForegroundService
import com.veiltype.app.domain.AppRule
import com.veiltype.app.domain.Schedule
import com.veiltype.app.domain.MotionSpeed
import com.veiltype.app.domain.ControlPosition
import com.veiltype.app.domain.ControlShape
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val settings: VeilSettings = VeilSettings(),
    val accessibilityEnabled: Boolean = false,
    val serviceConnected: Boolean = false,
    val overlayAllowed: Boolean = false,
    val serviceRunning: Boolean = false,
    val keyboardVisible: Boolean = false,
    val controlVisible: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as VeilTypeApplication).settingsRepository
    private val accessibilityEnabled = MutableStateFlow(AccessibilityStatus.isEnabled(application))
    private val overlayAllowed = MutableStateFlow(Settings.canDrawOverlays(application))

    val uiState = combine(
        repository.settings,
        accessibilityEnabled,
        VeilAccessibilityService.snapshot,
        overlayAllowed,
        combine(VeilForegroundService.running, VeilForegroundService.controlVisible) { running, control -> running to control },
    ) { settings, access, snapshot, overlay, service -> MainUiState(settings, access, snapshot.connected, overlay, service.first, snapshot.keyboard != null, service.second) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun refreshPermission() { accessibilityEnabled.value = AccessibilityStatus.isEnabled(getApplication()); overlayAllowed.value = Settings.canDrawOverlays(getApplication()) }
    fun setEnabled(value: Boolean) = launch {
        val context = getApplication<Application>()
        if (value && (!Settings.canDrawOverlays(context) || !AccessibilityStatus.isEnabled(context))) return@launch
        repository.setEnabled(value)
        if (value) runCatching { VeilForegroundService.start(context) }.onFailure { repository.setEnabled(false) }
        else context.stopService(Intent(context, VeilForegroundService::class.java))
    }
    fun setOpacity(value: Float) = launch { repository.setOpacity(value) }
    fun setScreenOpacity(value: Float) = launch { repository.setScreenOpacity(value) }
    fun setMode(value: ActivationMode) = launch { repository.setMode(value) }
    fun setColor(value: ShadeColor) = launch { repository.setColor(value) }
    fun setFloatingControl(value: Boolean) = launch { repository.setFloatingControl(value) }
    fun acceptDisclosure() = launch { repository.acceptDisclosure() }
    fun replayOnboarding() = launch { repository.replayOnboarding() }
    fun setPhoto(value: String) = launch { repository.setPhoto(value) }
    fun setTouchThrough(value: Boolean) = launch { repository.setTouchThrough(value) }
    fun setOffset(value: Int) = launch { repository.setOffset(value) }
    fun setMotion(value: MotionSpeed) = launch { repository.setMotion(value) }
    fun setPosition(value: ControlPosition) = launch { repository.setPosition(value) }
    fun setShape(value: ControlShape) = launch { repository.setShape(value) }
    fun setControlSize(value: Int) = launch { repository.setControlSize(value) }
    fun setControlOpacity(value: Float) = launch { repository.setControlOpacity(value) }
    fun setControlColor(value: Int) = launch { repository.setControlColor(value) }
    fun setBoot(value: Boolean) = launch { repository.setBoot(value) }
    fun setAutoDefault(value: Boolean) = launch { repository.setAutoDefault(value) }
    fun setRule(packageName: String, value: AppRule) = launch { repository.setRule(packageName, value) }
    fun setSchedule(value: Schedule) = launch { repository.setSchedule(value) }

    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
