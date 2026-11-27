# SAFE — Personal Safety Network (Android)

An Android application built with Kotlin and Jetpack Compose (Material 3), providing a personal safety net when traveling or doing activities alone.

## Core Features

- **5-Step Session Creation Wizard**: Choose activity type, destination (presets or custom), estimated duration/ETA, trusted contact, and consent-based tracking summary.
- **Active Session Monitor**: Live elapsed/remaining time & ETA, custom route progress map (`SafeMapView`), pause/resume, extend duration, arrival confirmation, and end-of-session summary.
- **Configurable Detection Engine & State Machine**: Strict session transitions (`DRAFT -> READY -> ACTIVE / PAUSED / WARNING / ESCALATED -> COMPLETED / CANCELLED / EXPIRED`), delay warnings, prolonged stationary detection, and GPS-accuracy-guarded route deviation checks.
- **Scheduled Check-ins**: Schedule standalone time-based check-ins with customizable alert drafts and grace-period escalation.
- **SOS & Alert Protocol**: Two-step confirmation before opening the system dialer (`ACTION_DIAL`) for regional emergency numbers (`FR`, `BE`, `CH`, `CA`, `GB`, `US`, `EU`), non-alarmist trusted contact message sharing (`ACTION_SEND`), and clipboard/share-sheet location sharing.
- **Trusted Contacts & Activity History**: Full CRUD for trusted contacts with explicit consent confirmation, and privacy-aware history retention (`none`, `24h`, `7d`, `30d`) with one-tap local data deletion.
- **Demo / Simulation Mode**: Interactive bottom-sheet simulator for testing progress, elapsed time, anomalies, check-ins, and device conditions (low battery, offline, imprecise GPS).
- **Multi-language & Theme**: Supports `FR`, `EN`, `ES`, `DE`, `IT` and Light/Dark themes with locally bundled Inter typography.
