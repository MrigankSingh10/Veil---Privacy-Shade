package com.veiltype.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.veiltype.app.ui.MainUiState
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: MainUiState,
    onEnabledChange: (Boolean) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onOpenAccess: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val active = state.settings.enabled && state.serviceConnected
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("VeilType", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Your screen, kept discreet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(if (active) Color(0xFF3DAA70) else Color(0xFF9A909F)))
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(if (active) "Protection ready" else "Protection paused", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            !state.accessibilityEnabled -> "Accessibility access is not enabled"
                            !state.serviceConnected -> "Waiting for Android to connect the service"
                            !state.settings.enabled -> "Turn on the master switch"
                            else -> "Shade will respond to your keyboard"
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = state.settings.enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.semantics { contentDescription = "Master privacy shade switch" },
                )
            }
        }
        if (!state.accessibilityEnabled) {
            Button(onClick = onOpenAccess, modifier = Modifier.fillMaxWidth()) { Text("Set up accessibility access") }
        }
        Text("Shade preview", style = MaterialTheme.typography.titleMedium)
        ShadePreview(state)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Opacity", style = MaterialTheme.typography.titleMedium)
            Text("${(state.settings.opacity * 100).roundToInt()}%")
        }
        Slider(
            value = state.settings.opacity,
            onValueChange = onOpacityChange,
            valueRange = 0.2f..0.95f,
            modifier = Modifier.semantics { contentDescription = "Privacy shade opacity" },
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings and help") }
    }
}

@Composable
private fun ShadePreview(state: MainUiState) {
    val shadeColor = Color(state.settings.shadeColor.argb.toULong()).copy(alpha = state.settings.opacity)
    Box(
        Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.fillMaxWidth(0.65f).height(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(0.82f).height(11.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)))
        }
        Box(Modifier.matchParentSize().background(shadeColor))
        Text("Keyboard remains visible below", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), style = MaterialTheme.typography.labelMedium)
    }
}
