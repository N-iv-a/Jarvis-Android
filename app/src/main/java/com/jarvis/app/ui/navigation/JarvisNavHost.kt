package com.jarvis.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarvis.app.ui.habits.HabitsRulesScreen
import com.jarvis.app.ui.habits.HabitsScreen
import com.jarvis.app.ui.tasks.TaskEditorScreen
import com.jarvis.app.ui.tasks.TasksListScreen

/**
 * Grafo di navigazione, ora con BottomBar.
 *
 * - Route "root" (TASKS, HABITS): bottom bar visibile.
 * - Route "secondary" (TASK_EDITOR): bottom bar nascosta — è una pagina
 *   modale che si conclude con back/save.
 *
 * Il pattern di navigazione tra root usa `popUpTo(start) saveState=true`
 * + `launchSingleTop=true` + `restoreState=true`: garantisce che
 *   - lo stack non si gonfia di duplicati delle root,
 *   - lo scroll/state delle pagine viene preservato passando da una root
 *     all'altra e tornando indietro.
 */
@Composable
fun JarvisNavHost() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val isRootRoute = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isRootRoute) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentBackStack?.destination?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(JarvisDestinations.TASKS) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = JarvisDestinations.TASKS,
            modifier = Modifier.padding(innerPadding),
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

            composable(JarvisDestinations.HABITS) {
                HabitsScreen(
                    onOpenRules = {
                        navController.navigate(JarvisDestinations.HABITS_RULES)
                    },
                )
            }

            composable(JarvisDestinations.HABITS_RULES) {
                HabitsRulesScreen(
                    onBack = { navController.popBackStack() },
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
}
