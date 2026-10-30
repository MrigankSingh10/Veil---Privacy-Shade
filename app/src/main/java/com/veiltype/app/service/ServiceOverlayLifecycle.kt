package com.veiltype.app.service

/** Keeps service exit paths deterministic and independently testable. */
class ServiceOverlayLifecycle(private val removeAll: () -> Unit) {
    var connected: Boolean = false
        private set

    fun onConnected() { connected = true }

    fun onInterrupted() { removeAll() }

    fun onDestroyed() {
        removeAll()
        connected = false
    }
}
