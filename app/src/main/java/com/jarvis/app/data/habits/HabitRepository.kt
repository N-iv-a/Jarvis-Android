package com.jarvis.app.data.habits

import com.jarvis.app.domain.habits.Habit
import com.jarvis.app.domain.habits.HabitCheckIn
import com.jarvis.app.domain.habits.PointsService
import com.jarvis.app.domain.habits.weekStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade sopra [HabitDao] e [PointTransactionDao]. Espone modelli di dominio.
 * È l'unica porta di ingresso per ViewModel e seeder.
 *
 * Side-effect IMPORTANTE: ogni `setCheckIn` triggera [PointsService.reconcileWeek]
 * sulla settimana coinvolta. In questo modo il saldo punti rimane sempre
 * coerente con i check-in, senza che la UI debba pensarci.
 */
@Singleton
class HabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val pointsService: PointsService,
    private val pointTransactionDao: PointTransactionDao,
) {

    fun observeActiveHabits(): Flow<List<Habit>> =
        dao.observeActiveHabits().map { list -> list.map { it.toDomain() } }

    fun observeCheckInsBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<HabitCheckIn>> =
        dao.observeCheckInsBetween(fromEpochDay, toEpochDay).map { list ->
            list.map { it.toDomain() }
        }

    /** Saldo punti corrente, reattivo (per la TopBar). */
    fun observePointsBalance(): Flow<Int> = pointTransactionDao.observeBalance()

    suspend fun getCheckIn(habitId: Long, dayEpoch: Long): HabitCheckIn? =
        dao.getCheckIn(habitId, dayEpoch)?.toDomain()

    /**
     * Upsert di un check-in. Se [value] è 0 cancella la riga (assenza =
     * "non spuntato"). Dopo la scrittura, ricalcola lo stato vinto/non
     * della settimana e aggiorna i punti.
     *
     * @param day la data del check-in. Da qui ricaviamo il lunedì della
     *   settimana per la riconciliazione.
     */
    suspend fun setCheckIn(habitId: Long, day: LocalDate, value: Int) {
        val dayEpoch = day.toEpochDay()
        if (value <= 0) {
            dao.deleteCheckIn(habitId, dayEpoch)
        } else {
            dao.upsertCheckIn(
                HabitCheckInEntity(
                    habitId = habitId,
                    dateEpochDay = dayEpoch,
                    value = value,
                ),
            )
        }

        // Riconciliazione punti per la settimana del giorno toccato.
        // Snapshot one-shot: prendiamo il primo emit del Flow e proseguiamo.
        val habit = findHabit(habitId) ?: return
        val weekStart = day.weekStart()
        val weekEnd = weekStart.plusDays(6)
        val weekCheckIns = dao.observeCheckInsBetween(
            weekStart.toEpochDay(),
            weekEnd.toEpochDay(),
        ).first().map { it.toDomain() }
        pointsService.reconcileWeek(habit, weekStart, weekCheckIns)
    }

    /** Cerca una singola abitudine sullo snapshot corrente. */
    private suspend fun findHabit(habitId: Long): Habit? =
        dao.observeActiveHabits().first().firstOrNull { it.id == habitId }?.toDomain()

    suspend fun countHabits(): Int = dao.countAll()

    suspend fun insertHabit(entity: HabitEntity): Long = dao.insertHabit(entity)
}
