package com.jarvis.app.data.tasks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Riga della tabella `subtasks`.
 *
 * Ogni sottotask appartiene a UNA task (1:N). La `foreignKey` con
 * `onDelete = CASCADE` garantisce che cancellando una task vengano
 * cancellate automaticamente le sue sottotask — nessun orfano nel DB.
 *
 * `ordine` serve a preservare l'ordine scelto dall'utente
 * (le sottotask non sono ordinate cronologicamente ma per trascinamento).
 *
 * L'`Index` su `taskId` accelera la query "dammi le sottotask di questa task"
 * — Room emette un warning senza questo indice per le foreign key.
 */
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("task_id")],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "task_id")
    val taskId: Long,

    val titolo: String,

    val status: SubtaskStatus = SubtaskStatus.PENDING,

    /** Posizione nella lista. Valore arbitrario, serve solo per ORDER BY. */
    val ordine: Int = 0,
)
