package com.veiltype.app.service

/** Resolves known app windows without retrieving their accessibility nodes. */
internal class ForegroundWindowMemory {
    private val packages = linkedMapOf<Int, String>()

    fun remember(windowId: Int, packageName: String) {
        if (windowId < 0) return
        packages[windowId] = packageName
        if (packages.size > 64) packages.remove(packages.keys.first())
    }

    fun resolve(activeApplicationId: Int?, protectedSurfaceActive: Boolean): String? =
        if (protectedSurfaceActive) null else packages[activeApplicationId]

    fun clear() = packages.clear()
}
