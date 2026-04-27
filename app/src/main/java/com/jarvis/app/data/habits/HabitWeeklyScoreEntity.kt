package com.jarvis.app.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * "Stato vinto/non vinto" registrato per (abitudine, settimana).
 *
 * Serve a un solo scopo: capire se la settimana è GIÀ STATA premiata,
 * così se l'utente edita retroattivamente un check-in possiamo decidere:
 *   - era vinta e ora non più → emetti HABIT_REVOKED.
 *   - non era vinta e ora lo è → emetti HABIT_WON.
 *   - stato invariato → non fare nulla.
 *
 * Senza questa tabella dovremmo cercare nelle transazioni "esiste già una
 * HABIT_WON per (habit, week)?" → query più costosa e più fragile (richiede
 * di filtrare per reason e somma).
 *
 * Indice unico su (habit_id, week_start_epoch_day): una sola riga per
 * settimana per abitudine.
 *
 * Foreign key su HabitEntity con CASCADE: se l'abitudine viene cancellata,
 * tutto il suo storico settimanale sparisce.
 */
@Entity(
    tableName = "habit_weekly_scores",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["habit_id", "week_start_epoch_day"], unique = true),
        Index("week_start_epoch_day"),
    ],
)
data class HabitWeeklyScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "week_start_epoch_day")
    val weekStartEpochDay: Long,

    /** True se la settimana è stata "premiata" (transazione HABIT_WON emessa). */
    @ColumnInfo(name = "is_won")
    val isWon: Boolean,

    /** Quanti punti sono stati assegnati (snapshot al momento della vittoria). */
    @ColumnInfo(name = "points_awarded")
    val pointsAwarded: Int,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
