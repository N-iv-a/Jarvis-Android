package com.jarvis.app.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Risultato del check di disponibilità biometrica.
 */
enum class BiometricAvailability {
    /** Almeno impronta/faccia O un device credential (PIN/pattern/password) disponibili. */
    AVAILABLE,
    /** Il device non ha NÉ biometria NÉ credential configurati — blocco impossibile. */
    NOT_CONFIGURED,
    /** Hardware biometrico non disponibile/temporaneamente inutilizzabile. */
    HARDWARE_UNAVAILABLE,
}

/**
 * Wrapper su AndroidX BiometricPrompt.
 *
 * Usiamo la combinazione:
 *   BIOMETRIC_STRONG | DEVICE_CREDENTIAL
 *
 * - BIOMETRIC_STRONG = impronta / volto affidabile (classe 3)
 * - DEVICE_CREDENTIAL = PIN / pattern / password del telefono
 *
 * L'OR permette all'utente di ricadere sul PIN se il sensore biometrico
 * non funziona o non è configurato → nessun rischio di lockout dell'app.
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLockManager: AppLockManager,
) {

    private val allowedAuthenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Verifica se il device ha un metodo di autenticazione utilizzabile.
     * Chiamato prima di mostrare il prompt: se NOT_CONFIGURED, l'utente
     * deve prima impostare un blocco schermo nelle impostazioni di sistema.
     */
    fun checkAvailability(): BiometricAvailability {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_CONFIGURED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.HARDWARE_UNAVAILABLE
            else -> BiometricAvailability.HARDWARE_UNAVAILABLE
        }
    }

    /**
     * Mostra il prompt di autenticazione.
     *
     * @param activity  deve essere una FragmentActivity (requisito di BiometricPrompt).
     *                  MainActivity lo è perché estende FragmentActivity.
     * @param onSuccess chiamato quando l'utente si autentica correttamente.
     * @param onError   chiamato solo su errori "veri" (hardware, troppi tentativi, ecc.),
     *                  NON se l'utente preme "Annulla" — in quel caso semplicemente
     *                  la LockScreen resta mostrata senza messaggio d'errore.
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                appLockManager.onAuthSucceeded()
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                appLockManager.onAuthFinished()
                // Errori "silenziosi" che NON sono veri fallimenti:
                // l'utente ha annullato, chiuso il dialog, o premuto il tasto negativo.
                val isUserCancel = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                if (!isUserCancel) {
                    onError(errString.toString())
                }
            }

            // onAuthenticationFailed viene chiamato a OGNI tentativo sbagliato
            // (es. dito sporco). Non notifichiamo: il sistema mostra già il feedback.
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sblocca Jarvis")
            .setSubtitle("Conferma la tua identità")
            .setAllowedAuthenticators(allowedAuthenticators)
            // NOTA: con DEVICE_CREDENTIAL abilitato NON si può impostare setNegativeButtonText
            // (altrimenti crash). Il fallback al PIN è gestito dal sistema.
            .build()

        appLockManager.onAuthStarted()
        prompt.authenticate(info)
    }
}
