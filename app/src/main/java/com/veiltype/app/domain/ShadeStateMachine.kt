package com.veiltype.app.domain

data class ShadeInputs(
    val enabled: Boolean,
    val serviceConnected: Boolean,
    val mode: ActivationMode,
    val keyboardVisible: Boolean,
    val manualShadeEnabled: Boolean,
    val safeWindow: Boolean,
)

object ShadeStateMachine {
    fun shouldShowShade(input: ShadeInputs): Boolean {
        if (!input.enabled || !input.serviceConnected || !input.safeWindow) return false
        return when (input.mode) {
            ActivationMode.AUTOMATIC -> input.keyboardVisible
            ActivationMode.MANUAL -> input.manualShadeEnabled
        }
    }
}
