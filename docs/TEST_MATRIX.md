# Manual test matrix

Nothing below is marked passed until executed on hardware.

| Device / OS | Keyboard | Navigation | Portrait | Landscape | Rapid open/close | Split screen | Restart / revoke | Status |
|---|---|---|---|---|---|---|---|---|
| Pixel / Android 10 | Gboard | 3-button + gesture | — | — | — | — | — | Not run |
| Pixel / Android 11–13 | Gboard | gesture | — | — | — | — | — | Not run |
| Pixel / Android 14–16 | Gboard | gesture | — | — | — | — | — | Not run |
| Samsung One UI / supported OS | Samsung Keyboard | gesture | — | — | — | — | — | Not run |
| Samsung One UI / supported OS | Gboard | gesture | — | — | — | — | — | Not run |

Also verify password fields, permission dialogs, lock screen, floating keyboard, physical keyboard, control drag/toggle/dismiss, hold-to-peek, touch-through at 20%/80%/95% opacity, photo loss, whole-screen exit without a keyboard, app rules, overnight schedules, Quick Settings, notification denial, boot restoration and TalkBack coexistence.

An Android 17 emulator smoke test exercised onboarding, permission setup, arming, keyboard detection, cover bounds and toggling the cover off. The DataStore instrumented tests passed on that emulator. This does not replace the physical-device matrix above.
