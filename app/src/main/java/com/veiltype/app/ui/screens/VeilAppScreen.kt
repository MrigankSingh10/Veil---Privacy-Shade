package com.veiltype.app.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.veiltype.app.R
import com.veiltype.app.domain.*
import com.veiltype.app.ui.MainUiState
import com.veiltype.app.ui.MainViewModel
import java.util.Locale
import kotlin.math.roundToInt

private enum class Destination { SETUP, TUNING, APPS, SCHEDULE, PRIVACY }

@Composable
fun VeilAppScreen(state: MainUiState, vm: MainViewModel, onDisclosure: () -> Unit, onAccessibility: () -> Unit, onAppInfo: () -> Unit, onOverlay: () -> Unit, onNotifications: () -> Unit, onPhoto: () -> Unit, onTile: (Boolean) -> Unit) {
    var page by remember { mutableStateOf(Destination.SETUP) }
    var step by remember { mutableIntStateOf(0) }
    BackHandler(enabled = page != Destination.SETUP) { page = if (page == Destination.TUNING || page == Destination.PRIVACY) Destination.SETUP else Destination.TUNING }
    if (!state.settings.onboardingComplete && page == Destination.PRIVACY) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PrivacyPolicy { page = Destination.SETUP }
        }
        return
    }
    if (!state.settings.onboardingComplete) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().padding(28.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.brand), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(44.dp))
            Text(stringResource(listOf(R.string.walk_title_1, R.string.walk_title_2, R.string.walk_title_3)[step]), style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(listOf(R.string.walk_body_1, R.string.walk_body_2, R.string.walk_body_3)[step]), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(40.dp))
            Button(onClick = { if (step < 2) step++ else onDisclosure() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(if (step < 2) R.string.next else R.string.get_started)) }
            OutlinedButton(onClick = { page = Destination.PRIVACY }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.privacy_policy)) }
        }
        return
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.brand), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Row { Text(stringResource(R.string.setup), Modifier.clickable { page = Destination.SETUP }.padding(10.dp), color = if (page == Destination.SETUP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Text(stringResource(R.string.tuning), Modifier.clickable { page = Destination.TUNING }.padding(10.dp), color = if (page in setOf(Destination.TUNING, Destination.APPS, Destination.SCHEDULE)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (page) {
                Destination.SETUP -> Setup(state, vm, onAccessibility, onAppInfo, onOverlay, onNotifications) { page = Destination.PRIVACY }
                Destination.TUNING -> Tuning(state, vm, onPhoto, onTile, { page = Destination.APPS }, { page = Destination.SCHEDULE })
                Destination.APPS -> Apps(state, vm) { page = Destination.TUNING }
                Destination.SCHEDULE -> ScheduleEditor(state.settings.schedule, vm::setSchedule) { page = Destination.TUNING }
                Destination.PRIVACY -> PrivacyPolicy { page = Destination.SETUP }
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable private fun Heading(title: String, body: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Medium)
    if (body != null) Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable private fun Section(title: String) { Text(title.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
@Composable private fun CardRow(title: String, detail: String, action: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.width(8.dp)); action()
        }
    }
}

@Composable private fun Setup(state: MainUiState, vm: MainViewModel, access: () -> Unit, appInfo: () -> Unit, overlay: () -> Unit, notifications: () -> Unit, privacy: () -> Unit) {
    Heading(stringResource(R.string.setup_title), stringResource(R.string.setup_body))
    Section(stringResource(R.string.permissions))
    CardRow(stringResource(R.string.overlay_title), stringResource(R.string.overlay_detail)) { OutlinedButton(onClick = overlay) { Text(stringResource(if (state.overlayAllowed) R.string.ready else R.string.allow)) } }
    CardRow(stringResource(R.string.keyboard_title), stringResource(R.string.keyboard_detail)) { OutlinedButton(onClick = access) { Text(stringResource(if (state.accessibilityEnabled) R.string.ready else R.string.allow)) } }
    if (!state.accessibilityEnabled && Build.VERSION.SDK_INT >= 33) {
        Text(stringResource(R.string.restricted_settings_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = appInfo, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.open_app_info)) }
    }
    if (state.accessibilityEnabled && !state.serviceConnected) {
        Text(stringResource(R.string.accessibility_waiting), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
    CardRow(stringResource(R.string.notifications_title), stringResource(R.string.notifications_detail)) { OutlinedButton(onClick = notifications) { Text(stringResource(R.string.open)) } }
    Section(stringResource(R.string.protection))
    CardRow(stringResource(if (state.serviceRunning) R.string.armed else R.string.arm), stringResource(if (state.serviceRunning && !state.serviceConnected) R.string.accessibility_waiting else if (state.serviceRunning) R.string.armed_detail else R.string.arm_detail)) { val label = stringResource(R.string.arm); Switch(checked = state.settings.enabled && state.serviceRunning, onCheckedChange = vm::setEnabled, enabled = state.serviceRunning || (state.overlayAllowed && state.accessibilityEnabled && state.serviceConnected), modifier = Modifier.semantics { contentDescription = label }) }
    if (state.settings.enabled && !state.serviceRunning) Text(stringResource(R.string.restart_hint), style = MaterialTheme.typography.bodySmall)
    if (state.serviceRunning && state.serviceConnected) Text(stringResource(when { state.keyboardVisible && state.controlVisible -> R.string.keyboard_detected; state.keyboardVisible -> R.string.keyboard_detected_control_hidden; else -> R.string.keyboard_not_detected }), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Section(stringResource(R.string.try_title))
    Text(stringResource(R.string.try_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
    var text by remember { mutableStateOf("") }
    OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.try_placeholder)) })
    Section(stringResource(R.string.privacy))
    Text(stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyMedium)
    OutlinedButton(onClick = privacy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.privacy_policy)) }
    OutlinedButton(onClick = vm::replayOnboarding, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.replay)) }
    Text(stringResource(R.string.support), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun PrivacyPolicy(back: () -> Unit) {
    Text(stringResource(R.string.back), Modifier.clickable(onClick = back).padding(vertical = 8.dp))
    Heading(stringResource(R.string.privacy_policy), stringResource(R.string.privacy_updated))
    Section(stringResource(R.string.privacy_information_title))
    Text(stringResource(R.string.privacy_information_body))
    Section(stringResource(R.string.privacy_permissions_title))
    Text(stringResource(R.string.privacy_permissions_body))
    Section(stringResource(R.string.privacy_retention_title))
    Text(stringResource(R.string.privacy_retention_body))
    Section(stringResource(R.string.privacy_contact_title))
    Text(stringResource(R.string.privacy_contact_body))
}

@Composable private fun Tuning(state: MainUiState, vm: MainViewModel, photo: () -> Unit, tile: (Boolean) -> Unit, apps: () -> Unit, schedule: () -> Unit) {
    val s = state.settings
    Heading(stringResource(R.string.tuning_title), stringResource(R.string.tuning_body))
    Section(stringResource(R.string.veil_appearance))
    Text(stringResource(R.string.veil_style), style = MaterialTheme.typography.titleMedium)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { ShadeColor.entries.forEach { c -> FilterChip(selected = c == s.shadeColor, onClick = { vm.setColor(c) }, label = { Text(stringResource(when(c) { ShadeColor.BLACK -> R.string.color_black; ShadeColor.DARK_GRAY -> R.string.color_gray; ShadeColor.DEEP_PURPLE -> R.string.color_purple })) }) } }
    Text(stringResource(R.string.opacity_value, (s.opacity * 100).roundToInt()))
    Slider(s.opacity, vm::setOpacity, valueRange = .2f.. .95f)
    Text(stringResource(R.string.screen_opacity_value, (s.screenOpacity * 100).roundToInt()))
    Slider(s.screenOpacity, vm::setScreenOpacity, valueRange = .2f.. .95f)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = photo) { Text(stringResource(R.string.choose_photo)) }; if (s.photoUri.isNotEmpty()) OutlinedButton(onClick = { vm.setPhoto("") }) { Text(stringResource(R.string.clear_photo)) } }
    CardRow(stringResource(R.string.touch_title), stringResource(R.string.touch_detail)) { val label = stringResource(R.string.touch_title); Switch(s.touchThrough, vm::setTouchThrough, modifier = Modifier.semantics { contentDescription = label }) }
    Text(stringResource(R.string.offset_value, s.keyboardOffsetDp)); Slider(s.keyboardOffsetDp.toFloat(), { vm.setOffset(it.roundToInt()) }, valueRange = 0f..160f)
    Section(stringResource(R.string.control_title))
    Text(stringResource(R.string.position)); Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { ControlPosition.entries.forEach { p -> FilterChip(s.controlPosition == p, { vm.setPosition(p) }, label = { Text(stringResource(when(p) { ControlPosition.START -> R.string.start; ControlPosition.CENTER -> R.string.center; ControlPosition.END -> R.string.end })) }) } }
    Text(stringResource(R.string.shape)); Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { ControlShape.entries.forEach { p -> FilterChip(s.controlShape == p, { vm.setShape(p) }, label = { Text(stringResource(when(p) { ControlShape.CAPSULE -> R.string.capsule; ControlShape.SOFT -> R.string.soft; ControlShape.SHARP -> R.string.sharp })) }) } }
    Text(stringResource(R.string.control_size, s.controlSize)); Slider(s.controlSize.toFloat(), { vm.setControlSize(it.roundToInt()) }, valueRange = 75f..150f)
    Text(stringResource(R.string.control_opacity, (s.controlOpacity * 100).roundToInt())); Slider(s.controlOpacity, vm::setControlOpacity, valueRange = .3f..1f)
    Text(stringResource(R.string.control_color)); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(0xFFB7A4E8.toInt(), 0xFFA8CFBA.toInt(), 0xFFE4C38D.toInt(), 0xFFE7B2B7.toInt()).forEachIndexed { index, color -> FilterChip(s.controlColor == color, { vm.setControlColor(color) }, label = { Text(stringResource(listOf(R.string.violet, R.string.sage, R.string.amber, R.string.rose)[index])) }) } }
    Text(stringResource(R.string.motion)); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) { MotionSpeed.entries.forEach { m -> FilterChip(s.motionSpeed == m, { vm.setMotion(m) }, label = { Text(stringResource(when(m) { MotionSpeed.INSTANT -> R.string.instant; MotionSpeed.FAST -> R.string.fast; MotionSpeed.NORMAL -> R.string.normal; MotionSpeed.SLOW -> R.string.slow })) }) } }
    Section(stringResource(R.string.automation))
    CardRow(stringResource(R.string.auto_default), stringResource(R.string.auto_default_detail)) { val label = stringResource(R.string.auto_default); Switch(s.autoDefault, vm::setAutoDefault, modifier = Modifier.semantics { contentDescription = label }) }
    OutlinedButton(onClick = apps, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.app_rules)) }
    OutlinedButton(onClick = schedule, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.schedule)) }
    CardRow(stringResource(R.string.start_boot), stringResource(R.string.start_boot_detail)) { val label = stringResource(R.string.start_boot); Switch(s.startOnBoot, vm::setBoot, modifier = Modifier.semantics { contentDescription = label }) }
    Section(stringResource(R.string.quick_settings))
    OutlinedButton(onClick = { tile(false) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.add_arm_tile)) }
    OutlinedButton(onClick = { tile(true) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.add_screen_tile)) }
}

@Composable private fun Apps(state: MainUiState, vm: MainViewModel, back: () -> Unit) {
    val context = LocalContext.current
    val apps = remember { context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0).map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }.distinctBy { it.first }.sortedBy { it.second.lowercase() } }
    var query by remember { mutableStateOf("") }
    Text(stringResource(R.string.back), Modifier.clickable(onClick = back).padding(vertical = 8.dp))
    Heading(stringResource(R.string.app_rules), stringResource(R.string.app_rules_detail))
    OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_apps)) }, singleLine = true)
    apps.filter { it.second.contains(query, true) }.forEach { (pkg, name) ->
        var expanded by remember { mutableStateOf(false) }
        val rule = state.settings.appRules[pkg] ?: AppRule.DEFAULT
        CardRow(name, pkg) { Box { OutlinedButton(onClick = { expanded = true }) { Text(ruleLabel(rule)) }; DropdownMenu(expanded, { expanded = false }) { AppRule.entries.forEach { r -> DropdownMenuItem(text = { Text(ruleLabel(r)) }, onClick = { vm.setRule(pkg, r); expanded = false }) } } } }
    }
}
@Composable private fun ruleLabel(rule: AppRule) = stringResource(when(rule) { AppRule.DEFAULT -> R.string.rule_default; AppRule.AUTO_VEIL -> R.string.rule_auto; AppRule.FULL_SCREEN -> R.string.rule_full; AppRule.SKIP -> R.string.rule_skip })

@Composable private fun ScheduleEditor(s: Schedule, update: (Schedule) -> Unit, back: () -> Unit) {
    val context = LocalContext.current
    Text(stringResource(R.string.back), Modifier.clickable(onClick = back).padding(vertical = 8.dp))
    Heading(stringResource(R.string.schedule), stringResource(R.string.schedule_detail))
    CardRow(stringResource(R.string.schedule_enabled), stringResource(R.string.schedule_overnight)) { val label = stringResource(R.string.schedule_enabled); Switch(s.enabled, { update(s.copy(enabled = it)) }, modifier = Modifier.semantics { contentDescription = label }) }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(R.string.mon, R.string.tue, R.string.wed, R.string.thu, R.string.fri, R.string.sat, R.string.sun).forEachIndexed { index, label ->
            val day = index + 1
            FilterChip(day in s.days, { update(s.copy(days = if (day in s.days) s.days - day else s.days + day)) }, label = { Text(stringResource(label)) })
        }
    }
    fun choose(value: Int, onValue: (Int) -> Unit) { TimePickerDialog(context, { _, hour, minute -> onValue(hour * 60 + minute) }, value / 60, value % 60, true).show() }
    OutlinedButton(onClick = { choose(s.startMinute) { update(s.copy(startMinute = it)) } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.start_time, s.startMinute / 60, s.startMinute % 60)) }
    OutlinedButton(onClick = { choose(s.endMinute) { update(s.copy(endMinute = it)) } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.end_time, s.endMinute / 60, s.endMinute % 60)) }
    Text(stringResource(R.string.all_day_hint), style = MaterialTheme.typography.bodySmall)
}
