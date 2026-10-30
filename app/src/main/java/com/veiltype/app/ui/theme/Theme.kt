package com.veiltype.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6546A8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF24005B),
    secondary = Color(0xFF625B70),
    background = Color(0xFFFFF8FF),
    surface = Color(0xFFFFF8FF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD1BCFF),
    onPrimary = Color(0xFF352061),
    primaryContainer = Color(0xFF4D367F),
    onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFFCBC2DB),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
)

@Composable
fun VeilTypeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
