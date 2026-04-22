package com.jarvis.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jarvis.app.data.tasks.SubtaskEntity
import com.jarvis.app.data.tasks.TaskConverters
import com.jarvis.app.data.tasks.TaskDao
import com.jarvis.app.data.tasks.TaskEntity

/**
 * Database Room principale dell'app.
 *
 * Contiene:
 * - AppState (singleton-row con metadati globali)
 * - TaskEntity / SubtaskEntity (feature tasks)
 *
 * version = 2: bumpata da 1 a 2 aggiungendo tabelle tasks + subtasks.
 * Non scriviamo una Migration vera perché DatabaseModule usa
 * `fallbackToDestructiveMigration()` — in questa fase di sviluppo è
 * accettabile perdere i dati a ogni cambio schema. Quando l'app sarà
 * in uso reale scriveremo Migrations vere.
 *
 * exportSchema = false: stesso motivo. Lo attiveremo quando congeleremo
 * lo schema e servirà per generare Migrations corrette.
 */
@Database(
    entities = [
        AppState::class,
        TaskEntity::class,
        SubtaskEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(TaskConverters::class)
abstract class JarvisDatabase : RoomDatabase() {

    abstract fun appStateDao(): AppStateDao

    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "jarvis.db"
    }
}
