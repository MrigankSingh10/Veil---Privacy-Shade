package com.veiltype.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.app.PendingIntent
import android.os.Build
import com.veiltype.app.VeilTypeApplication
import com.veiltype.app.platform.AccessibilityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = (context.applicationContext as VeilTypeApplication).settingsRepository.settings.first()
                if (settings.startOnBoot && settings.enabled && Settings.canDrawOverlays(context) && AccessibilityStatus.isEnabled(context)) {
                    runCatching { VeilForegroundService.start(context) }
                }
            } finally { pending.finish() }
        }
    }
}

abstract class BaseVeilTile : TileService() {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    protected val repository get() = (application as VeilTypeApplication).settingsRepository
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    protected fun openSetup() {
        val intent = Intent(this, com.veiltype.app.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        else { @Suppress("DEPRECATION") startActivityAndCollapse(intent) }
    }
}

class ArmTileService : BaseVeilTile() {
    override fun onStartListening() {
        super.onStartListening()
        scope.launch { qsTile?.apply { state = if (repository.settings.first().enabled && VeilForegroundService.running.value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE; updateTile() } }
    }
    override fun onClick() {
        super.onClick()
        scope.launch {
            val armed = repository.settings.first().enabled && VeilForegroundService.running.value
            if (armed) {
                repository.setEnabled(false)
                stopService(Intent(this@ArmTileService, VeilForegroundService::class.java))
            } else if (Settings.canDrawOverlays(this@ArmTileService) && AccessibilityStatus.isEnabled(this@ArmTileService)) {
                repository.setEnabled(true)
                runCatching { VeilForegroundService.start(this@ArmTileService) }.onFailure { repository.setEnabled(false) }
            } else { openSetup(); return@launch }
            qsTile?.apply { state = if (armed) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE; updateTile() }
        }
    }
}

class ScreenTileService : BaseVeilTile() {
    override fun onStartListening() {
        super.onStartListening()
        scope.launch { VeilForegroundService.fullScreenActive.collect { active -> qsTile?.apply { state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE; updateTile() } } }
    }
    override fun onClick() {
        super.onClick()
        if (VeilForegroundService.running.value) runCatching { VeilForegroundService.start(this, VeilForegroundService.ACTION_FULL_SCREEN) }
        else openSetup()
    }
}
