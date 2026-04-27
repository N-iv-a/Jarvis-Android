package com.jarvis.app

import android.app.Application
import com.jarvis.app.data.habits.HabitSeeder
import com.jarvis.app.data.security.AppLockManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var appLockManager: AppLockManager

    @Inject
    lateinit var habitSeeder: HabitSeeder

    /**
     * Scope separato dal viewModelScope per lanciare lavoro one-shot all'avvio
     * (seeding abitudini). SupervisorJob: un errore non affonda tutto lo scope.
     * IO dispatcher: lavoro DB.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Eager-init del lock (v. documentazione di AppLockManager).
        appLockManager.hashCode()

        // Semina abitudini se DB vuoto. Fire-and-forget: se fallisce, l'app
        // continua a funzionare (le abitudini appariranno al successivo boot
        // quando retry-eremo, o zero abitudini → UI vuota con messaggio).
        appScope.launch {
            runCatching { habitSeeder.seedIfEmpty() }
        }
    }
}
