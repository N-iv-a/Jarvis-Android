package com.jarvis.app.data.habits

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pre-popola il DB con le 7 abitudini iniziali al PRIMO avvio.
 *
 * Strategia "semina idempotente": se il DB ha già abitudini (anche una sola)
 * non fa nulla. Così se l'utente ne aggiunge/rimuove in futuro non
 * riapparirà nulla dal seeder.
 *
 * Tutti i punti sono inizialmente uguali (10/settimana). Dopo qualche
 * settimana di uso reale potremo tarare in base alla difficoltà
 * percepita di ciascuna abitudine.
 *
 * Le `description` finiscono nella pagina "Regole". Per le abitudini
 * auto-esplicative (es. Sigarette) lasciamo null → in Regole compaiono
 * solo titolo + target.
 *
 * Chiamato da JarvisApplication.onCreate (con coroutine IO).
 */
@Singleton
class HabitSeeder @Inject constructor(
    private val repository: HabitRepository,
) {

    suspend fun seedIfEmpty() {
        if (repository.countHabits() > 0) return

        INITIAL_HABITS.forEachIndexed { index, seed ->
            repository.insertHabit(
                HabitEntity(
                    title = seed.title,
                    type = seed.type,
                    weeklyTarget = seed.weeklyTarget,
                    pointsPerWeek = DEFAULT_POINTS_PER_WEEK,
                    description = seed.description,
                    ordine = index,
                ),
            )
        }
    }

    private data class Seed(
        val title: String,
        val type: HabitType,
        val weeklyTarget: Int,
        val description: String?,
    )

    companion object {
        private const val DEFAULT_POINTS_PER_WEEK = 10

        // Le 7 abitudini di partenza. NB: "Sigarette" è POSITIVE_DAILY:
        // ogni sera mi chiedo "ho rispettato il limite?" e spunto.
        // Lo stesso vale per Lettura (≥30 min → spunta) e Allenamento.
        private val INITIAL_HABITS = listOf(
            Seed(
                title = "Lettura",
                type = HabitType.POSITIVE_DAILY,
                weeklyTarget = 5,
                description = "Almeno 30 minuti di lettura al giorno.",
            ),
            Seed(
                title = "Sigarette",
                type = HabitType.POSITIVE_DAILY,
                weeklyTarget = 5,
                description = null, // il nome basta
            ),
            Seed(
                title = "Dieta",
                type = HabitType.POSITIVE_DAILY,
                weeklyTarget = 5,
                description = "Rispettare il piano alimentare per la giornata.",
            ),
            Seed(
                title = "No red zone",
                type = HabitType.POSITIVE_DAILY,
                weeklyTarget = 6,
                description = "Niente comportamenti compulsivi o sgarri pesanti.",
            ),
            Seed(
                title = "Allenamento",
                type = HabitType.POSITIVE_DAILY,
                weeklyTarget = 2,
                description = "Una sessione di allenamento svolta.",
            ),
            Seed(
                title = "Bridget",
                type = HabitType.WEEKLY_CAP,
                weeklyTarget = 4,
                description = "Spunta i giorni in cui vi siete visti. Cap settimanale.",
            ),
            Seed(
                title = "Alcolici",
                type = HabitType.WEEKLY_CAP,
                weeklyTarget = 2,
                description = "Spunta i giorni in cui hai bevuto. Cap settimanale.",
            ),
        )
    }
}
