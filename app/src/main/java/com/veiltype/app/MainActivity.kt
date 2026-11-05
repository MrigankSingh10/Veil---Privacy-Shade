package com.veiltype.app

import android.Manifest
import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.veiltype.app.service.ArmTileService
import com.veiltype.app.service.ScreenTileService
import com.veiltype.app.ui.MainViewModel
import com.veiltype.app.ui.screens.DisclosureScreen
import com.veiltype.app.ui.screens.VeilAppScreen
import com.veiltype.app.ui.theme.VeilTypeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { VeilTypeTheme { VeilApp() } }
    }
}

@Composable
private fun VeilApp(vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var disclosure by remember { mutableStateOf(false) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            vm.setPhoto(uri.toString())
        }
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) vm.refreshPermission() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    fun access() { runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
    if (disclosure) {
        BackHandler { disclosure = false }
        DisclosureScreen(onAccept = { vm.acceptDisclosure(); disclosure = false; access() }, onCancel = { disclosure = false })
    } else {
        VeilAppScreen(state, vm,
            onDisclosure = { disclosure = true },
            onAccessibility = { if (state.settings.disclosureAccepted) access() else disclosure = true },
            onAppInfo = { runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) } },
            onOverlay = { runCatching { context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))) } },
            onNotifications = { if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notifications.launch(Manifest.permission.POST_NOTIFICATIONS) else runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) } },
            onPhoto = { photo.launch(arrayOf("image/*")) },
            onTile = { screen ->
                if (Build.VERSION.SDK_INT >= 33) {
                    val manager = context.getSystemService(StatusBarManager::class.java)
                    val component = ComponentName(context, if (screen) ScreenTileService::class.java else ArmTileService::class.java)
                    manager.requestAddTileService(component, context.getString(if (screen) R.string.tile_screen else R.string.tile_arm), android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_veil), context.mainExecutor) { }
                } else runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            },
        )
    }
}
