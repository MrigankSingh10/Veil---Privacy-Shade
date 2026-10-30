# Test VeilType on a local Android emulator

Run these commands from the project directory. The examples use the default Android SDK location on macOS; adjust it if your SDK is elsewhere.

## Start an emulator

List available virtual devices:

```bash
"$HOME/Library/Android/sdk/emulator/emulator" -list-avds
```

Start an AVD, replacing the name with one from the list:

```bash
"$HOME/Library/Android/sdk/emulator/emulator" -avd YOUR_AVD_NAME
```

Leave that terminal running and wait until Android has finished booting. In a second terminal, go to the project and check the device:

```bash
cd /path/to/veiltype
adb devices -l
```

If more than one device is listed, select the emulator's serial for the `adb` commands, replacing `emulator-5554` with the serial shown by `adb devices -l`:

```bash
export ANDROID_SERIAL=emulator-5554
```

## Install and launch

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.veiltype.app/.MainActivity
```

Complete the walkthrough, allow display over other apps, enable VeilType under Android Accessibility settings, and arm the veil. Tap the test field to show the keyboard, tap **Veil**, hold the cover to peek, and tap **Off**. Try the Tuning screen and per-app rules as needed.

## Run automated tests

```bash
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

Gradle's connected test task can run on every connected device, so disconnect other devices first if you want to test only this emulator. The connected tests install and uninstall their test packages; if the app is no longer available afterward, run the install and launch commands again. Emulator testing does not replace the physical-device checks in [TEST_MATRIX.md](TEST_MATRIX.md).
