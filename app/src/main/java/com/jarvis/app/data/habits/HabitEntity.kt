package com.jarvis.app.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Riga della tabella `habits`. Una abitudine.
 *
 * Note:
 * - `weeklyTarget` significato dipende dal tipo:
 *   * POSITIVE_DAILY → giorni minimi/settimana da spuntare per vincere.
 *   * WEEKLY_CAP → cap massimo di giorni/settimana oltre cui si perde.
 * - `pointsPerWeek`: quanti punti vale "vincere" la settimana di questa
 *   abitudine. Per ora seedato a 10 per tutti, in futuro tarabile.
 * - `description`: testo libero mostrato nella pagina "Regole". null per
 *   le abitudini il cui nome è auto-esplicativo (es. Sigarette).
 * - `ordine`: ordine stabile in lista (timeline). Valori arbitrari.
 * - `archived`: per "mettere in pausa" senza perdere lo storico.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val type: HabitType,

    @ColumnInfo(name = "weekly_target")
    val weeklyTarget: Int,

    @ColumnInfo(name = "points_per_week", defaultValue = "10")
    val pointsPerWeek: Int = 10,

    @ColumnInfo(name = "description")
    val description: String? = null,

    val ordine: Int = 0,

    val archived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
