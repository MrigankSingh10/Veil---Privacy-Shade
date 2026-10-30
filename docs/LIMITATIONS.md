# Known limitations

- Keyboard visibility and foreground-app detection depend on accessibility window metadata. Floating, split, handwriting and OEM keyboards may report unusual bounds, and system surfaces or multi-window modes may behave differently.
- The veil deters casual observation; it cannot prevent screenshots, screen readers, cameras or privileged software. Content may be visible before the overlay appears.
- Android may stop background services or restrict foreground-service startup after reboot. VeilType cannot grant its own permissions; users can re-arm it from Setup if needed.
- Touch-through overlay opacity is capped on Android 12 and later. Other overlay apps can also cause Android to block touches beneath combined obscuring windows.
- A document provider can revoke or remove a chosen photo. The solid veil remains available as a fallback.
- Accessibility access and special-use foreground services require distribution policy review. The foreground notification may be less visible if notification permission is denied.
- Build and unit-test results do not establish device compatibility. The device matrix in `TEST_MATRIX.md` must be run on physical hardware.
