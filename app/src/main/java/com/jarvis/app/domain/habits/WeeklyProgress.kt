package com.jarvis.app.domain.habits

import com.jarvis.app.data.habits.HabitType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Progresso settimanale di una singola abitudine. Calcolato al volo dai
 * check-in: non lo memorizziamo, così non ci sono dati da invalidare quando
 * cambia qualcosa.
 *
 * - `count` = giorni spuntati nella settimana per questa abitudine.
 * - `target` = soglia (min per POSITIVE_DAILY, max per WEEKLY_CAP).
 * - `isCap` = true se è un'abitudine WEEKLY_CAP (tetto da non superare).
 * - `isWon` = la settimana è "vinta" secondo la regola del tipo.
 * - `isLost` = la settimana è IRRECUPERABILE (solo CAP: cap superato).
 *   Per POSITIVE_DAILY non esiste isLost finché la settimana è in corso:
 *   teoricamente puoi ancora recuperare. Decideremo a fine settimana.
 * - `progressFraction` = 0..1 per la barra UI.
 *   POSITIVE: count/target.
 *   CAP: 1 - count/target (parte piena e si svuota verso lo sforamento).
 */
data class WeeklyProgress(
    val habitId: Long,
    val count: Int,
    val target: Int,
    val isCap: Boolean,
    val isWon: Boolean,
    val isLost: Boolean,
    val progressFraction: Float,
)

/**
 * Ritorna il lunedì della settimana contenente [day]. Se [day] è lunedì,
 * ritorna [day] stesso.
 */
fun LocalDate.weekStart(): LocalDate =
    with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/**
 * Calcola [WeeklyProgress] data un'abitudine e i suoi check-in della
 * settimana (lista libera, qui filtriamo per habitId).
 *
 * Funzione PURA — testabile senza Android.
 */
fun computeWeeklyProgress(
    habit: Habit,
    checkInsInWeek: List<HabitCheckIn>,
): WeeklyProgress {
    // Solo i check-in di questa abitudine, "spuntati" (value >= 1).
    val count = checkInsInWeek.count { it.habitId == habit.id && it.value >= 1 }

    val isCap = habit.type == HabitType.WEEKLY_CAP
    val target = habit.weeklyTarget

    val isWon = if (isCap) count <= target else count >= target
    val isLost = isCap && count > target

    val fraction: Float = when {
        target == 0 -> if (isWon) 1f else 0f
        isCap -> ((target - count).toFloat() / target.toFloat()).coerceIn(0f, 1f)
        else -> (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    }

    return WeeklyProgress(
        habitId = habit.id,
        count = count,
        target = target,
        isCap = isCap,
        isWon = isWon,
        isLost = isLost,
        progressFraction = fraction,
    )
}
