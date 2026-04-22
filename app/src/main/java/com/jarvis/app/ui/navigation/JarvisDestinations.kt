package com.jarvis.app.ui.navigation

/**
 * Destinazioni di navigazione — una per ogni schermata top-level.
 *
 * Tenerle in un unico file enum-like evita typo sulle route (stringhe magiche).
 */
object JarvisDestinations {
    const val WELCOME = "welcome"
    const val TASKS = "tasks"
    const val FINANCE = "finance"
    const val SETTINGS = "settings"

    /**
     * Route parametrica per l'editor di una task.
     *
     * - Pattern con argomento: "task_editor/{taskId}"
     * - taskId = 0 → modalità "nuova task".
     * - taskId > 0 → modalità "modifica" (carica dal DB).
     *
     * Mantenere anche il builder [taskEditor] per generare la route concreta
     * evita typo tra chi definisce e chi naviga.
     */
    const val TASK_EDITOR_ROUTE = "task_editor/{taskId}"
    const val TASK_EDITOR_ARG = "taskId"

    fun taskEditor(taskId: Long = 0L): String = "task_editor/$taskId"
}
