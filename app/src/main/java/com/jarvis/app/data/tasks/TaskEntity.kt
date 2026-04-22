package com.jarvis.app.data.tasks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Riga della tabella `tasks`.
 *
 * NOTE IMPLEMENTATIVE:
 * - Room salva gli enum come String (usando TypeConverter — vedi [TaskConverters]).
 * - Le date sono salvate come Long (epoch millis) per `createdAt`/`completedAt`
 *   e come Long = epoch day (giorni dal 1970-01-01) per la `deadline`, perché
 *   la deadline è SOLO una data (senza ora), e confonderla con timestamp
 *   millisecondo porta a bug di fuso orario.
 * - `pinDaysBefore` controlla quando mostrare il pin visivo: se
 *   `(deadline - oggi) <= pinDaysBefore` → pin acceso. Configurabile per-task
 *   perché alcune scadenze servono 7gg prima, altre il giorno stesso.
 * - NON memorizziamo un campo `pinned` separato: è SEMPRE derivato a runtime
 *   dalla combinazione deadline + pinDaysBefore + data corrente. Memorizzarlo
 *   richiederebbe un job che lo aggiorna ogni mezzanotte → complessità inutile.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val titolo: String,
    val descrizione: String? = null,

    val importanza: Importanza = Importanza.MEDIA,
    val stato: Stato = Stato.APERTO,

    /** Scadenza come epoch day (giorni dal 1970-01-01). null = nessuna. */
    @ColumnInfo(name = "deadline_epoch_day")
    val deadlineEpochDay: Long? = null,

    /** Quanti giorni prima della deadline mostrare il pin. Default 3. */
    @ColumnInfo(name = "pin_days_before")
    val pinDaysBefore: Int = 3,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
)
