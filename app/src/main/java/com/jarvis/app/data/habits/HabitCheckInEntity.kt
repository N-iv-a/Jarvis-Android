package com.jarvis.app.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Riga della tabella `habit_checkins`. Un check-in di una abitudine in un
 * dato giorno.
 *
 * Unicità: esiste UN solo check-in per (habitId, dateEpochDay). Se l'utente
 * modifica il valore per lo stesso giorno, sovrascriviamo. Garantito dal
 * vincolo di unicità composito.
 *
 * `value` interpretazione: post-refactor è SEMPRE binario.
 * 1 = giorno spuntato, 0 = "togli spunta".
 * Il repository normalizza: se value <= 0, la riga viene CANCELLATA invece
 * che salvata a 0 → meno rumore, "assenza di riga = non spuntato".
 * Manteniamo Int (e non Boolean) per estensioni future senza migration.
 *
 * `dateEpochDay`: giorno calendar-based (LocalDate.toEpochDay). Salvare Long
 * invece di timestamp millis evita ogni ambiguità di fuso orario.
 */
@Entity(
    tableName = "habit_checkins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // Indice unico: un solo check-in per (habit, giorno).
        Index(value = ["habit_id", "date_epoch_day"], unique = true),
        Index("date_epoch_day"),
    ],
)
data class HabitCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "date_epoch_day")
    val dateEpochDay: Long,

    val value: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
