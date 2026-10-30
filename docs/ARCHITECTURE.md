# Architecture

VeilType is an offline single-module Compose application. DataStore holds preferences, app rules, a schedule and the optional persisted photo URI. Existing enabled, opacity, color, control position and onboarding keys remain readable; the legacy automatic mode is used as the default automation setting when explicitly stored.

The accessibility service observes top-level window types and bounds and foreground package changes. It publishes a window snapshot without requesting accessibility node text. The foreground service observes that snapshot and DataStore, evaluates the current schedule and app rule, and owns the application-overlay cover and keyboard control. A pure reducer resolves activation decisions. The service is armed from the app or tile, maintains a notification, and removes windows on disarm, loss of overlay permission, screen lock, or destruction. The boot receiver restores an armed service only when opted in and permissions are still present.

Application overlays require display-over-apps access. On Android 12 and later, the touch-through cover is capped at 80% window opacity to meet the system's touch-obscuring limit. The screen veil action is an explicit manual action and overrides app skip rules; schedules affect automatic behavior only. Pressing Off suppresses automatic activation for the current keyboard session.

The accessibility service is not declared as an accessibility tool. Release distribution needs appropriate Android and store policy review. No internet, analytics, billing, account, or media-library permission is used.
