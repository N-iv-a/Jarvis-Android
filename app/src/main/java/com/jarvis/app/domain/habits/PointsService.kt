package com.jarvis.app.domain.habits

import com.jarvis.app.data.habits.HabitWeeklyScoreDao
import com.jarvis.app.data.habits.HabitWeeklyScoreEntity
import com.jarvis.app.data.habits.PointReason
import com.jarvis.app.data.habits.PointTransactionDao
import com.jarvis.app.data.habits.PointTransactionEntity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servizio di dominio: regola il ledger dei punti.
 *
 * Idea centrale: ad OGNI cambiamento di check-in (utente che spunta o
 * toglie spunta), ricalcoliamo lo stato vinto/non-vinto della SETTIMANA
 * coinvolta e SINCRONIZZIAMO il ledger:
 *
 *   stato precedente | stato attuale | azione
 *   ─────────────────┼───────────────┼────────────────────────
 *   non vinta        | vinta         | +pointsPerWeek (HABIT_WON)
 *   vinta            | non vinta     | -pointsPerWeek (HABIT_REVOKED)
 *   vinta            | vinta         | nulla
 *   non vinta        | non vinta     | nulla
 *
 * Lo stato precedente vive in `habit_weekly_scores`. Lo stato attuale lo
 * calcoliamo via [computeWeeklyProgress] sui check-in correnti della
 * settimana, che il chiamante ci passa.
 *
 * Tutto qui — non c'è altra logica. La complessità sta tutta nel "quando
 * lo chiamo": vedi [HabitsRepositoryFacade.setCheckInAndAwardPoints] o
 * equivalente nel ViewModel.
 *
 * NOTA SULLA TRANSAZIONALITÀ: idealmente le scritture (insert transaction +
 * upsert score) dovrebbero stare in una @Transaction. Per ora restano due
 * chiamate separate; il rischio è minimo (in caso di crash tra le due, il
 * ledger sarebbe leggermente disallineato dallo stato score → si auto-corregge
 * al prossimo edit della stessa settimana). Migliorabile in futuro.
 */
@Singleton
class PointsService @Inject constructor(
    private val scoreDao: HabitWeeklyScoreDao,
    private val ledgerDao: PointTransactionDao,
) {

    /**
     * Riconcilia il ledger per (abitudine, settimana) date le info attuali.
     *
     * Chiamato dopo OGNI setCheckIn. Non importa quanto spesso: se lo stato
     * non è cambiato non scrive nulla.
     *
     * @param habit l'abitudine coinvolta (serve type + weeklyTarget + pointsPerWeek).
     * @param weekStart lunedì della settimana coinvolta dall'edit.
     * @param checkInsInWeek check-in attuali della settimana (filtrabili o no
     *        per habitId — la funzione li filtra internamente).
     */
    suspend fun reconcileWeek(
        habit: Habit,
        weekStart: LocalDate,
        checkInsInWeek: List<HabitCheckIn>,
    ) {
        val weekEpoch = weekStart.toEpochDay()
        val progress = computeWeeklyProgress(habit, checkInsInWeek)
        val nowWon = progress.isWon

        val previous = scoreDao.getFor(habit.id, weekEpoch)
        val previouslyWon = previous?.isWon == true

        // Nessun cambio di stato → nessuna scrittura. Caso più frequente.
        if (previouslyWon == nowWon && previous != null) return

        when {
            !previouslyWon && nowWon -> {
                // Vittoria nuova → +punti.
                ledgerDao.insert(
                    PointTransactionEntity(
                        habitId = habit.id,
                        weekStartEpochDay = weekEpoch,
                        delta = habit.pointsPerWeek,
                        reason = PointReason.HABIT_WON,
                        note = habit.title,
                    ),
                )
                upsertScore(previous, habit.id, weekEpoch, isWon = true, points = habit.pointsPerWeek)
            }
            previouslyWon && !nowWon -> {
                // Vittoria revocata (edit retroattivo) → storno.
                val toRevoke = previous?.pointsAwarded ?: habit.pointsPerWeek
                ledgerDao.insert(
                    PointTransactionEntity(
                        habitId = habit.id,
                        weekStartEpochDay = weekEpoch,
                        delta = -toRevoke,
                        reason = PointReason.HABIT_REVOKED,
                        note = habit.title,
                    ),
                )
                upsertScore(previous, habit.id, weekEpoch, isWon = false, points = 0)
            }
            else -> {
                // Stato non vinto e nessuna riga score esistente → creiamo
                // la riga "non vinta" giusto per registrare che l'abbiamo
                // già valutata (evita ricalcoli inutili).
                if (previous == null) {
                    upsertScore(null, habit.id, weekEpoch, isWon = false, points = 0)
                }
            }
        }
    }

    /**
     * Crea o aggiorna la riga di score. Centralizzato per non duplicare il
     * controllo "esiste? insert : update".
     */
    private suspend fun upsertScore(
        previous: HabitWeeklyScoreEntity?,
        habitId: Long,
        weekEpoch: Long,
        isWon: Boolean,
        points: Int,
    ) {
        if (previous == null) {
            scoreDao.insert(
                HabitWeeklyScoreEntity(
                    habitId = habitId,
                    weekStartEpochDay = weekEpoch,
                    isWon = isWon,
                    pointsAwarded = points,
                ),
            )
        } else {
            scoreDao.update(
                previous.copy(
                    isWon = isWon,
                    pointsAwarded = points,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
