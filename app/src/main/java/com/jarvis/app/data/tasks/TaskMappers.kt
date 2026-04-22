package com.jarvis.app.data.tasks

import com.jarvis.app.domain.tasks.Subtask
import com.jarvis.app.domain.tasks.Task
import java.time.LocalDate

/**
 * Funzioni di conversione tra layer data (Room) e layer domain.
 *
 * Le teniamo qui (nel layer data) perché è la data-layer che conosce
 * entrambi i formati; il domain resta puro e non sa nulla di Room.
 */

fun TaskEntity.toDomain(subtasks: List<SubtaskEntity>): Task = Task(
    id = id,
    titolo = titolo,
    descrizione = descrizione,
    importanza = importanza,
    stato = stato,
    // epoch day → LocalDate. null resta null.
    deadline = deadlineEpochDay?.let { LocalDate.ofEpochDay(it) },
    pinDaysBefore = pinDaysBefore,
    createdAt = createdAt,
    completedAt = completedAt,
    subtasks = subtasks
        .sortedBy { it.ordine }
        .map { it.toDomain() },
)

fun TaskWithSubtasks.toDomain(): Task = task.toDomain(subtasks)

fun SubtaskEntity.toDomain(): Subtask = Subtask(
    id = id,
    taskId = taskId,
    titolo = titolo,
    status = status,
    ordine = ordine,
)

/**
 * Conversione inversa usata dal repository quando l'UI salva modifiche.
 * NON convertiamo le sottotask qui: vanno salvate separatamente (sono
 * entità figlie con id propri e serve `taskId`).
 */
fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    titolo = titolo,
    descrizione = descrizione,
    importanza = importanza,
    stato = stato,
    deadlineEpochDay = deadline?.toEpochDay(),
    pinDaysBefore = pinDaysBefore,
    createdAt = createdAt,
    completedAt = completedAt,
)

fun Subtask.toEntity(): SubtaskEntity = SubtaskEntity(
    id = id,
    taskId = taskId,
    titolo = titolo,
    status = status,
    ordine = ordine,
)
