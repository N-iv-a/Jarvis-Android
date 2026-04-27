package com.jarvis.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.jarvis.app.data.habits.HabitDao
import com.jarvis.app.data.habits.HabitWeeklyScoreDao
import com.jarvis.app.data.habits.PointTransactionDao
import com.jarvis.app.data.security.DatabasePassphraseManager
import com.jarvis.app.data.tasks.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

/**
 * Modulo Hilt che fornisce il database Room cifrato con SQLCipher.
 *
 * Flusso:
 * 1. Carichiamo le librerie native di SQLCipher (.so)
 * 2. Recuperiamo (o generiamo) la passphrase dal Keystore
 * 3. Creiamo una SupportOpenHelperFactory di SQLCipher con quella passphrase
 * 4. Room usa la factory al posto di quella di default → DB cifrato trasparentemente
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager,
    ): JarvisDatabase {
        // Carica le librerie native di SQLCipher.
        // DEVE essere chiamato prima di qualsiasi operazione sul DB.
        System.loadLibrary("sqlcipher")

        val passphrase = passphraseManager.getOrCreatePassphrase()

        // Factory che intercetta l'apertura del DB e applica cifratura AES-256.
        // La passphrase NON è copiata internamente: SQLCipher la azzera dalla memoria
        // dopo averla derivata in chiave (PBKDF2 → 256000 round di default).
        val factory: SupportSQLiteOpenHelper.Factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            JarvisDatabase::class.java,
            JarvisDatabase.DATABASE_NAME,
        )
            .openHelperFactory(factory)
            // In fase di sviluppo: se lo schema cambia, azzera e ricrea il DB.
            // DA RIVEDERE quando avremo dati reali — allora useremo Migration.
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Espone il TaskDao al grafo DI. Così i repository possono iniettarlo
     * direttamente senza dover prima chiedere l'intero JarvisDatabase.
     */
    @Provides
    fun provideTaskDao(database: JarvisDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideHabitDao(database: JarvisDatabase): HabitDao = database.habitDao()

    @Provides
    fun providePointTransactionDao(database: JarvisDatabase): PointTransactionDao =
        database.pointTransactionDao()

    @Provides
    fun provideHabitWeeklyScoreDao(database: JarvisDatabase): HabitWeeklyScoreDao =
        database.habitWeeklyScoreDao()
}
