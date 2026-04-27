package com.jarvis.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jarvis.app.data.habits.HabitCheckInEntity
import com.jarvis.app.data.habits.HabitConverters
import com.jarvis.app.data.habits.HabitDao
import com.jarvis.app.data.habits.HabitEntity
import com.jarvis.app.data.habits.HabitWeeklyScoreDao
import com.jarvis.app.data.habits.HabitWeeklyScoreEntity
import com.jarvis.app.data.habits.PointTransactionDao
import com.jarvis.app.data.habits.PointTransactionEntity
import com.jarvis.app.data.tasks.SubtaskEntity
import com.jarvis.app.data.tasks.TaskConverters
import com.jarvis.app.data.tasks.TaskDao
import com.jarvis.app.data.tasks.TaskEntity

/**
 * Database Room principale dell'app.
 *
 * version = 6: aggiunta colonna `description` a `habits` (testo Regole).
 * (v5 = engine punti, v4 = refactor a 2 tipi binari.)
 * Ancora `fallbackToDestructiveMigration()` nel DatabaseModule: in fase
 * dev va bene, niente dati reali da preservare. Al prossimo avvio Room
 * azzera il DB e il seeder ripopola.
 */
@Database(
    entities = [
        AppState::class,
        TaskEntity::class,
        SubtaskEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        PointTransactionEntity::class,
        HabitWeeklyScoreEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
@TypeConverters(TaskConverters::class, HabitConverters::class)
abstract class JarvisDatabase : RoomDatabase() {

    abstract fun appStateDao(): AppStateDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun pointTransactionDao(): PointTransactionDao
    abstract fun habitWeeklyScoreDao(): HabitWeeklyScoreDao

    companion object {
        const val DATABASE_NAME = "jarvis.db"
    }
}
