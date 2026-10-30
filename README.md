# VeilType

VeilType is an offline Android privacy veil. When armed, it places a control near the keyboard so you can cover the screen above it, briefly peek underneath, or use per-app rules to cover content automatically. A separate action covers the whole screen. The veil is a visual aid, not protection against screenshots, cameras, or privileged software.

## Setup

1. Build with Android SDK 36 and JDK 17: `./gradlew testDebugUnitTest assembleDebug`.
2. Install `app/build/outputs/apk/debug/app-debug.apk`, complete the walkthrough and review the accessibility disclosure.
3. Allow display over other apps and enable VeilType keyboard awareness in Android accessibility settings. Notification access is recommended for the foreground-service controls.
4. Arm the veil in Setup. Open a keyboard, tap the control to cover the screen, and hold the cover to peek. With touch-through enabled, hold the control instead.
5. Use Tuning for app rules, schedules, styles, photos, the keyboard offset, boot restoration, and Quick Settings tiles.

The app has no internet permission. Accessibility observes top-level keyboard windows and foreground package names; it does not traverse accessibility nodes or read typed or displayed content. Settings and app rules are stored locally. An optional photo is accessed through a user-selected document URI.

The Android overlay and accessibility mechanisms vary by device and keyboard. See [limitations](docs/LIMITATIONS.md) and the [device test matrix](docs/TEST_MATRIX.md). Store distribution requires accessibility and foreground-service policy review.

For local emulator commands, see [local AVD testing](docs/LOCAL_AVD_TESTING.md).
