package com.veiltype.app.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.veiltype.app.domain.ControlPosition
import com.veiltype.app.domain.ControlShape
import com.veiltype.app.domain.OverlayMode
import com.veiltype.app.domain.VeilSettings
import com.veiltype.app.R
import kotlin.math.abs
import kotlin.math.roundToInt

class OverlayController(
    private val context: Context,
    private val onToggle: () -> Unit,
    private val onDismiss: () -> Unit,
    private val onPeek: (Boolean) -> Unit,
    private val onMoved: (Int, Int) -> Unit,
) {
    private val wm = context.getSystemService(WindowManager::class.java)
    private val density = context.resources.displayMetrics.density
    private fun dp(n: Int) = (n * density).roundToInt()
    private val cover = FrameLayout(context)
    private val image = ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    private val control = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; orientation = LinearLayout.HORIZONTAL }
    private val toggle = TextView(context)
    private val close = TextView(context)
    private var coverAdded = false
    private var controlAdded = false
    private var lastPhoto = ""
    private var settings = VeilSettings()
    private var mode = OverlayMode.HIDDEN
    private var ime: Rect? = null

    init {
        cover.addView(image, FrameLayout.LayoutParams(-1, -1))
        cover.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { onPeek(true); true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onPeek(false); true }
                else -> true
            }
        }
        toggle.apply { gravity = Gravity.CENTER; setPadding(dp(18), 0, dp(18), 0); setTextColor(Color.BLACK); textSize = 13f; setOnClickListener { onToggle() } }
        close.apply { gravity = Gravity.CENTER; text = "×"; textSize = 22f; setTextColor(Color.WHITE); contentDescription = context.getString(R.string.dismiss_control); setOnClickListener { onDismiss() } }
        control.addView(toggle, LinearLayout.LayoutParams(-2, -1))
        control.addView(close, LinearLayout.LayoutParams(dp(40), -1))
        control.setOnTouchListener(DragListener())
        toggle.setOnTouchListener(DragListener(true))
    }

    fun render(value: VeilSettings, newMode: OverlayMode, keyboard: Rect?, showControl: Boolean) {
        settings = value
        mode = newMode
        ime = keyboard
        updateAppearance()
        if (newMode == OverlayMode.HIDDEN) removeCover() else showCover()
        if (showControl) {
            val display = bounds()
            showControl(keyboard ?: Rect(display.left, display.bottom - dp(80), display.right, display.bottom))
        } else removeControl()
        toggle.text = context.getString(if (newMode != OverlayMode.HIDDEN) R.string.control_off else R.string.control_veil)
    }

    private fun updateAppearance() {
        cover.setBackgroundColor(settings.shadeColor.argb.toInt())
        if (settings.photoUri != lastPhoto) {
            lastPhoto = settings.photoUri
            image.setImageDrawable(null)
            if (lastPhoto.isNotEmpty()) runCatching {
                val uri = Uri.parse(lastPhoto)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                val display = bounds()
                var sample = 1
                while (options.outWidth / sample > display.width() * 2 || options.outHeight / sample > display.height() * 2) sample *= 2
                val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
                if (bitmap != null) image.setImageDrawable(BitmapDrawable(context.resources, bitmap))
            }
        }
        image.visibility = if (image.drawable != null) View.VISIBLE else View.GONE
        control.alpha = settings.controlOpacity
        control.background = GradientDrawable().apply { setColor(0xFF25232A.toInt()); cornerRadius = dp(28).toFloat() }
        toggle.background = GradientDrawable().apply {
            setColor(settings.controlColor)
            cornerRadius = dp(when (settings.controlShape) { ControlShape.CAPSULE -> 24; ControlShape.SOFT -> 8; ControlShape.SHARP -> 0 }).toFloat()
        }
    }

    private fun showCover() {
        val display = bounds()
        val bottom = if (mode == OverlayMode.FULL_SCREEN) display.bottom else (ime?.top ?: return).plus(dp(settings.keyboardOffsetDp)).coerceIn(display.top, display.bottom)
        if (bottom <= display.top) { removeCover(); return }
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            if (settings.touchThrough) WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE else WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        val params = params(display.width(), bottom - display.top, flags, "VeilType cover").apply {
            x = display.left; y = display.top
            val opacity = if (mode == OverlayMode.FULL_SCREEN) settings.screenOpacity else settings.opacity
            alpha = if (settings.touchThrough && Build.VERSION.SDK_INT >= 31) opacity.coerceAtMost(.8f) else opacity
        }
        try {
            if (coverAdded) wm.updateViewLayout(cover, params) else {
                // The control must stay above the cover when the user turns the veil on.
                removeControl()
                wm.addView(cover, params); coverAdded = true
                cover.alpha = if (settings.motionSpeed.durationMs == 0L) 1f else 0f
                cover.animate().alpha(1f).setDuration(settings.motionSpeed.durationMs).start()
            }
        } catch (_: RuntimeException) { removeCover() }
    }

    private fun showControl(keyboard: Rect) {
        val display = bounds()
        val height = dp(46 * settings.controlSize / 100)
        val width = dp(150 * settings.controlSize / 100)
        val params = params(width, height, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, "VeilType control")
        val defaultX = when (settings.controlPosition) { ControlPosition.START -> display.left; ControlPosition.CENTER -> display.centerX() - width / 2; ControlPosition.END -> display.right - width }
        params.x = (if (settings.bubblePositionSaved) settings.bubbleX else defaultX).coerceIn(display.left, (display.right - width).coerceAtLeast(display.left))
        params.y = (if (settings.bubblePositionSaved) settings.bubbleY else keyboard.top + dp(settings.keyboardOffsetDp)).coerceIn(display.top, (display.bottom - height).coerceAtLeast(display.top))
        try {
            if (controlAdded) wm.updateViewLayout(control, params) else { wm.addView(control, params); controlAdded = true }
        } catch (_: RuntimeException) { removeControl() }
    }

    private fun params(width: Int, height: Int, flags: Int, label: String) = WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, flags, PixelFormat.TRANSLUCENT).apply {
        gravity = Gravity.TOP or Gravity.START
        title = label
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
    @Suppress("DEPRECATION") private fun bounds(): Rect = if (Build.VERSION.SDK_INT >= 30) wm.maximumWindowMetrics.bounds else Point().also(wm.defaultDisplay::getRealSize).let { Rect(0, 0, it.x, it.y) }
    private fun removeCover() { cover.animate().cancel(); if (coverAdded) runCatching { wm.removeViewImmediate(cover) }; coverAdded = false; cover.alpha = 1f }
    private fun removeControl() { if (controlAdded) runCatching { wm.removeViewImmediate(control) }; controlAdded = false }
    fun removeAll() { removeCover(); removeControl() }
    fun setPeeking(value: Boolean) { cover.animate().cancel(); cover.alpha = if (value) 0f else 1f }

    private inner class DragListener(private val handlesClick: Boolean = false) : View.OnTouchListener {
        private var startX = 0; private var startY = 0; private var downX = 0f; private var downY = 0f; private var dragging = false; private var peeking = false
        private val longPress = Runnable { if (!dragging && settings.touchThrough && mode != OverlayMode.HIDDEN) { peeking = true; onPeek(true) } }
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            val p = control.layoutParams as? WindowManager.LayoutParams ?: return false
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { startX = p.x; startY = p.y; downX = e.rawX; downY = e.rawY; dragging = false; peeking = false; v.postDelayed(longPress, ViewConfiguration.getLongPressTimeout().toLong()); return true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX; val dy = e.rawY - downY
                    if (abs(dx) > dp(8) || abs(dy) > dp(8)) { dragging = true; v.removeCallbacks(longPress) }
                    if (dragging) { val b = bounds(); p.x = (startX + dx).roundToInt().coerceIn(b.left, (b.right-p.width).coerceAtLeast(b.left)); p.y = (startY + dy).roundToInt().coerceIn(b.top, (b.bottom-p.height).coerceAtLeast(b.top)); runCatching { wm.updateViewLayout(control, p) } }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.removeCallbacks(longPress)
                    if (peeking) onPeek(false) else if (dragging) { onMoved(p.x, p.y) } else if (handlesClick && e.actionMasked == MotionEvent.ACTION_UP) v.performClick()
                    return true
                }
            }
            return false
        }
    }
}
