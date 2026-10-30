package com.veiltype.app.overlay

data class IntBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
}

object OverlayGeometry {
    fun shadeBounds(display: IntBounds, ime: IntBounds?): IntBounds? {
        val bottom = ime?.top?.coerceIn(display.top, display.bottom) ?: return null
        if (bottom <= display.top) return null
        return IntBounds(display.left, display.top, display.right, bottom)
    }
}
