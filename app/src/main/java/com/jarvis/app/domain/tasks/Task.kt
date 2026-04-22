package com.jarvis.app.domain.tasks

import com.jarvis.app.data.tasks.Importanza
import com.jarvis.app.data.tasks.Stato
import com.jarvis.app.data.tasks.SubtaskStatus
import java.time.LocalDate

/**
 * Modello di dominio di una Task — la rappresentazione che il resto dell'app
 * (ViewModel, UI) usa, separata dall'entity Room.
 *
 * Perché duplicare? Alcuni buoni motivi:
 * - L'entity usa `deadlineEpochDay: Long?`; qui usiamo `LocalDate?` che è
 *   molto più comodo da maneggiare nell'UI e nelle decisioni di business
 *   (es. calcolo del pin).
 * - `isPinned` è calcolato on-the-fly: vive solo nel dominio, non nel DB.
 * - Se un giorno cambiamo persistenza (es. DataStore, server), l'UI non
 *   se ne accorge.
 */
data class Task(
    val id: Long,
    val titolo: String,
    val descrizione: String?,
    val importanza: Importanza,
    val stato: Stato,
    val deadline: LocalDate?,
    val pinDaysBefore: Int,
    val createdAt: Long,
    val completedAt: Long?,
    val subtasks: List<Subtask>,
) {
    /**
     * Una task è "pinned" se ha una scadenza, non è completata, e manca
     * meno di [pinDaysBefore] giorni alla scadenza (o è già passata).
     * Calcolato al volo — mai persistito.
     */
    fun isPinned(today: LocalDate = LocalDate.now()): Boolean {
        if (stato == Stato.COMPLETATO) return false
        val d = deadline ?: return false
        val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, d)
        return daysLeft <= pinDaysBefore
    }
}

data class Subtask(
    val id: Long,
    val taskId: Long,
    val titolo: String,
    val status: SubtaskStatus,
    val ordine: Int,
)
