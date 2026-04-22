# Jarvis Mobile

App Android personale e privata — task manager + finance tracker.

## Prerequisiti

- **Android Studio** (Hedgehog 2023.1.1 o più recente) — gratis: https://developer.android.com/studio
- **JDK 17** (incluso in Android Studio)
- Un telefono Android (API 26+, cioè Android 8.0 o più recente) con **debug USB attivo**

## Primo avvio

1. Apri Android Studio → **Open** → seleziona questa cartella (`The Sims/`)
2. Attendi la sincronizzazione Gradle (la prima volta scarica ~500MB di dipendenze)
3. Collega il telefono via USB, autorizza il debug quando richiesto
4. Premi il tasto **Run ▶** (o Shift+F10)

L'APK viene installato e aperto automaticamente sul telefono.

## Build APK firmato (per installazione permanente)

```
Build → Generate Signed Bundle / APK → APK → Create new keystore...
```

Salva il file `.jks` fuori dal repo (es. `~/keystores/jarvis.jks`). **Non perderlo**: senza keystore non puoi più aggiornare l'app installata, devi disinstallare e reinstallare perdendo i dati.

## Architettura

```
app/src/main/java/com/jarvis/app/
├── JarvisApplication.kt     # Entry point Hilt
├── MainActivity.kt          # Unica Activity (Compose singola)
├── ui/
│   ├── theme/               # Colori, tipografia, tema scuro
│   └── navigation/          # Grafo di navigazione
├── data/
│   ├── db/                  # Room + SQLCipher
│   └── security/            # Keystore, cifratura
└── features/
    ├── tasks/               # (da implementare)
    └── finance/             # (da implementare)
```

## Sicurezza

- **DB cifrato**: Room + SQLCipher, passphrase generata al primo avvio e salvata nell'Android Keystore (hardware-backed su dispositivi recenti).
- **No backup cloud**: `allowBackup=false` impedisce che Google Drive copi i dati.
- **No cleartext**: `usesCleartextTraffic=false` blocca HTTP non cifrato.
- **Nessun permesso INTERNET** per ora — verrà aggiunto solo quando implementeremo sync esterno.
- **Biometric lock**: all'apertura l'app richiede impronta/faccia (da implementare).
