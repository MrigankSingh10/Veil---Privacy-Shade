package com.veiltype.app.service

/** Keeps distinct consecutive foreground candidates until the next window scan. */
internal class ForegroundEventQueue {
    private val events = mutableListOf<Pair<String, Int>>()
    val isNotEmpty: Boolean get() = events.isNotEmpty()

    fun add(packageName: String, windowId: Int) {
        val event = packageName to windowId
        if (events.lastOrNull() != event) events.add(event)
    }

    fun drain(): List<Pair<String, Int>> = events.toList().also { events.clear() }
    fun clear() = events.clear()
}
