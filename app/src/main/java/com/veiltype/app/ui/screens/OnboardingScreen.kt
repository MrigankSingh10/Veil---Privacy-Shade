package com.veiltype.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("VeilType", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp))
        Text("Privacy while you type", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("VeilType places a dark, adjustable shade over the app above your keyboard. Your keyboard stays visible and usable.")
        Spacer(Modifier.height(24.dp))
        Text("• Works entirely on your device\n• Never reads or stores what you type\n• Has no internet permission\n• Can be disabled at any time", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(30.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Review required access") }
    }
}
