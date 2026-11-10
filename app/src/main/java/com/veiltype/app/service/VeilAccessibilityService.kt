package com.veiltype.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.veiltype.app.platform.AccessibilityKeyboardDetector
import com.veiltype.app.platform.ForegroundEventFilter
import com.veiltype.app.platform.WindowKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class WindowSnapshot(val keyboard: Rect? = null, val foreground: String? = null, val connected: Boolean = false)

class VeilAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val detector = AccessibilityKeyboardDetector()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var foreground: String? = null
    private val pendingForeground = ForegroundEventQueue()
    private val foregroundWindows = ForegroundWindowMemory()
    private val refreshScheduler = WindowRefreshScheduler(
        post = { task, delay -> handler.postDelayed(task, delay) },
        remove = { handler.removeCallbacks(it) },
        refresh = { refreshWindows() },
    )

    private fun refreshWindows() {
        val current = runCatching { windows.orEmpty() }.getOrDefault(emptyList())
        val imePackage = if (pendingForeground.isNotEmpty) Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/') else null
        pendingForeground.drain().forEach { (pkg, windowId) ->
            val window = current.firstOrNull { it.id == windowId }
            val kind = when (window?.type) {
                AccessibilityWindowInfo.TYPE_APPLICATION -> WindowKind.APPLICATION
                AccessibilityWindowInfo.TYPE_INPUT_METHOD -> WindowKind.KEYBOARD
                null -> WindowKind.UNKNOWN
                else -> WindowKind.OTHER
            }
            val protectedForeground = pkg in protectedPackages &&
                (window == null || window.isActive || window.isFocused || pkg in permissionPackages)
            if (ForegroundEventFilter.shouldTrack(pkg, packageName, imePackage, kind, protectedForeground)) {
                foreground = pkg
                if (kind == WindowKind.APPLICATION) foregroundWindows.remember(windowId, pkg)
            }
        }
        val activeApps = current.filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && (it.isActive || it.isFocused) }
        val protectedApp = activeApps.firstOrNull { foregroundWindows.resolve(it.id, false) in protectedPackages }
        val activeApp = protectedApp ?: activeApps.firstOrNull { it.isFocused } ?: activeApps.firstOrNull()
        val protectedSurfaceActive = current.any {
            it.type != AccessibilityWindowInfo.TYPE_APPLICATION && it.type != AccessibilityWindowInfo.TYPE_INPUT_METHOD &&
                (it.isActive || it.isFocused) && it.type != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
        }
        foregroundWindows.resolve(activeApp?.id, protectedSurfaceActive)?.let { foreground = it }
        snapshot.value = WindowSnapshot(detector.detect(current).bounds, foreground, true)
        connected.value = true
    }
    private val periodic = object : Runnable {
        override fun run() {
            if (VeilForegroundService.running.value) {
                refreshScheduler.request()
                handler.postDelayed(this, 2_000)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        connected.value = true
        refreshScheduler.immediate()
        scope.launch {
            VeilForegroundService.running.collect { running ->
                handler.removeCallbacks(periodic)
                if (running) { handler.post(periodic); refreshScheduler.transition() }
                else refreshScheduler.cancel()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName != null) {
            val pkg = event.packageName.toString()
            pendingForeground.add(pkg, event.windowId)
            // Protected system surfaces must not wait behind an app-transition burst.
            if (pkg in protectedPackages) {
                refreshScheduler.immediate()
                if (VeilForegroundService.running.value) refreshScheduler.transition()
                return
            }
        }
        if (VeilForegroundService.running.value) refreshScheduler.transition() else refreshScheduler.request()
    }

    override fun onInterrupt() { refreshScheduler.cancel(); handler.removeCallbacks(periodic); pendingForeground.clear(); foregroundWindows.clear(); foreground = null; snapshot.value = WindowSnapshot(); connected.value = false }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        refreshScheduler.cancel()
        foregroundWindows.clear()
        scope.cancel()
        snapshot.value = WindowSnapshot()
        connected.value = false
        super.onDestroy()
    }

    companion object {
        val connected = MutableStateFlow(false)
        val snapshot = MutableStateFlow(WindowSnapshot())
        private val protectedPackages = setOf("android", "com.android.systemui", "com.android.permissioncontroller", "com.google.android.permissioncontroller")
        private val permissionPackages = setOf("com.android.permissioncontroller", "com.google.android.permissioncontroller")
    }
}
