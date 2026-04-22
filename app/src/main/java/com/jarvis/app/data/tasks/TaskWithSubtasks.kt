package com.jarvis.app.data.tasks

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO che Room popola automaticamente con una task e tutte le sue sottotask.
 *
 * @Embedded: i campi di TaskEntity vengono "inlinati" qui.
 * @Relation: Room esegue una seconda query (SELECT * FROM subtasks WHERE task_id = :id)
 *   per ogni task, e associa i risultati. È fatto in modo ottimizzato (batch)
 *   quindi non crea il classico problema N+1.
 */
data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "task_id",
    )
    val subtasks: List<SubtaskEntity> = emptyList(),
)
