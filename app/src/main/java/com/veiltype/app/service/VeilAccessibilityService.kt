package com.veiltype.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.veiltype.app.platform.AccessibilityKeyboardDetector
import kotlinx.coroutines.flow.MutableStateFlow

data class WindowSnapshot(val keyboard: Rect? = null, val foreground: String? = null, val connected: Boolean = false)

class VeilAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val detector = AccessibilityKeyboardDetector()
    private var foreground: String? = null
    private val refresh = Runnable {
        val current = runCatching { windows.orEmpty() }.getOrDefault(emptyList())
        snapshot.value = WindowSnapshot(detector.detect(current).bounds, foreground, true)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        connected.value = true
        handler.post(refresh)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName != null) {
            val window = runCatching { windows.orEmpty().firstOrNull { it.id == event.windowId } }.getOrNull()
            val pkg = event.packageName.toString()
            val imePackage = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/')
            if (window?.type == AccessibilityWindowInfo.TYPE_APPLICATION || pkg in protectedPackages || (window == null && pkg != imePackage && pkg != packageName)) foreground = pkg
        }
        handler.removeCallbacks(refresh)
        handler.postDelayed(refresh, 50)
    }

    override fun onInterrupt() { snapshot.value = WindowSnapshot(); connected.value = false }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        snapshot.value = WindowSnapshot()
        connected.value = false
        super.onDestroy()
    }

    companion object {
        val connected = MutableStateFlow(false)
        val snapshot = MutableStateFlow(WindowSnapshot())
        private val protectedPackages = setOf("android", "com.android.systemui", "com.android.permissioncontroller", "com.google.android.permissioncontroller")
    }
}
