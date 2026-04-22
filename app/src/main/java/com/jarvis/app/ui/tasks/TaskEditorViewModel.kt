package com.jarvis.app.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.app.data.tasks.Importanza
import com.jarvis.app.data.tasks.Stato
import com.jarvis.app.data.tasks.TaskRepository
import com.jarvis.app.domain.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Stato locale dell'editor. Tutti i campi sono grezzi (non validati):
 * la validazione avviene solo al salvataggio.
 */
data class TaskEditorUiState(
    val id: Long? = null,
    val titolo: String = "",
    val descrizione: String = "",
    val importanza: Importanza = Importanza.MEDIA,
    val stato: Stato = Stato.APERTO,
    val deadline: LocalDate? = null,
    val pinDaysBefore: Int = 3,
    val canSave: Boolean = false,
    val isLoading: Boolean = false,
    val saved: Boolean = false,
)

/**
 * ViewModel condiviso per creazione E modifica di una task.
 *
 * La navigazione passa `taskId`:
 * - null → crea una task nuova.
 * - valore → modifica una task esistente (la carichiamo da DB).
 *
 * Salvataggio atomico: costruisce un [Task] dal proprio state e lo passa
 * al repository. Al termine emette `saved=true` come segnale "puoi navigare indietro".
 */
@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<Long?>(ARG_TASK_ID)
        ?.takeIf { it > 0L }

    private val _state = MutableStateFlow(TaskEditorUiState(isLoading = taskId != null))
    val state: StateFlow<TaskEditorUiState> = _state.asStateFlow()

    init {
        if (taskId != null) loadExisting(taskId)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            val existing = repository.getById(id) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            _state.value = TaskEditorUiState(
                id = existing.id,
                titolo = existing.titolo,
                descrizione = existing.descrizione.orEmpty(),
                importanza = existing.importanza,
                stato = existing.stato,
                deadline = existing.deadline,
                pinDaysBefore = existing.pinDaysBefore,
                canSave = existing.titolo.isNotBlank(),
                isLoading = false,
                saved = false,
            )
        }
    }

    fun onTitoloChange(value: String) = _state.update {
        it.copy(titolo = value, canSave = value.isNotBlank())
    }

    fun onDescrizioneChange(value: String) = _state.update { it.copy(descrizione = value) }

    fun onImportanzaChange(value: Importanza) = _state.update { it.copy(importanza = value) }

    fun onStatoChange(value: Stato) = _state.update { it.copy(stato = value) }

    fun onDeadlineChange(value: LocalDate?) = _state.update { it.copy(deadline = value) }

    fun onPinDaysBeforeChange(value: Int) = _state.update {
        it.copy(pinDaysBefore = value.coerceAtLeast(0))
    }

    /**
     * Salva la task. Se il titolo è vuoto, esce silenziosamente
     * (il pulsante Salva dovrebbe già essere disabilitato via canSave).
     */
    fun save() {
        val s = _state.value
        if (s.titolo.isBlank()) return

        viewModelScope.launch {
            if (s.id == null) {
                // Nuova task
                repository.createTask(
                    Task(
                        id = 0,
                        titolo = s.titolo.trim(),
                        descrizione = s.descrizione.trim().ifBlank { null },
                        importanza = s.importanza,
                        stato = s.stato,
                        deadline = s.deadline,
                        pinDaysBefore = s.pinDaysBefore,
                        createdAt = System.currentTimeMillis(),
                        completedAt = if (s.stato == Stato.COMPLETATO) {
                            System.currentTimeMillis()
                        } else {
                            null
                        },
                        subtasks = emptyList(),
                    ),
                )
            } else {
                // Update: carichiamo prima l'entity corrente per preservare
                // createdAt e completedAt (quando pertinenti).
                val existing = repository.getById(s.id) ?: return@launch
                val newCompletedAt = when {
                    s.stato == Stato.COMPLETATO && existing.stato != Stato.COMPLETATO ->
                        System.currentTimeMillis()
                    s.stato != Stato.COMPLETATO -> null
                    else -> existing.completedAt
                }
                repository.updateTask(
                    existing.copy(
                        titolo = s.titolo.trim(),
                        descrizione = s.descrizione.trim().ifBlank { null },
                        importanza = s.importanza,
                        stato = s.stato,
                        deadline = s.deadline,
                        pinDaysBefore = s.pinDaysBefore,
                        completedAt = newCompletedAt,
                    ),
                )
            }
            _state.update { it.copy(saved = true) }
        }
    }

    companion object {
        /** Chiave dell'argomento di navigazione. Usata sia dal NavHost che qui. */
        const val ARG_TASK_ID = "taskId"
    }
}
