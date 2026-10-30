# Implementation status

The core replication uses an armed foreground service and application overlays, with accessibility window observation, local app rules and schedules, control customization, photo selection, boot restoration and Quick Settings controls. The project remains offline and has no purchase or account system.

Unit tests cover activation decisions and schedule boundaries; instrumented tests cover DataStore persistence. An Android 17 emulator smoke test verified onboarding, arming and keyboard-cover geometry. Remaining validation includes the physical device matrix, automated Compose navigation coverage, and instrumented tests for system integrations such as boot, tiles, photo access and permission revocation. See `TEST_MATRIX.md`.
