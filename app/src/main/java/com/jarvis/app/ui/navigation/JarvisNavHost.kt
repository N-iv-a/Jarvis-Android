package com.jarvis.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarvis.app.ui.tasks.TaskEditorScreen
import com.jarvis.app.ui.tasks.TasksListScreen

/**
 * Grafo di navigazione.
 *
 * startDestination = TASKS: abbiamo rimosso la welcome screen come entry point
 * per andare diretti alla feature principale dopo lo sblocco biometrico.
 *
 * - TASKS: lista task
 * - TASK_EDITOR/{taskId}: creazione o modifica (taskId=0 → nuova)
 */
@Composable
fun JarvisNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = JarvisDestinations.TASKS,
    ) {
        composable(JarvisDestinations.TASKS) {
            TasksListScreen(
                onAddTask = {
                    navController.navigate(JarvisDestinations.taskEditor(0L))
                },
                onEditTask = { taskId ->
                    navController.navigate(JarvisDestinations.taskEditor(taskId))
                },
            )
        }

        composable(
            route = JarvisDestinations.TASK_EDITOR_ROUTE,
            arguments = listOf(
                navArgument(JarvisDestinations.TASK_EDITOR_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) {
            TaskEditorScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
