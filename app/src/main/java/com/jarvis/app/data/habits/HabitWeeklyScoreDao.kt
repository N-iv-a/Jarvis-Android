package com.jarvis.app.data.habits

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO della tabella di stato "vinto/non vinto" per (abitudine, settimana).
 *
 * NB: tutte le operazioni qui sono `suspend` non Flow perché lo stato non
 * pilota direttamente la UI (la UI calcola da check-in). È solo memoria
 * interna del PointsService per decidere se accreditare/stornare.
 */
@Dao
interface HabitWeeklyScoreDao {

    @Query(
        """
        SELECT * FROM habit_weekly_scores
        WHERE habit_id = :habitId AND week_start_epoch_day = :weekStart
        LIMIT 1
        """
    )
    suspend fun getFor(habitId: Long, weekStart: Long): HabitWeeklyScoreEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: HabitWeeklyScoreEntity): Long

    @Update
    suspend fun update(row: HabitWeeklyScoreEntity)
}
