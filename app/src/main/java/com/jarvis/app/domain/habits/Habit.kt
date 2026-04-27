package com.jarvis.app.domain.habits

import com.jarvis.app.data.habits.HabitType

/**
 * Modello di dominio di un'abitudine. Separato dall'entity Room così la
 * UI/logica non sa nulla di Room (più semplice testare e cambiare backend).
 */
data class Habit(
    val id: Long,
    val title: String,
    val type: HabitType,
    val weeklyTarget: Int,
    val pointsPerWeek: Int,
    /** Descrizione mostrata nella pagina "Regole". null = nessuna spiegazione. */
    val description: String?,
    val ordine: Int,
    val archived: Boolean,
)

/**
 * Check-in di un'abitudine in un giorno specifico.
 *
 * `value` è semanticamente binario (1 = spuntato) ma lo manteniamo Int per
 * future estensioni ed evitare migration solo per cambio tipo colonna.
 */
data class HabitCheckIn(
    val habitId: Long,
    val dayEpoch: Long,
    val value: Int,
)
