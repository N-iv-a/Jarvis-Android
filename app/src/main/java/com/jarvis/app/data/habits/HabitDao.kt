package com.jarvis.app.data.habits

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO per abitudini e check-in.
 *
 * - `observeActiveHabits` torna Flow → UI reattiva.
 * - `observeCheckInsBetween` permette di caricare in blocco una settimana
 *   senza una query per abitudine (performance e meno roundtrip).
 * - `upsertCheckIn` fa OnConflictStrategy.REPLACE: se esiste già una riga
 *   per (habitId, dateEpochDay), la sovrascrive. Combinato con l'indice
 *   unico composito, garantisce "un check-in per giorno per abitudine".
 */
@Dao
interface HabitDao {

    // --------------- Habits ---------------

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY ordine ASC, id ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHabit(habit: HabitEntity): Long

    // --------------- Check-ins ---------------

    /**
     * Tutti i check-in in un intervallo di giorni (estremi inclusi).
     * Usato per caricare una settimana intera in un colpo.
     */
    @Query(
        """
        SELECT * FROM habit_checkins
        WHERE date_epoch_day BETWEEN :fromEpochDay AND :toEpochDay
        """
    )
    fun observeCheckInsBetween(
        fromEpochDay: Long,
        toEpochDay: Long,
    ): Flow<List<HabitCheckInEntity>>

    /**
     * Un check-in per una specifica abitudine e data (serve per leggere
     * il valore attualmente salvato quando l'utente edita).
     */
    @Query(
        """
        SELECT * FROM habit_checkins
        WHERE habit_id = :habitId AND date_epoch_day = :dateEpochDay
        LIMIT 1
        """
    )
    suspend fun getCheckIn(habitId: Long, dateEpochDay: Long): HabitCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)

    @Query(
        """
        DELETE FROM habit_checkins
        WHERE habit_id = :habitId AND date_epoch_day = :dateEpochDay
        """
    )
    suspend fun deleteCheckIn(habitId: Long, dateEpochDay: Long)
}
