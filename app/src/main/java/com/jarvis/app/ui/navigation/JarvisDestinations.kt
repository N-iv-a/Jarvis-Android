package com.jarvis.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinazioni di navigazione — una per ogni route top-level.
 *
 * Le route "root" sono quelle tra cui si salta con la bottom bar.
 * Le route "secondary" (editor, dettagli) non compaiono in bottom bar e
 * fanno nascondere la bar quando attive.
 */
object JarvisDestinations {
    const val TASKS = "tasks"

    /** Pagina unica abitudini (timeline con toggle diretto sui pallini). */
    const val HABITS = "habits"

    /** Pagina di sola lettura con le descrizioni delle abitudini. */
    const val HABITS_RULES = "habits_rules"

    /** Route parametrica per l'editor di una task (taskId=0 → nuova). */
    const val TASK_EDITOR_ROUTE = "task_editor/{taskId}"
    const val TASK_EDITOR_ARG = "taskId"

    fun taskEditor(taskId: Long = 0L): String = "task_editor/$taskId"
}

/**
 * Descrittore di una voce della bottom bar.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

/** Voci della bottom bar, nell'ordine in cui vanno mostrate. */
val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(
        route = JarvisDestinations.TASKS,
        label = "Task",
        icon = Icons.AutoMirrored.Filled.Assignment,
    ),
    BottomNavItem(
        route = JarvisDestinations.HABITS,
        label = "Abitudini",
        icon = Icons.Filled.TaskAlt,
    ),
)
