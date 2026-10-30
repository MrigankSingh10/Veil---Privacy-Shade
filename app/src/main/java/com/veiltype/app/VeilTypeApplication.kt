package com.veiltype.app

import android.app.Application
import com.veiltype.app.data.SettingsRepository

class VeilTypeApplication : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(applicationContext) }
}
