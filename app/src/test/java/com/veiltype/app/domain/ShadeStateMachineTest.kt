package com.veiltype.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadeStateMachineTest {
    private fun input(
        enabled: Boolean = true,
        connected: Boolean = true,
        mode: ActivationMode = ActivationMode.AUTOMATIC,
        keyboard: Boolean = true,
        manual: Boolean = false,
        safe: Boolean = true,
    ) = ShadeInputs(enabled, connected, mode, keyboard, manual, safe)

    @Test fun automaticFollowsKeyboard() {
        assertTrue(ShadeStateMachine.shouldShowShade(input(keyboard = true)))
        assertFalse(ShadeStateMachine.shouldShowShade(input(keyboard = false)))
    }

    @Test fun disabledDisconnectedAndUnsafeAlwaysHide() {
        assertFalse(ShadeStateMachine.shouldShowShade(input(enabled = false)))
        assertFalse(ShadeStateMachine.shouldShowShade(input(connected = false)))
        assertFalse(ShadeStateMachine.shouldShowShade(input(safe = false)))
    }

    @Test fun manualModeUsesManualToggle() {
        assertFalse(ShadeStateMachine.shouldShowShade(input(mode = ActivationMode.MANUAL, keyboard = true, manual = false)))
        assertTrue(ShadeStateMachine.shouldShowShade(input(mode = ActivationMode.MANUAL, keyboard = false, manual = true)))
    }

    @Test fun rapidKeyboardSequenceHasNoLatchedState() {
        val results = listOf(false, true, false, true, true, false).map { visible ->
            ShadeStateMachine.shouldShowShade(input(keyboard = visible))
        }
        assertTrue(results == listOf(false, true, false, true, true, false))
    }
}
