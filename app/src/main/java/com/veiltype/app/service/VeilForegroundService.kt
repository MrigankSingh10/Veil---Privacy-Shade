package com.veiltype.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.veiltype.app.MainActivity
import com.veiltype.app.R
import com.veiltype.app.VeilTypeApplication
import com.veiltype.app.domain.AppRule
import com.veiltype.app.domain.OverlayInputs
import com.veiltype.app.domain.OverlayReducer
import com.veiltype.app.domain.OverlayMode
import com.veiltype.app.domain.VeilSettings
import com.veiltype.app.overlay.OverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class VeilForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository get() = (application as VeilTypeApplication).settingsRepository
    private lateinit var overlay: OverlayController
    private var settings = VeilSettings()
    private var snapshot = WindowSnapshot()
    private var manualKeyboard = false
    private var manualFullScreen = false
    private var dismissed = false
    private var automaticDismissed = false
    private var shownMode = OverlayMode.HIDDEN
    private var locked = false
    private val tick = android.os.Handler(android.os.Looper.getMainLooper())
    private val reevaluate = object : Runnable { override fun run() { evaluate(); tick.postDelayed(this, 30_000) } }
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            locked = intent?.action == Intent.ACTION_SCREEN_OFF
            if (locked) { manualKeyboard = false; manualFullScreen = false; overlay.removeAll(); controlVisible.value = false }
            else evaluate()
        }
    }

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW))
        startForeground(1, notification())
        running.value = true
        overlay = OverlayController(this, onToggle = {
            if (shownMode != OverlayMode.HIDDEN) { manualKeyboard = false; manualFullScreen = false; automaticDismissed = true }
            else { manualKeyboard = true; automaticDismissed = false }
            evaluate()
        }, onDismiss = { dismissed = true; manualKeyboard = false; manualFullScreen = false; automaticDismissed = true; evaluate() }, onPeek = { overlay.setPeeking(it) }, onMoved = { x, y -> scope.launch { repository.saveBubblePosition(x, y) } })
        registerReceiver(screenReceiver, IntentFilter().apply { addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT) })
        scope.launch { repository.settings.collect { settings = it; if (!it.enabled || !Settings.canDrawOverlays(this@VeilForegroundService)) stopSelf() else evaluate() } }
        scope.launch { VeilAccessibilityService.snapshot.collect { next ->
            if (next.keyboard == null && snapshot.keyboard != null) { dismissed = false; manualKeyboard = false; automaticDismissed = false }
            if (next.foreground != snapshot.foreground) { manualKeyboard = false; automaticDismissed = false; dismissed = false }
            snapshot = next
            evaluate()
        } }
        tick.post(reevaluate)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISARM -> { scope.launch { repository.setEnabled(false); stopSelf() }; return START_NOT_STICKY }
            ACTION_FULL_SCREEN -> { if (shownMode == OverlayMode.FULL_SCREEN) { manualFullScreen = false; automaticDismissed = true } else { dismissed = false; manualFullScreen = true }; evaluate() }
        }
        return START_STICKY
    }
    private fun evaluate() {
        if (!::overlay.isInitialized) return
        if (!Settings.canDrawOverlays(this)) { overlay.removeAll(); controlVisible.value = false; stopSelf(); return }
        val now = LocalDateTime.now()
        val safe = !locked && !getSystemService(KeyguardManager::class.java).isKeyguardLocked && getSystemService(PowerManager::class.java).isInteractive && snapshot.foreground !in BLOCKED
        val rule = settings.appRules[snapshot.foreground] ?: AppRule.DEFAULT
        val decision = OverlayReducer.decide(OverlayInputs(settings.enabled, snapshot.connected, safe, snapshot.keyboard != null, rule, settings.schedule.includes(now.dayOfWeek.value, now.hour * 60 + now.minute), manualKeyboard, manualFullScreen, dismissed, false, automaticDismissed, settings.autoDefault))
        shownMode = decision.mode
        fullScreenActive.value = decision.mode == OverlayMode.FULL_SCREEN
        overlay.render(settings, decision.mode, snapshot.keyboard, decision.showControl)
        controlVisible.value = overlay.isControlVisible
    }
    private fun notification(): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val full = PendingIntent.getService(this, 1, Intent(this, VeilForegroundService::class.java).setAction(ACTION_FULL_SCREEN), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 2, Intent(this, VeilForegroundService::class.java).setAction(ACTION_DISARM), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_veil).setContentTitle(getString(R.string.notification_title)).setContentText(getString(R.string.notification_body)).setContentIntent(open).setOngoing(true)
            .addAction(0, getString(R.string.whole_screen), full).addAction(0, getString(R.string.disarm), stop).build()
    }
    override fun onDestroy() {
        tick.removeCallbacksAndMessages(null)
        if (::overlay.isInitialized) overlay.dispose()
        runCatching { unregisterReceiver(screenReceiver) }
        running.value = false
        controlVisible.value = false
        fullScreenActive.value = false
        scope.cancel()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_ARM = "com.veiltype.app.ARM"
        const val ACTION_DISARM = "com.veiltype.app.DISARM"
        const val ACTION_FULL_SCREEN = "com.veiltype.app.FULL_SCREEN"
        private const val CHANNEL = "veil_service"
        val running = MutableStateFlow(false)
        val controlVisible = MutableStateFlow(false)
        val fullScreenActive = MutableStateFlow(false)
        private val BLOCKED = setOf("android", "com.android.systemui", "com.android.permissioncontroller", "com.google.android.permissioncontroller")
        fun start(context: Context, action: String = ACTION_ARM) {
            ContextCompat.startForegroundService(context, Intent(context, VeilForegroundService::class.java).setAction(action))
        }
    }
}
