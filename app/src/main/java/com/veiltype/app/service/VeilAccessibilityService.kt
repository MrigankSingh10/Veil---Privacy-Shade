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
    private val refresh = Runnable {
        val current = runCatching { windows.orEmpty() }.getOrDefault(emptyList())
        snapshot.value = WindowSnapshot(detector.detect(current).bounds, foreground, true)
        connected.value = true
    }
    private val periodic = object : Runnable {
        override fun run() {
            if (VeilForegroundService.running.value) {
                scheduleRefresh()
                handler.postDelayed(this, 2_000)
            }
        }
    }

    private fun scheduleRefresh() {
        // Coalesce bursts without continually postponing detection while windows animate.
        if (!handler.hasCallbacks(refresh)) handler.postDelayed(refresh, 50)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        connected.value = true
        handler.post(refresh)
        scope.launch {
            VeilForegroundService.running.collect { running ->
                handler.removeCallbacks(periodic)
                if (running) handler.post(periodic)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName != null) {
            val window = runCatching { windows.orEmpty().firstOrNull { it.id == event.windowId } }.getOrNull()
            val pkg = event.packageName.toString()
            val imePackage = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/')
            val kind = when (window?.type) {
                AccessibilityWindowInfo.TYPE_APPLICATION -> WindowKind.APPLICATION
                AccessibilityWindowInfo.TYPE_INPUT_METHOD -> WindowKind.KEYBOARD
                null -> WindowKind.UNKNOWN
                else -> WindowKind.OTHER
            }
            // System UI emits window-state events during IME transitions too. Only treat
            // its window as the foreground surface when it is active/focused; otherwise
            // a navigation-bar event can keep the control hidden after the keyboard opens.
            val protectedForeground = pkg in protectedPackages &&
                (window == null || window.isActive || window.isFocused || pkg in permissionPackages)
            if (ForegroundEventFilter.shouldTrack(pkg, packageName, imePackage, kind, protectedForeground)) foreground = pkg
        }
        scheduleRefresh()
    }

    override fun onInterrupt() { handler.removeCallbacks(refresh); snapshot.value = WindowSnapshot(); connected.value = false }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
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
