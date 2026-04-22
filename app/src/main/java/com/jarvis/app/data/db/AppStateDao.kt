package com.jarvis.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO = Data Access Object. Espone le query SQL verso l'entity in modo type-safe.
 *
 * Room genera l'implementazione concreta a compile-time (KSP):
 * quando chiami `dao.get()`, esegue davvero la query SQL qui annotata.
 */
@Dao
interface AppStateDao {

    @Query("SELECT * FROM app_state WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = AppState.SINGLETON_ID): AppState?

    /**
     * REPLACE: se esiste già una riga con lo stesso id, la sovrascrive.
     * Combinato con l'id fisso, garantisce "una sola riga sempre".
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: AppState)
}
