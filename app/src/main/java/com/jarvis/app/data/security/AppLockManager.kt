package com.jarvis.app.data.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce lo stato di blocco dell'app a livello di processo.
 *
 * Regole:
 * - All'avvio dell'app → BLOCCATO (isLocked = true)
 * - Quando l'app va in background → BLOCCATO
 * - Quando l'utente si autentica correttamente → SBLOCCATO
 * - Quando il biometric prompt stesso manda l'app in stop (caso del PIN
 *   di sistema che apre un'altra Activity) → NON riblocchiamo, altrimenti
 *   al ritorno l'utente si trova bloccato nonostante si sia appena autenticato.
 *   Per questo esiste il flag [authInProgress].
 *
 * Osserva [ProcessLifecycleOwner]: è l'unico modo corretto di sapere quando
 * TUTTA l'app (non la singola Activity) va in background — es. Home, recent apps,
 * altra app in primo piano, schermo spento.
 *
 * È @Singleton e viene iniettato eagerly in [JarvisApplication.onCreate]
 * per garantire che l'observer sia attivo PRIMA che qualsiasi Activity parta.
 */
@Singleton
class AppLockManager @Inject constructor() : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /**
     * Vero quando abbiamo mostrato il BiometricPrompt e stiamo aspettando il risultato.
     * Con DEVICE_CREDENTIAL attivo il sistema può aprire una Activity separata per il PIN,
     * portando la nostra app in ON_STOP → senza questo flag rilocchereremmo subito.
     */
    @Volatile
    private var authInProgress: Boolean = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Chiamato dal BiometricAuthManager PRIMA di mostrare il prompt. */
    fun onAuthStarted() {
        authInProgress = true
    }

    /** Chiamato quando l'autenticazione riesce. */
    fun onAuthSucceeded() {
        authInProgress = false
        _isLocked.value = false
    }

    /** Chiamato su errore o annullamento: non sblocchiamo, ma ripuliamo il flag. */
    fun onAuthFinished() {
        authInProgress = false
    }

    /**
     * Forza il blocco immediato. Utile se in futuro esporremo un pulsante "Blocca ora".
     */
    fun lockNow() {
        _isLocked.value = true
    }

    // --- ProcessLifecycle callbacks ---

    /**
     * Chiamato quando l'ULTIMA Activity visibile va in background.
     * Se non stiamo aspettando il risultato del prompt, blocchiamo.
     */
    override fun onStop(owner: LifecycleOwner) {
        if (!authInProgress) {
            _isLocked.value = true
        }
    }
}
