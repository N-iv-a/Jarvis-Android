package com.jarvis.app.data.habits

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO per il libro mastro dei punti.
 *
 * - `observeBalance`: saldo in tempo reale come Flow (per la TopBar).
 *   Notare il `?: 0`: SUM su tabella vuota torna NULL in SQLite, non 0.
 * - `observeRecent`: storico recente per la pagina "punti".
 */
@Dao
interface PointTransactionDao {

    @Insert
    suspend fun insert(tx: PointTransactionEntity): Long

    @Query("SELECT IFNULL(SUM(delta), 0) FROM point_transactions")
    fun observeBalance(): Flow<Int>

    @Query("SELECT IFNULL(SUM(delta), 0) FROM point_transactions")
    suspend fun getBalance(): Int

    @Query(
        """
        SELECT * FROM point_transactions
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int = 50): Flow<List<PointTransactionEntity>>
}
