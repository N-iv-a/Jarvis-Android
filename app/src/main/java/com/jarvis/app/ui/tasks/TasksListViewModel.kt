package com.jarvis.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.app.data.tasks.Stato
import com.jarvis.app.data.tasks.SubtaskStatus
import com.jarvis.app.data.tasks.TaskRepository
import com.jarvis.app.domain.tasks.Subtask
import com.jarvis.app.domain.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Stato della schermata lista.
 *
 * Le task sono già raggruppate per stato: la UI si limita a renderizzare.
 * Così nessuna logica di presentazione finisce nei Composable.
 */
data class TasksListUiState(
    val aperte: List<Task> = emptyList(),
    val inAttesa: List<Task> = emptyList(),
    val completate: List<Task> = emptyList(),
    val showCompleted: Boolean = false,
    val isLoading: Boolean = true,
)

/**
 * ViewModel della schermata principale della feature Tasks.
 *
 * Pattern:
 * - Un [StateFlow<TasksListUiState>] unico con tutto lo stato UI.
 * - La sorgente è il flow del repository + il flag showCompleted locale.
 * - Le azioni utente sono metodi che lanciano coroutine su viewModelScope.
 */
@HiltViewModel
class TasksListViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {

    private val showCompleted = MutableStateFlow(false)

    val uiState: StateFlow<TasksListUiState> =
        combine(
            repository.observeAll(),
            showCompleted,
        ) { tasks, show ->
            TasksListUiState(
                aperte = tasks.filter { it.stato == Stato.APERTO },
                inAttesa = tasks.filter { it.stato == Stato.IN_ATTESA },
                completate = tasks.filter { it.stato == Stato.COMPLETATO },
                showCompleted = show,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            // WhileSubscribed(5000): il flow upstream resta attivo per 5 secondi
            // dopo che l'ultima UI si disiscrive → sopravvive a rotazioni dello
            // schermo senza ripartire da zero.
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TasksListUiState(),
        )

    fun toggleShowCompleted() {
        showCompleted.value = !showCompleted.value
    }

    /**
     * Cambia lo stato di una task. Se passa a COMPLETATO imposta anche
     * `completedAt`; se torna indietro lo azzera.
     */
    fun setTaskStato(task: Task, newStato: Stato) {
        viewModelScope.launch {
            val updated = task.copy(
                stato = newStato,
                completedAt = if (newStato == Stato.COMPLETATO) {
                    System.currentTimeMillis()
                } else {
                    null
                },
            )
            repository.updateTask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            val newStatus = if (subtask.status == SubtaskStatus.DONE) {
                SubtaskStatus.PENDING
            } else {
                SubtaskStatus.DONE
            }
            repository.setSubtaskStatus(subtask.id, newStatus)
        }
    }

    fun addSubtask(taskId: Long, titolo: String) {
        val clean = titolo.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            // Ordine: mettiamo la nuova sottotask alla fine.
            val existing = repository.getById(taskId)?.subtasks.orEmpty()
            val nextOrder = (existing.maxOfOrNull { it.ordine } ?: -1) + 1
            repository.createSubtask(
                Subtask(
                    id = 0,
                    taskId = taskId,
                    titolo = clean,
                    status = SubtaskStatus.PENDING,
                    ordine = nextOrder,
                ),
            )
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch { repository.deleteSubtask(subtask) }
    }
}
