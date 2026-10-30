package com.veiltype.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.veiltype.app.domain.ActivationMode
import com.veiltype.app.domain.ShadeColor
import com.veiltype.app.ui.MainUiState
import com.veiltype.app.ui.components.SettingsSwitch

@Composable
fun SettingsScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onModeChange: (ActivationMode) -> Unit,
    onFloatingChange: (Boolean) -> Unit,
    onColorChange: (ShadeColor) -> Unit,
    onPermission: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("‹  Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp))
        Text("Activation", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActivationMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.settings.activationMode == mode,
                    onClick = { onModeChange(mode) },
                    label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) },
                )
            }
        }
        Text(
            if (state.settings.activationMode == ActivationMode.AUTOMATIC) "Shows only while an on-screen keyboard is detected."
            else "Use the floating V control to show or hide the shade.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsSwitch(
            title = "Floating control",
            subtitle = "A draggable control shown over other apps",
            checked = state.settings.floatingControl,
            onCheckedChange = onFloatingChange,
        )
        Text("Shade color", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadeColor.entries.forEach { color ->
                FilterChip(
                    selected = state.settings.shadeColor == color,
                    onClick = { onColorChange(color) },
                    label = { Text(color.label) },
                )
            }
        }
        Text("Permission", style = MaterialTheme.typography.titleLarge)
        Text(if (state.accessibilityEnabled) "Accessibility access enabled" else "Accessibility access disabled")
        OutlinedButton(onClick = onPermission, modifier = Modifier.fillMaxWidth()) { Text("Open accessibility settings") }
        Text("Help & troubleshooting", style = MaterialTheme.typography.titleLarge)
        Text("If automatic mode does not respond, confirm the VeilType service is enabled, then toggle it off and on. Some device makers stop accessibility services for battery saving; allow VeilType to run in the background. Floating and split keyboards vary by device.")
        Text("Privacy", style = MaterialTheme.typography.titleLarge)
        Text("VeilType works offline. It does not read screen text, typed text, passwords, clipboard data, or screenshots. No analytics, advertising, or tracking is included.")
        Text("About VeilType", style = MaterialTheme.typography.titleLarge)
        Text("Version 1.0.0\nA visual privacy aid—not a substitute for device security.")
    }
}
