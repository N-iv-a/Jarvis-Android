package com.jarvis.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce la passphrase che cifra il database SQLCipher.
 *
 * Flusso:
 * 1. Al primo avvio generiamo una passphrase casuale di 32 byte (256 bit entropy reale).
 * 2. La salviamo in EncryptedSharedPreferences, a sua volta cifrata con una chiave
 *    AES-256 gestita dall'Android Keystore (hardware-backed se il device lo supporta).
 * 3. Ai successivi avvii, leggiamo la passphrase decifrata e la passiamo a SQLCipher.
 *
 * Risultato: il database NON è mai leggibile da fuori l'app anche se il telefono
 * viene rootato o il backup ADB viene estratto. La chiave vive solo in Keystore,
 * che è un hardware security module (TEE/StrongBox) sui Pixel/Samsung recenti.
 *
 * NOTA: se l'utente disinstalla o pulisce i dati dell'app, la passphrase sparisce
 * e il DB diventa irrecuperabile. Questo è il comportamento desiderato.
 */
@Singleton
class DatabasePassphraseManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext
    private val context: Context,
) {

    private val prefs by lazy {
        // MasterKey: la chiave maestra gestita dal Keystore.
        // AES256_GCM è il preset moderno raccomandato da AndroidX Security.
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
            // Schema di cifratura per le CHIAVI delle preferenze (i "nomi")
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            // Schema di cifratura per i VALORI — AES-256 in GCM (authenticated encryption)
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Restituisce la passphrase come ByteArray (attenzione: dati sensibili, non loggare).
     * Alla prima invocazione ne genera una nuova e la persiste.
     */
    fun getOrCreatePassphrase(): ByteArray {
        // Le SharedPreferences memorizzano String, non ByteArray.
        // Codifichiamo/decodifichiamo in esadecimale — 64 caratteri per 32 byte.
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return existing.hexToBytes()
        }

        // Genera passphrase nuova
        val bytes = ByteArray(PASSPHRASE_LENGTH_BYTES).also {
            SecureRandom().nextBytes(it)
        }

        // Salva in hex (EncryptedSharedPreferences supporta solo tipi primitivi/String)
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, bytes.toHex())
            .apply()

        return bytes
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0) { "Hex string deve avere lunghezza pari" }
        return ByteArray(length / 2) { i ->
            ((this[i * 2].digitToInt(16) shl 4) or this[i * 2 + 1].digitToInt(16)).toByte()
        }
    }

    companion object {
        private const val PREF_FILE_NAME = "jarvis_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase_v1"
        private const val PASSPHRASE_LENGTH_BYTES = 32 // 256 bit
    }
}
