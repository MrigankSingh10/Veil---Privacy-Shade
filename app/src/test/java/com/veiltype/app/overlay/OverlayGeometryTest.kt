package com.veiltype.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverlayGeometryTest {
    private val display = IntBounds(0, 0, 1080, 2400)

    @Test fun shadeStopsAtKeyboardTop() {
        assertEquals(IntBounds(0, 0, 1080, 1600), OverlayGeometry.shadeBounds(display, IntBounds(0, 1600, 1080, 2400)))
    }

    @Test fun missingKeyboardProducesNoAutomaticShade() {
        assertNull(OverlayGeometry.shadeBounds(display, null))
    }

    @Test fun keyboardBoundsAreClampedToDisplay() {
        assertEquals(display, OverlayGeometry.shadeBounds(display, IntBounds(0, 2600, 1080, 2800)))
        assertNull(OverlayGeometry.shadeBounds(display, IntBounds(0, -100, 1080, 500)))
    }

    @Test fun supportsSplitScreenDisplayOrigin() {
        val split = IntBounds(0, 1200, 1080, 2400)
        assertEquals(IntBounds(0, 1200, 1080, 1800), OverlayGeometry.shadeBounds(split, IntBounds(0, 1800, 1080, 2400)))
    }
}
