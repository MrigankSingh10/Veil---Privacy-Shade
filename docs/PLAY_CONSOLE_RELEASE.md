# Play Console preparation

This guide prepares VeilType for an **internal test**. It does not assert policy approval or authorize publishing. The repository currently uses application ID `com.veiltype.app`, version code `2`, version name `1.0.1-beta.1`, minimum Android 10 (API 29), and target API 36. Before creating the Play app, confirm that this application ID is available and is the permanent ID you intend to use. Play requires a greater version code for every subsequently uploaded version.

## 1. Confirm ownership and signing

- Decide whether this is a new Play app. If an app already exists in Play, use its registered upload key and existing application ID; do not create a replacement key without following Play's upload-key reset process.
- This new app uses a separate VeilType **upload keystore**. Keep the keystore and passwords in a secure, backed-up location outside this repository; don't send passwords in chat. Verify the upload-certificate fingerprint against VeilType's Play Console once registered. Enroll VeilType in Play App Signing. The Android debug key is not a release upload key.
- Android developer verification may ask for proof of ownership of the existing package or signing key. This package has already been distributed outside Play as a debug-signed APK, so check the actual Play Console requirements before registering or changing the package ID. Do not delete or publish any existing key.
- A Play-distributed build normally cannot update the GitHub debug APK because it will be signed with a different app-signing key. Testers may need to uninstall the debug build first, losing its local preferences.

## 2. Build and verify the upload bundle

Provide the **absolute path** to your upload keystore and its credentials in your own terminal session. Do not paste credentials into source files, chat, screenshots or issue reports. The following prompts work in Bash and Zsh:

```bash
printf 'Upload keystore path: '; read -r VEIL_UPLOAD_STORE_FILE
printf 'Upload key alias: '; read -r VEIL_UPLOAD_KEY_ALIAS
printf 'Keystore password: '; read -rs VEIL_UPLOAD_STORE_PASSWORD; printf '\n'
printf 'Key password: '; read -rs VEIL_UPLOAD_KEY_PASSWORD; printf '\n'
export VEIL_UPLOAD_STORE_FILE VEIL_UPLOAD_KEY_ALIAS VEIL_UPLOAD_STORE_PASSWORD VEIL_UPLOAD_KEY_PASSWORD
./gradlew testDebugUnitTest :app:lintRelease :app:bundleRelease
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
unset VEIL_UPLOAD_STORE_FILE VEIL_UPLOAD_KEY_ALIAS VEIL_UPLOAD_STORE_PASSWORD VEIL_UPLOAD_KEY_PASSWORD
```

The bundle is `app/build/outputs/bundle/release/app-release.aab`. Check the `jarsigner` output for a verified signature and the expected **upload certificate**; a self-signed certificate can produce a trust-chain warning. Compare the SHA-256 certificate fingerprint with the upload certificate in Play Console for an existing app. The Gradle configuration rejects partially supplied signing variables. **When none are supplied, `bundleRelease` produces an unsigned bundle for build verification only; it cannot be uploaded to Play.** Signing environment variables are visible to processes under your user account while present, so use a trusted machine and clear them as shown.

## 3. Prepare Play Console content

- Create or select the correct app and configure Play App Signing. Complete Play Console's requested app access, ads, target audience, content rating, government/financial/health declarations and any applicable verification forms truthfully. Internal testing may have different review timing; check the Console's actual requirements.
- Publish a reviewed privacy policy at a stable public, non-geofenced URL. [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md) and the in-app policy are starting points; review both before publication and keep them synchronized. Include the developer/app identity and public support contact, accessibility and foreground-app observation, locally stored preferences and app rules, the user-selected photo URI and local photo access, retention/deletion, and any future collection or sharing. Review all SDKs before making final claims.
- Complete the Data safety form whenever Play requires it for the selected track. Base answers on the full shipped build and Play's definitions, distinguishing on-device processing from data transmitted off device. The current manifest has no internet permission, but this is not a substitute for reviewing every dependency and actual behavior.
- Complete the Accessibility API declaration as an app that is **not** an accessibility tool. Explain that the user explicitly enables the service to detect input-method window bounds and the foreground app for user-selected privacy rules. The service must not inspect accessibility node content. Keep the existing prominent disclosure and affirmative consent; review the current policy and the actual release behavior before submitting. Prepare the required reviewer video showing disclosure, consent and refusal flows, enabling access, and the feature in use.
- Declare the `specialUse` foreground service in Play Console. Explain the user-armed privacy overlay and ongoing notification, the effect of interruption, and provide the requested demonstration video. This service type is subject to Play review; manifest declarations alone do not establish eligibility.
- Provide the store listing, support contact, app icon and required screenshots, and document accessibility use in the listing. Capture screenshots and reviewer videos from the actual signed candidate, without exposing real personal data.

## 4. Verify before distributing

- The current Samsung/Gboard keyboard-boundary problem is unresolved. Test on the target phone before inviting testers; verify overlays, peeking, touch-through, permission revocation, keyboard visibility, lock screen, notifications, restart and tiles using [TEST_MATRIX.md](TEST_MATRIX.md).
- Upload the signed AAB to **Testing > Internal testing**, add the intended testers and follow the Console's instructions to make the release available. Testers install from the Play opt-in link and enable accessibility themselves.
- Do not assume the internal track removes every device restriction or guarantees policy acceptance. Record the package ID, version code, upload-certificate fingerprint, Play app-signing certificate fingerprint, and bundle hash in a secure release log.

## Official references

- [Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Internal testing](https://support.google.com/googleplay/android-developer/answer/9845334)
- [AccessibilityService declaration](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Foreground service declarations](https://support.google.com/googleplay/android-developer/answer/13392821)
- [Privacy policy requirements](https://support.google.com/googleplay/android-developer/answer/10144311)
