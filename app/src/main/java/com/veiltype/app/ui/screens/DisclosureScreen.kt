package com.veiltype.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.veiltype.app.R

@Composable
fun DisclosureScreen(onAccept: () -> Unit, onCancel: () -> Unit) {
    var confirmed by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.disclosure_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.disclosure_why), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.disclosure_detail))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.disclosure_privacy_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.disclosure_privacy_detail))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.disclosure_system))
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val consentLabel = stringResource(R.string.disclosure_consent)
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it }, modifier = Modifier.semantics { contentDescription = consentLabel })
            Text(stringResource(R.string.disclosure_consent))
        }
        Spacer(Modifier.height(18.dp))
        Button(enabled = confirmed, onClick = onAccept, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.disclosure_continue)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.not_now)) }
    }
}
