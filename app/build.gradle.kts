plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.veiltype.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.veiltype.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.1-beta.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }

    // Secrets are supplied by the developer's environment, never checked in.
    val uploadStore = providers.environmentVariable("VEIL_UPLOAD_STORE_FILE").orNull
    val uploadStorePassword = providers.environmentVariable("VEIL_UPLOAD_STORE_PASSWORD").orNull
    val uploadAlias = providers.environmentVariable("VEIL_UPLOAD_KEY_ALIAS").orNull
    val uploadKeyPassword = providers.environmentVariable("VEIL_UPLOAD_KEY_PASSWORD").orNull
    val uploadValues = listOf(uploadStore, uploadStorePassword, uploadAlias, uploadKeyPassword)
    require(uploadValues.all { it.isNullOrBlank() } || uploadValues.all { !it.isNullOrBlank() }) {
        "Set all four VEIL_UPLOAD_* signing variables, or leave all four unset."
    }

    if (uploadValues.all { !it.isNullOrBlank() }) {
        signingConfigs {
            create("playUpload") {
                storeFile = file(requireNotNull(uploadStore))
                storePassword = uploadStorePassword
                keyAlias = uploadAlias
                keyPassword = uploadKeyPassword
            }
        }
        buildTypes {
            getByName("release") { signingConfig = signingConfigs.getByName("playUpload") }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
