package com.jarvis.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.app.data.habits.HabitRepository
import com.jarvis.app.domain.habits.Habit
import com.jarvis.app.domain.habits.HabitCheckIn
import com.jarvis.app.domain.habits.WeeklyProgress
import com.jarvis.app.domain.habits.computeWeeklyProgress
import com.jarvis.app.domain.habits.weekStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Numero di settimane mostrate nella timeline: corrente + scorsa.
 *
 * I check-in più vecchi restano nel DB (utili per future statistiche e
 * grafici), ma non vengono renderizzati: tenere lo scroll corto evita
 * inutile rumore visivo. Se servirà uno "storico completo" lo metteremo
 * dietro un click dalla pagina Statistiche.
 */
private const val WEEKS_VISIBLE = 2

/**
 * Una settimana nella timeline.
 * - `start` = lunedì.
 * - `cellsByHabit` = per ogni habitId, il mini-stato del giorno
 *   (Map<dayOffset 0..6, true/false>). true = spuntato.
 * - `progressByHabit` = per ogni habitId, il progresso settimanale calcolato.
 *
 * Nessun campo `editable`: con WEEKS_VISIBLE=2 tutto ciò che è visibile è
 * editabile (la finestra di editabilità coincide con quella visiva).
 */
data class WeekRow(
    val start: LocalDate,
    val cellsByHabit: Map<Long, Map<Int, Boolean>>,
    val progressByHabit: Map<Long, WeeklyProgress>,
)

data class HabitsUiState(
    val habits: List<Habit> = emptyList(),
    val weeks: List<WeekRow> = emptyList(),
    val pointsBalance: Int = 0,
    val isLoading: Boolean = true,
)

/**
 * ViewModel della schermata "Abitudini" — la timeline.
 *
 * Carichiamo in un colpo TUTTI i check-in delle ultime [WEEKS_VISIBLE]
 * settimane. Sono pochi dati (max ~7 abitudini × 7 giorni × 2 sett = 98
 * record), una sola query è la scelta più semplice.
 *
 * Niente più bottom sheet: l'utente tocca direttamente il pallino del
 * giorno × abitudine per fare il toggle. Più rapido, un solo gesto.
 */
@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = run {
        val today = LocalDate.now()
        val currentWeekStart = today.weekStart()
        val oldestWeekStart = currentWeekStart.minusWeeks((WEEKS_VISIBLE - 1).toLong())
        val rangeStartEpoch = oldestWeekStart.toEpochDay()
        val rangeEndEpoch = currentWeekStart.plusDays(6).toEpochDay()

        combine(
            repository.observeActiveHabits(),
            repository.observeCheckInsBetween(rangeStartEpoch, rangeEndEpoch),
            repository.observePointsBalance(),
        ) { habits, checkIns, balance ->
            HabitsUiState(
                habits = habits,
                weeks = buildWeeks(habits, checkIns, currentWeekStart),
                pointsBalance = balance,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HabitsUiState(),
        )
    }

    /**
     * Toggle di un singolo (abitudine, giorno).
     *
     * - Niente toggle sul futuro (date > oggi): la UI già disabilita il
     *   click ma metto un guard di sicurezza qui per non fare scrivere
     *   in DB stati assurdi se mai arrivasse un click.
     * - Lookup dello stato attuale dal flusso del DB (non dalla UI), così
     *   anche due tap rapidissimi convergono al risultato giusto.
     * - Repository fa il resto: cancella la riga se newValue=0 e attiva
     *   la riconciliazione punti automatica.
     */
    fun toggleDot(habit: Habit, date: LocalDate) {
        if (date > LocalDate.now()) return

        viewModelScope.launch {
            val current = repository.getCheckIn(habit.id, date.toEpochDay())
            val newValue = if ((current?.value ?: 0) >= 1) 0 else 1
            repository.setCheckIn(habit.id, date, newValue)
        }
    }

    // ---------------- Helpers ----------------

    private fun buildWeeks(
        habits: List<Habit>,
        checkIns: List<HabitCheckIn>,
        currentWeekStart: LocalDate,
    ): List<WeekRow> {
        return (0 until WEEKS_VISIBLE).map { offset ->
            val start = currentWeekStart.minusWeeks(offset.toLong())
            val startEpoch = start.toEpochDay()
            val endEpoch = startEpoch + 6

            val checkInsThisWeek = checkIns.filter {
                it.dayEpoch in startEpoch..endEpoch
            }

            val cellsByHabit: Map<Long, Map<Int, Boolean>> = habits.associate { habit ->
                val perDay = (0..6).associateWith { dayOffset ->
                    val targetEpoch = startEpoch + dayOffset
                    checkInsThisWeek.any {
                        it.habitId == habit.id && it.dayEpoch == targetEpoch && it.value >= 1
                    }
                }
                habit.id to perDay
            }

            val progressByHabit: Map<Long, WeeklyProgress> = habits.associate { habit ->
                habit.id to computeWeeklyProgress(habit, checkInsThisWeek)
            }

            WeekRow(
                start = start,
                cellsByHabit = cellsByHabit,
                progressByHabit = progressByHabit,
            )
        }
    }
}
