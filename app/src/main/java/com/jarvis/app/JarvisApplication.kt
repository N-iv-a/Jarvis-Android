package com.jarvis.app

import android.app.Application
import com.jarvis.app.data.security.AppLockManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Classe Application del progetto.
 *
 * @HiltAndroidApp genera il componente DI di Hilt al build time.
 * È l'unico punto di ingresso del grafo DI: ogni altra classe annotata
 * (Activity, ViewModel, ecc.) lo riceve da qui.
 *
 * Dichiarata in AndroidManifest.xml con android:name=".JarvisApplication".
 */
@HiltAndroidApp
class JarvisApplication : Application() {

    /**
     * Iniettato eagerly per garantire che l'observer del ProcessLifecycle
     * venga registrato PRIMA che qualsiasi Activity parta. Senza questo,
     * il primo ON_STOP dopo il lancio potrebbe non essere intercettato.
     *
     * Il riferimento non serve a nulla a runtime: è solo un "trigger"
     * per far sì che Hilt costruisca il singleton subito.
     */
    @Inject
    lateinit var appLockManager: AppLockManager

    override fun onCreate() {
        super.onCreate()
        // Tocchiamo il field per essere sicuri che venga inizializzato.
        // Non è strettamente necessario (Hilt lo fa comunque prima di onCreate
        // terminare grazie a @Inject lateinit), ma rende l'intento esplicito.
        appLockManager.hashCode()
    }
}
