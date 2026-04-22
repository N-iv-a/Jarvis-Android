package com.jarvis.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity di servizio: memorizza metadati dell'app a livello di database.
 *
 * Per ora contiene solo la data del primo avvio — ma è il posto giusto
 * dove accumulare in futuro cose tipo:
 * - ultima sync con servizi esterni
 * - flag di onboarding completato
 * - numero di versione dello schema applicato
 *
 * Usiamo un ID fisso (= 1) perché in questa tabella vive UNA sola riga:
 * pattern "singleton row". Più semplice di SharedPreferences per cose
 * che vogliamo all'interno del DB cifrato.
 */
@Entity(tableName = "app_state")
data class AppState(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val firstLaunchAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
