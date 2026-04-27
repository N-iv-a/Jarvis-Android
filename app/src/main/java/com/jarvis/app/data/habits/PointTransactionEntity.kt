package com.jarvis.app.data.habits

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una riga del libro mastro dei punti.
 *
 * Filosofia "event sourcing leggera": il SALDO PUNTI è la SOMMA di tutte
 * le transazioni. Non memorizziamo un saldo aggregato — è sempre derivato
 * via SUM(delta). Vantaggi:
 * - Niente "saldo che si disallinea": il valore è sempre la verità.
 * - Storno automatico: per togliere punti bastera inserire una nuova riga
 *   con delta negativo.
 * - Audit trail completo: in ogni momento posso mostrare "perché ho 47 punti?"
 *   scorrendo le transazioni.
 *
 * Tipi di transazione (`reason`):
 * - HABIT_WON: settimana vinta → +pointsPerWeek dell'abitudine.
 * - HABIT_REVOKED: storno di una HABIT_WON precedente, perché un edit
 *   retroattivo ha invalidato la vittoria → -pointsPerWeek.
 * - REWARD_PURCHASE: acquisto premio → -costo.
 * - REWARD_REFUND: storno acquisto (non implementato in Phase 2B, hook futuro).
 *
 * `habitId` e `weekStartEpochDay` sono nullable perché REWARD_PURCHASE non
 * è legato a un'abitudine. Per HABIT_WON/REVOKED sono entrambi popolati e
 * formano la chiave logica della "vittoria settimanale".
 */
@Entity(
    tableName = "point_transactions",
    indices = [
        // Per "saldo per data" o "filtra per abitudine in storia".
        Index("habit_id"),
        Index("week_start_epoch_day"),
        Index("reason"),
    ],
)
data class PointTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long? = null,

    @ColumnInfo(name = "week_start_epoch_day")
    val weekStartEpochDay: Long? = null,

    /** Punti aggiunti (>0) o sottratti (<0). */
    val delta: Int,

    /** Ragione machine-readable. Vedi [PointReason]. */
    val reason: String,

    /** Note opzionali leggibili (es. nome del premio acquistato). */
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Costanti per il campo [PointTransactionEntity.reason]. Stringhe e non enum
 * per non avere casino con i TypeConverter quando aggiungeremo nuovi motivi.
 */
object PointReason {
    const val HABIT_WON = "habit_won"
    const val HABIT_REVOKED = "habit_revoked"
    const val REWARD_PURCHASE = "reward_purchase"
    const val REWARD_REFUND = "reward_refund"
}
