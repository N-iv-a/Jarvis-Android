package com.jarvis.app.data.tasks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO unificato per tasks + sottotask.
 *
 * Regole di progetto:
 * - Le query che tornano liste usano [Flow]: l'UI si aggiorna in automatico
 *   ad ogni modifica del DB, senza dover "rileggere" manualmente.
 * - Le operazioni di modifica sono `suspend`: vanno chiamate da una coroutine
 *   → Room le esegue fuori dal main thread automaticamente.
 * - @Transaction sulle query con @Relation per garantire consistenza
 *   (task + sottotask lette "nello stesso istante").
 */
@Dao
interface TaskDao {

    // ------------------------- Query lettura -------------------------

    /**
     * Tutte le task con le relative sottotask. Ordinamento a due livelli:
     * - importanza ASC (Alta < Media < Bassa perché salvate come stringa,
     *   l'ordine alfabetico va: ALTA, BASSA, MEDIA → NON funziona).
     *   Per questo motivo ordiniamo con CASE esplicito.
     * - poi per deadline crescente (NULL in fondo).
     * - poi per createdAt (più vecchie prima, a parità di tutto).
     *
     * NOTA: non filtriamo per stato qui — il raggruppamento per stato
     * avviene lato ViewModel per semplificare le query.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY
          CASE importanza
            WHEN 'ALTA' THEN 0
            WHEN 'MEDIA' THEN 1
            WHEN 'BASSA' THEN 2
          END,
          CASE WHEN deadline_epoch_day IS NULL THEN 1 ELSE 0 END,
          deadline_epoch_day ASC,
          created_at ASC
        """
    )
    fun observeAllWithSubtasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getByIdWithSubtasks(id: Long): TaskWithSubtasks?

    // ------------------------- Scrittura task -------------------------

    /** Ritorna l'ID generato per poter creare subito le sottotask. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // ------------------------- Scrittura sottotask -------------------------

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    /**
     * Toggle rapido dello stato di una sottotask. Evita un round-trip
     * per rileggere/modificare/scrivere l'oggetto completo.
     */
    @Query("UPDATE subtasks SET status = :status WHERE id = :id")
    suspend fun setSubtaskStatus(id: Long, status: SubtaskStatus)
}
