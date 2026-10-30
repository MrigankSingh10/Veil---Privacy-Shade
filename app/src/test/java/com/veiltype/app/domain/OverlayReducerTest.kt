package com.veiltype.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayReducerTest {
    private val base = OverlayInputs(armed = true, permissions = true, safe = true, keyboardVisible = true, rule = AppRule.DEFAULT, scheduled = true, manualKeyboard = false, manualFullScreen = false, controlDismissed = false, peeking = false)

    @Test fun appRulesAndManualActions() {
        assertEquals(OverlayDecision(OverlayMode.HIDDEN, true), OverlayReducer.decide(base))
        assertEquals(OverlayMode.KEYBOARD, OverlayReducer.decide(base.copy(rule = AppRule.AUTO_VEIL)).mode)
        assertEquals(OverlayMode.FULL_SCREEN, OverlayReducer.decide(base.copy(rule = AppRule.FULL_SCREEN)).mode)
        assertEquals(OverlayDecision(OverlayMode.HIDDEN, false), OverlayReducer.decide(base.copy(rule = AppRule.SKIP, manualKeyboard = true)))
        assertEquals(OverlayMode.FULL_SCREEN, OverlayReducer.decide(base.copy(rule = AppRule.SKIP, manualFullScreen = true)).mode)
        assertEquals(OverlayMode.KEYBOARD, OverlayReducer.decide(base.copy(scheduled = false, manualKeyboard = true)).mode)
        assertEquals(OverlayMode.HIDDEN, OverlayReducer.decide(base.copy(scheduled = false, rule = AppRule.AUTO_VEIL)).mode)
        assertEquals(OverlayMode.HIDDEN, OverlayReducer.decide(base.copy(rule = AppRule.AUTO_VEIL, automaticDismissed = true)).mode)
    }

    @Test fun protectionStopsWhenNotSafeOrAuthorized() {
        listOf(base.copy(armed = false), base.copy(permissions = false), base.copy(safe = false)).forEach {
            assertEquals(OverlayDecision(OverlayMode.HIDDEN, false), OverlayReducer.decide(it.copy(manualFullScreen = true)))
        }
        assertEquals(OverlayMode.HIDDEN, OverlayReducer.decide(base.copy(manualKeyboard = true, peeking = true)).mode)
        assertEquals(OverlayDecision(OverlayMode.HIDDEN, false), OverlayReducer.decide(base.copy(keyboardVisible = false, manualKeyboard = true)))
    }

    @Test fun migratedAutomaticDefaultRespectsExplicitRules() {
        assertEquals(OverlayMode.KEYBOARD, OverlayReducer.decide(base.copy(autoDefault = true)).mode)
        assertEquals(OverlayMode.HIDDEN, OverlayReducer.decide(base.copy(autoDefault = true, rule = AppRule.SKIP)).mode)
    }
    @Test fun wholeScreenAlwaysOffersAnExitWithoutKeyboard() {
        assertEquals(OverlayDecision(OverlayMode.FULL_SCREEN, true), OverlayReducer.decide(base.copy(keyboardVisible = false, manualFullScreen = true, rule = AppRule.SKIP)))
    }
}
