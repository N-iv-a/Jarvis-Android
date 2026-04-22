package com.jarvis.app.data.tasks

import com.jarvis.app.domain.tasks.Subtask
import com.jarvis.app.domain.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade sopra il [TaskDao]: espone API in termini di modelli di dominio
 * ([Task], [Subtask]) invece di entity Room. Il resto dell'app non tocca
 * mai il DAO direttamente.
 *
 * È un Singleton perché è stateless e creare più istanze non avrebbe senso.
 */
@Singleton
class TaskRepository @Inject constructor(
    private val dao: TaskDao,
) {

    /** Flusso reattivo di tutte le task. Emette a ogni modifica del DB. */
    fun observeAll(): Flow<List<Task>> =
        dao.observeAllWithSubtasks().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getById(id: Long): Task? =
        dao.getByIdWithSubtasks(id)?.toDomain()

    // ----------------------- Task CRUD -----------------------

    /**
     * Inserisce una nuova task. Ritorna l'id generato, utile se il chiamante
     * vuole subito collegare sottotask o navigare al detail.
     */
    suspend fun createTask(task: Task): Long =
        dao.insertTask(task.toEntity())

    suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    suspend fun deleteTask(task: Task) {
        // onDelete=CASCADE sulla FK cancellerà automaticamente le sottotask.
        dao.deleteTaskById(task.id)
    }

    // ----------------------- Subtask CRUD -----------------------

    suspend fun createSubtask(subtask: Subtask): Long =
        dao.insertSubtask(subtask.toEntity())

    suspend fun updateSubtask(subtask: Subtask) {
        dao.updateSubtask(subtask.toEntity())
    }

    suspend fun deleteSubtask(subtask: Subtask) {
        dao.deleteSubtask(subtask.toEntity())
    }

    /** Toggle rapido — evita di rileggere/riscrivere l'intera sottotask. */
    suspend fun setSubtaskStatus(id: Long, status: SubtaskStatus) {
        dao.setSubtaskStatus(id, status)
    }
}
