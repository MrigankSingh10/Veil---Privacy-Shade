package com.veiltype.app.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    MaterialTheme(colorScheme = DarkColors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
