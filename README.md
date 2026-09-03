# Jarvis Mobile

A native, personal, private Android app — a task manager with subtasks and a gamified habit system (weekly points), meant to live entirely on the phone, with no account and no cloud.

> Repo note: there's a previous JavaScript version (`Progetto_Jarvis`), abandoned in favor of this native Android rewrite — see "Evolution" below.

## Evolution: from web app to native Android

The first version (`Progetto_Jarvis`, a single commit, later abandoned) was a classic web app: **FastAPI + React (Vite) + SQLite**, with two working modules — a task manager and a finance tracker (automatic sync from BudgetBakers Wallet via API, with IBAN masking on account names) — plus a planned Google Calendar integration via OAuth.

The backend-plus-webapp model stopped making sense for how I actually use this tool:

- **Keeping a server running for a single user (me) is complexity without benefit.** No multi-device sync was ever really needed — just my phone.
- **The sensitive-configuration surface was growing, not shrinking:** a Google OAuth client secret, a Wallet API token, a `.env` to protect server-side — all avoidable if the data stays on-device, behind the Keystore.
- **A native app is simply there, on the phone, locked until I unlock it with a fingerprint** — no "open the browser, point it at localhost".

The finance tracker hasn't been ported back (yet): integrating an external API (Wallet) reintroduces exactly the problem the native rewrite was meant to remove — a token that has to be kept safe on-device. It's on the roadmap, but it needs to be rethought for the new model, not just ported over.

## 1. Problem

Existing task/habit-tracking apps (Todoist, Habitica, etc.) impose their own rules: how a week is "won", how points are awarded, what happens if you edit a check-in after the fact. Jarvis exists to have that logic custom-built — see `PointsService`, which reconciles the score on every check-in change instead of just summing ticks — and to keep the data (tasks, habits, personal notes) off third-party servers.

## 2. Approach

Native Android app, single Activity, Jetpack Compose + Hilt for injection, Room for persistence.

```
app/src/main/java/com/jarvis/app/
├── data/
│   ├── db/          Room database (JarvisDatabase)
│   ├── security/     AppLockManager, BiometricAuthManager, DatabasePassphraseManager
│   ├── tasks/        Entity/Dao/Repository for tasks + subtasks
│   └── habits/       Entity/Dao/Repository for habits, check-ins, weekly score
├── domain/
│   ├── tasks/         Task domain models
│   └── habits/        PointsService (points engine), WeeklyProgress
└── ui/
    ├── screens/        Lock screen, welcome
    ├── tasks/          Lists, editor
    ├── habits/          Lists, rules, viewmodel
    └── navigation/      Compose navigation graph
```

The Room database is encrypted with SQLCipher; the passphrase (32 random bytes) is generated on first launch and stored in `EncryptedSharedPreferences`, itself protected by a key in the Android Keystore (hardware-backed where available). The app locks automatically every time it goes to background (`AppLockManager`, hooked into `ProcessLifecycleOwner`) and requires biometric unlock to come back.

## 3. Choices and trade-offs

- **Native Android instead of a backend + webapp.** For a single-user tool (me, on my phone), a server to keep online would be complexity without benefit: no multi-device sync is actually required today. The cost is that data lives only on that phone — see the anti-feature below.
- **SQLCipher instead of a plaintext DB.** Personal tasks and habits (including any notes) shouldn't be readable even from an ADB backup or a rooted phone.
- **Passphrase generated at runtime, never hardcoded.** No secret in the code or build config: the key lives exclusively in the device Keystore.
- **State reconciliation instead of accumulating events** in `PointsService`: every check-in recomputes whether the week is "won" and compares it against the saved state, instead of just summing points on every tap. More code, but correct even when the user un-ticks a check-in made by mistake.

## 4. Anti-features

Deliberate choices, not oversights:

- **No account, no cloud sync.** `allowBackup=false`, no `INTERNET` permission in the manifest. Switch phones without a manual backup, and the data stays on the old one.
- **No external database copy.** There's no automatic export: in this app's threat model, the convenience of a Drive backup weighs less than the risk of a plaintext DB outside the device Keystore.
- **No "social" gamification.** No leaderboards, no sharing points: they're points for me, not for competing.

## 5. Design principles

- **Locked by default.** `AppLockManager` starts with `isLocked = true` and re-locks on every process `onStop` — the explicit exception is the `authInProgress` flag, so the app doesn't re-lock in the brief moment the system biometric prompt opens its own Activity.
- **Idempotency above all in `PointsService`.** The points ledger is only written when the week's "won/not won" state actually changes: re-running reconciliation multiple times on the same state produces no duplicates.

## 6. What I'd do differently

- `PointsService.reconcileWeek` writes the ledger insert and the score update as two separate calls instead of a single Room `@Transaction`: a crash between the two would leave the ledger slightly out of sync (it self-corrects on the next check-in for that week, but it shouldn't be able to happen at all).
- The project folder name (`The Sims/`, visible in the first README) was a leftover from the initial setup, not a chosen name — needs cleaning up next time the structure gets reorganized.
- No automated tests yet, neither for `PointsService` nor the DAOs — the most serious gap in the repo today, especially given the non-trivial reconciliation logic.

## 7. Roadmap — current state

- [x] Tasks + subtasks (full CRUD)
- [x] Habits with check-ins, weekly score, points-reconciliation engine
- [x] App lock + biometric unlock
- [x] Encrypted database (SQLCipher + Keystore)
- [ ] Finance tracker (built in the original project, not yet started here)
- [ ] Automated tests (unit tests for `PointsService`, DAOs)
- [ ] Manual local export/backup (kept off the cloud, on explicit user request)

## Screenshots

_[to add]_

## Setup

- **Android Studio** (Hedgehog 2023.1.1 or newer) — https://developer.android.com/studio
- **JDK 17** (bundled with Android Studio)
- An Android phone, API 26+, with USB debugging enabled

```
Open Android Studio → Open → select the project folder
Wait for Gradle sync (~500MB on first run)
Connect the phone via USB, authorize debugging
Run ▶ (Shift+F10)
```

For a permanently installable APK: `Build → Generate Signed Bundle / APK`, saving the keystore **outside the repo** (e.g. `~/keystores/jarvis.jks` — without that file you can no longer update the installed app).

---

*Built with AI assistance (Claude); architecture, modeling choices and trade-offs are mine.*
