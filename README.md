# Jarvis Mobile

App Android nativa, personale e privata — task manager con sotto-task e un sistema di abitudini gamificato (punti settimanali), pensata per girare solo sul telefono, senza account né cloud.

> Nota sul repo: esiste una versione precedente in JavaScript (`Progetto_Jarvis`), abbandonata a favore di questa riscrittura nativa Android.

## 1. Problema

Le app di task/habit tracking esistenti (Todoist, Habitica, ecc.) impongono le loro regole: come si "vince" una settimana, come si assegnano i punti, cosa succede se modifichi un check-in a posteriori. Jarvis nasce per avere quella logica su misura — vedi `PointsService`, che riconcilia il punteggio a ogni modifica di check-in invece di limitarsi a sommare spunte — e per tenere i dati (task, abitudini, note personali) fuori dai server di terzi.

## 2. Approccio

App Android nativa a singola Activity, Jetpack Compose + Hilt per l'injection, Room per la persistenza.

```
app/src/main/java/com/jarvis/app/
├── data/
│   ├── db/          Room database (JarvisDatabase)
│   ├── security/     AppLockManager, BiometricAuthManager, DatabasePassphraseManager
│   ├── tasks/        Entity/Dao/Repository per task + sotto-task
│   └── habits/       Entity/Dao/Repository per abitudini, check-in, punteggio settimanale
├── domain/
│   ├── tasks/         Modelli di dominio task
│   └── habits/        PointsService (motore punti), WeeklyProgress
└── ui/
    ├── screens/        Lock screen, welcome
    ├── tasks/          Liste, editor
    ├── habits/          Liste, regole, viewmodel
    └── navigation/      Grafo di navigazione Compose
```

Il database Room è cifrato con SQLCipher; la passphrase (32 byte casuali) viene generata al primo avvio e salvata in `EncryptedSharedPreferences`, a sua volta protetta da una chiave nell'Android Keystore (hardware-backed dove disponibile). L'app si blocca automaticamente ogni volta che va in background (`AppLockManager`, agganciato al `ProcessLifecycleOwner`) e richiede sblocco biometrico per rientrare.

## 3. Scelte e trade-off

- **Nativo Android invece di un backend + webapp.** Per uno strumento a uso singolo (io, sul mio telefono), un server da mantenere online sarebbe complessità senza beneficio: nessuna sincronizzazione multi-dispositivo è richiesta oggi. Il costo è che i dati vivono solo su quel telefono — vedi anti-feature sotto.
- **SQLCipher invece di un DB in chiaro.** Task e abitudini personali (incluse eventuali note) non devono essere leggibili nemmeno da un backup ADB o da un telefono rootato.
- **Passphrase generata a runtime, mai hardcoded.** Nessun segreto nel codice o nella configurazione di build: la chiave vive esclusivamente nel Keystore del dispositivo.
- **Riconciliazione dello stato invece di eventi accumulativi** in `PointsService`: ogni check-in ricalcola se la settimana è "vinta" e confronta con lo stato salvato, invece di sommare punti a ogni tap. Più codice, ma corretto anche quando l'utente toglie uno spunta messo per errore.

## 4. Anti-features

Scelte deliberate, non dimenticanze:

- **Nessun account, nessun cloud sync.** `allowBackup=false`, nessun permesso `INTERNET` nel manifest. Se cambi telefono senza backup manuale, i dati restano sul vecchio.
- **Nessuna copia esterna del database.** Non esiste un export automatico: nel modello di minaccia di quest'app, la comodità di un backup su Drive pesa meno del rischio di un DB in chiaro fuori dal Keystore del dispositivo.
- **Nessuna gamification "social".** Niente classifiche, niente condivisione punti: sono punti per me, non per competere.

## 5. Design principles

- **Stato bloccato di default.** `AppLockManager` parte con `isLocked = true` e si riblocca a ogni `onStop` del processo — l'eccezione esplicita è il flag `authInProgress`, per non ribloccare l'app nel breve istante in cui il prompt biometrico di sistema apre la propria Activity.
- **Idempotenza sopra tutto in `PointsService`.** Il ledger punti si scrive solo quando lo stato "vinta/non vinta" della settimana cambia davvero: rieseguire la riconciliazione più volte sullo stesso stato non produce duplicati.

## 6. Cosa farei diversamente

- `PointsService.reconcileWeek` scrive l'inserimento nel ledger e l'aggiornamento dello score in due chiamate separate invece che in una `@Transaction` Room: un crash tra le due lascerebbe il ledger leggermente disallineato (si auto-corregge al check-in successivo, ma non dovrebbe poter succedere).
- Il nome della cartella di progetto (`The Sims/`, visibile nel primo README) era un residuo del setup iniziale, non un nome scelto: va ripulito quando riorganizzo la struttura.
- Nessun test automatico ancora, né per `PointsService` né per i DAO — è la lacuna più seria del repo a oggi, soprattutto data la logica non banale di riconciliazione.

## 7. Roadmap — stato attuale

- [x] Task + sotto-task (CRUD completo)
- [x] Abitudini con check-in, punteggio settimanale, motore di riconciliazione punti
- [x] Blocco app + sblocco biometrico
- [x] Database cifrato (SQLCipher + Keystore)
- [ ] Finance tracker (in progetto iniziale, non ancora avviato)
- [ ] Test automatici (unit su `PointsService`, DAO)
- [ ] Export/backup manuale locale (fuori dal cloud, su richiesta esplicita dell'utente)

## Screenshot

_[da aggiungere]_

## Setup

- **Android Studio** (Hedgehog 2023.1.1 o più recente) — https://developer.android.com/studio
- **JDK 17** (incluso in Android Studio)
- Telefono Android API 26+ con debug USB attivo

```
Apri Android Studio → Open → seleziona la cartella del progetto
Attendi la sincronizzazione Gradle (~500MB al primo avvio)
Collega il telefono via USB, autorizza il debug
Run ▶ (Shift+F10)
```

Per un APK installabile in modo permanente: `Build → Generate Signed Bundle / APK`, salvando il keystore **fuori dal repo** (es. `~/keystores/jarvis.jks` — senza quel file non potrai più aggiornare l'app installata).

---

*Costruito con l'assistenza di IA (Claude); architettura, scelte di modellazione e trade-off sono miei.*
