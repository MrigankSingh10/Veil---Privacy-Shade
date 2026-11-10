package com.veiltype.app.service

/** Coalesces event bursts and checks again while keyboard windows settle. */
internal class WindowRefreshScheduler(
    private val post: (Runnable, Long) -> Unit,
    private val remove: (Runnable) -> Unit,
    private val refresh: () -> Unit,
) {
    private var pending = false
    private var probing = false
    private val first = Runnable { pending = false; refresh() }
    private val probes = listOf(150L, 350L, 700L).map { delay ->
        delay to Runnable {
            if (delay == 700L) probing = false
            refresh()
        }
    }

    fun request() {
        if (!pending) { pending = true; post(first, 50L) }
    }

    fun transition() {
        request()
        // Events from an animating window must not keep postponing recovery.
        if (!probing) {
            probing = true
            probes.forEach { (delay, task) -> post(task, delay) }
        }
    }

    fun immediate() {
        remove(first)
        pending = false
        refresh()
    }

    fun cancel() {
        remove(first)
        probes.forEach { (_, task) -> remove(task) }
        pending = false
        probing = false
    }
}
