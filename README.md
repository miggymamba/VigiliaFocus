[![VigiliаFocus CI](https://github.com/miggymamba/VigiliaFocus/actions/workflows/ci.yml/badge.svg)](https://github.com/miggymamba/VigiliaFocus/actions/workflows/ci.yml)

# Vigiliа Focus

**Vigiliа Focus** is a Pomodoro timer built with Kotlin Multiplatform and Compose Multiplatform, targeting Android with iOS as a stretch goal.

Rather than treating a timer app as a trivial countdown, this project approaches it as a study in **shared cross-platform architecture** — proving that domain logic, UI state, and Compose UI can live entirely in `commonMain` while platform-specific concerns remain isolated in thin actuals.

## Key Features

- **Shared UI:** Compose Multiplatform renders the same UI on Android and iOS from a single `commonMain` source set.
- **Shared Domain:** Timer logic, session state, and use cases are pure Kotlin with zero platform imports.
- **MVI State Management:** Immutable `TimerUiState` driven by `StateFlow` ensures predictable rendering across configuration changes.
- **`expect/actual` Platform Bridge:** Platform clock and timer tick are abstracted behind a clean contract, with each target providing its own implementation.
- **Settings Persistence:** Timer durations survive app restarts via multiplatform-settings.
- **Foreground Service:** Active countdowns keep ticking in the background via an Android ForegroundService with a live notification.
- **Completion Feedback:** Audio and haptic alert on session end via `PlatformAlerter` — `RingtoneManager` + `Vibrator` on Android.

---

## Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.2-4285F4?logo=jetpackcompose&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-4.1.1-F8821A)
![License](https://img.shields.io/badge/License-Apache%202.0-green)

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform — shared across Android and iOS |
| State | MVI — `StateFlow` + sealed interface |
| DI | Koin — KMP-native, no annotation processing |
| Persistence | Multiplatform Settings (`commonMain`) — settings storage |
| Platform Timer | `expect/actual` — wraps platform clock per target |
| Background | Android ForegroundService — keeps countdown alive when backgrounded |
| Navigation | Compose Navigation — single `NavHost` in `commonMain` |
| Testing | `kotlin.test` in `commonTest` + Compose UI tests in `androidDeviceTest` |
| CI | GitHub Actions |

---

## Technical Architecture

This project follows **Clean Architecture** with a KMP/CMP source set layout. Domain and UI are fully shared; platform actuals are thin and isolated.

### Source Set Breakdown

- **`commonMain`**: Compose UI, ViewModels, domain models, use cases, repository interfaces, and `expect` declarations. No platform imports.
- **`androidMain`**: Android actuals — `PlatformTimer.android.kt`, `PlatformAlerter.android.kt`, `TimerForegroundService.kt`.
- **`iosMain`**: iOS actuals — `PlatformTimer.ios.kt`. (Phase 6)
- **`androidApp`**: Entry point only — `MainActivity` calls `setContent { App() }`, nothing else.

```
VigiliаFocus/
├── composeApp/
│   ├── commonMain/       ← Compose UI, ViewModels, domain, use cases, data, expect
│   ├── androidMain/      ← PlatformTimer, PlatformAlerter, TimerForegroundService
│   └── iosMain/          ← PlatformTimer iOS actual (Phase 6)
└── androidApp/           ← Entry point only (MainActivity)
```

### Layer Breakdown

- **`domain/`**: Pure Kotlin. `TimerMode`, `TimerState`, `TimerSettings`, `Session`, use cases, and `ISettingsRepository` interface. Zero platform or Android dependencies.
- **`presentation/`**: `TimerViewModel`, `SettingsViewModel`, `TimerScreen`, `SettingsScreen`. MVI pattern — UI observes `StateFlow`, emits intents.
- **`platform/`**: `expect class PlatformTimer` and `expect class PlatformAlerter` — the only seams between shared and platform code.
- **`di/`**: Koin modules. `appModule` in `commonMain`, `androidModule` in `androidMain`.

---

## What This Project Demonstrates

- Shared Compose UI across Android and iOS from a single source set
- `expect/actual` pattern for platform capability bridging
- Clean Architecture applied to KMP — domain layer with zero platform imports
- Koin dependency injection in a multiplatform context
- MVI state management with `StateFlow` and sealed interfaces
- Explicit UI state machine — `TimerUiState` sealed interface routes `Idle`, `Running`, `Paused`, and `Completed` to dedicated composables
- Android ForegroundService integration with live notification updates
- `kotlin.test` unit testing in `commonTest` — no Android test runner required
- Compose UI instrumented tests in `androidDeviceTest`

---

## Development Setup

1. Clone the repository.
2. Open in **Android Studio Panda 2025.3.2** or later.
3. Sync Gradle.
4. Run on an Android emulator or device (API 26+).

---

## Testing Strategy

- **Domain Layer:** Use case and state transition tests in `commonTest` using `kotlin.test`. No mocking framework required — pure functions.
- **UI Layer:** `runComposeUiTest` instrumented tests in `androidDeviceTest` verifying timer display, Start/Pause toggle, mode label transitions, and the session completion screen.

---

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    participant UI as Compose UI
    participant VM as TimerViewModel
    participant USE as UseCases
    participant SET as SettingsRepo
    participant PT as PlatformTimer (expect/actual)
    participant AL as PlatformAlerter (expect/actual)

    Note over UI, AL: [TIMER EXECUTION FLOW - KMP ARCHITECTURE]

    UI->>VM: UserIntent.Start
    VM->>USE: StartTimerUseCase()
    USE-->>VM: TimerState(isRunning=true)
    VM->>PT: start(durationSeconds, onTick, onFinish)
    activate PT

    loop Every 1 Second
        PT-->>VM: onTick(remaining)
        VM->>VM: _timerState.update(remainingSeconds)
        VM-->>UI: Recompose (TimerUiState.Running)
    end

    Note over UI, AL: [SESSION COMPLETION - USER ACKNOWLEDGEMENT REQUIRED]

    PT-->>VM: onFinish()
    deactivate PT
    VM->>AL: playCompletionAlert()
    VM->>PT: reset()
    VM->>VM: _timerState stays at remainingSeconds=0
    VM-->>UI: Recompose (TimerUiState.Completed)
    UI-->>UI: Show TimerCompleteLayout

    UI->>VM: UserIntent.Skip (user taps SKIP)
    VM->>USE: SkipToNextModeUseCase()
    USE-->>VM: TimerState(nextMode, fullDuration, isRunning=false)
    VM-->>UI: Recompose (TimerUiState.Idle, next mode)

    Note over UI, AL: [SETTINGS SYNC]

    SET-->>VM: getSettings() emission
    VM->>USE: ResetTimerUseCase() if isAtStart
    VM-->>UI: Recompose (updated duration)
```

---

## Screenshots

### Dark Mode

| Feature | Screenshot |
|---|---|
| Focus Mode (Running)  | <img src="docs/images/timer_focus_dark.png" width="300" alt="Timer Focus Mode (Dark)" /> |
| Break Mode (Idle)  | <img src="docs/images/timer_break_dark.png" width="300" alt="Timer Break Mode (Dark)" /> |
| Settings  | <img src="docs/images/settings_dark.png" width="300" alt="Settings Screen (Dark)" /> |

### Light Mode

| Feature | Screenshot |
|---|---|
| Focus Mode (Running)  | <img src="docs/images/timer_focus_light.png" width="300" alt="Timer Focus Mode (Light)" /> |
| Break Mode (Idle)  | <img src="docs/images/timer_break_light.png" width="300" alt="Timer Break Mode (Light)" /> |
| Settings  | <img src="docs/images/settings_light.png" width="300" alt="Settings Screen (Light)" /> |

---

## Future Roadmap

- **iOS Support:** `PlatformTimer.ios.kt` actual using `NSTimer` or `DispatchQueue`. Full iOS validation. (Phase 6)
- **Session History:** Persist completed sessions with timestamps for productivity tracking.

---

## License

Vigiliа Focus is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) for details.