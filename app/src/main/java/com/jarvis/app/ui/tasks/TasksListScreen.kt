package com.jarvis.app.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.app.data.tasks.Importanza
import com.jarvis.app.data.tasks.Stato
import com.jarvis.app.data.tasks.SubtaskStatus
import com.jarvis.app.domain.tasks.Subtask
import com.jarvis.app.domain.tasks.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Schermata principale della feature Tasks.
 *
 * Struttura:
 * - TopAppBar con toggle "mostra/nascondi completate"
 * - Lista raggruppata per stato: APERTO → IN_ATTESA → COMPLETATO (se visibili)
 * - FAB per creare una nuova task
 *
 * Ogni card è espandibile con tap e mostra inline sottotask editabili.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TasksListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            imageVector = if (state.showCompleted) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (state.showCompleted) {
                                "Nascondi completate"
                            } else {
                                "Mostra completate"
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = "Nuova task")
            }
        },
    ) { padding ->
        TaskListContent(
            padding = padding,
            state = state,
            onEditTask = onEditTask,
            onChangeStato = { task, stato -> viewModel.setTaskStato(task, stato) },
            onDeleteTask = { viewModel.deleteTask(it) },
            onToggleSubtask = { viewModel.toggleSubtask(it) },
            onAddSubtask = { taskId, titolo -> viewModel.addSubtask(taskId, titolo) },
            onDeleteSubtask = { viewModel.deleteSubtask(it) },
        )
    }
}

@Composable
private fun TaskListContent(
    padding: PaddingValues,
    state: TasksListUiState,
    onEditTask: (Long) -> Unit,
    onChangeStato: (Task, Stato) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onAddSubtask: (Long, String) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
) {
    if (state.isLoading) return

    val isEmpty = state.aperte.isEmpty() &&
        state.inAttesa.isEmpty() &&
        (!state.showCompleted || state.completate.isEmpty())

    if (isEmpty) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Nessuna task.\nTocca il + per aggiungerne una.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        taskSection(
            title = "Da fare",
            tasks = state.aperte,
            onEditTask = onEditTask,
            onChangeStato = onChangeStato,
            onDeleteTask = onDeleteTask,
            onToggleSubtask = onToggleSubtask,
            onAddSubtask = onAddSubtask,
            onDeleteSubtask = onDeleteSubtask,
        )
        taskSection(
            title = "In attesa",
            tasks = state.inAttesa,
            onEditTask = onEditTask,
            onChangeStato = onChangeStato,
            onDeleteTask = onDeleteTask,
            onToggleSubtask = onToggleSubtask,
            onAddSubtask = onAddSubtask,
            onDeleteSubtask = onDeleteSubtask,
        )
        if (state.showCompleted) {
            taskSection(
                title = "Completate",
                tasks = state.completate,
                onEditTask = onEditTask,
                onChangeStato = onChangeStato,
                onDeleteTask = onDeleteTask,
                onToggleSubtask = onToggleSubtask,
                onAddSubtask = onAddSubtask,
                onDeleteSubtask = onDeleteSubtask,
            )
        }
    }
}

/**
 * Estensione pigra: aggiunge un'intestazione + le card delle task alla LazyColumn.
 * Separa in funzione per evitare duplicazione tra le 3 sezioni.
 */
private fun androidx.compose.foundation.lazy.LazyListScope.taskSection(
    title: String,
    tasks: List<Task>,
    onEditTask: (Long) -> Unit,
    onChangeStato: (Task, Stato) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onAddSubtask: (Long, String) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
) {
    if (tasks.isEmpty()) return

    item(key = "section_$title") {
        Text(
            text = "$title (${tasks.size})",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }

    items(tasks, key = { it.id }) { task ->
        TaskCard(
            task = task,
            onEdit = { onEditTask(task.id) },
            onChangeStato = { newStato -> onChangeStato(task, newStato) },
            onDelete = { onDeleteTask(task) },
            onToggleSubtask = onToggleSubtask,
            onAddSubtask = { titolo -> onAddSubtask(task.id, titolo) },
            onDeleteSubtask = onDeleteSubtask,
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onChangeStato: (Stato) -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ImportanzaDot(task.importanza)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.titolo,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.stato == Stato.COMPLETATO) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (task.stato == Stato.COMPLETATO) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    TaskMetaRow(task)
                }

                // Toggle espansione
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        contentDescription = if (expanded) "Comprimi" else "Espandi",
                    )
                }

                // Menu overflow
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Altre azioni")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modifica") },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                            onClick = { menuOpen = false; onEdit() },
                        )
                        // Cambio stato rapido: mostriamo gli stati diversi da quello corrente
                        Stato.entries.filter { it != task.stato }.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("Sposta in: ${s.label()}") },
                                onClick = { menuOpen = false; onChangeStato(s) },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Elimina") },
                            leadingIcon = { Icon(Icons.Filled.Delete, null) },
                            onClick = { menuOpen = false; onDelete() },
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                ExpandedSection(
                    task = task,
                    onToggleSubtask = onToggleSubtask,
                    onAddSubtask = onAddSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                )
            }
        }
    }
}

@Composable
private fun TaskMetaRow(task: Task) {
    val pinned = task.isPinned()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        if (pinned) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = "In scadenza",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        task.deadline?.let { date ->
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ITALIAN)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
        }
        if (task.subtasks.isNotEmpty()) {
            val done = task.subtasks.count { it.status == SubtaskStatus.DONE }
            Text(
                text = "$done / ${task.subtasks.size} ⏳",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpandedSection(
    task: Task,
    onToggleSubtask: (Subtask) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        if (!task.descrizione.isNullOrBlank()) {
            Text(
                text = task.descrizione,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        task.subtasks.forEach { sub ->
            SubtaskRow(
                subtask = sub,
                onToggle = { onToggleSubtask(sub) },
                onDelete = { onDeleteSubtask(sub) },
            )
        }

        AddSubtaskField(onAdd = onAddSubtask)
    }
}

@Composable
private fun SubtaskRow(
    subtask: Subtask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (subtask.status == SubtaskStatus.DONE) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (subtask.status == SubtaskStatus.DONE) {
                    "Segna come da fare"
                } else {
                    "Segna come fatto"
                },
                tint = if (subtask.status == SubtaskStatus.DONE) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Text(
            text = subtask.titolo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textDecoration = if (subtask.status == SubtaskStatus.DONE) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
            color = if (subtask.status == SubtaskStatus.DONE) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Rimuovi sottotask",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddSubtaskField(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Aggiungi sottotask…") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(
            enabled = text.isNotBlank(),
            onClick = {
                onAdd(text)
                text = ""
            },
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Aggiungi")
        }
    }
}

@Composable
private fun ImportanzaDot(importanza: Importanza) {
    val color = when (importanza) {
        Importanza.ALTA -> Color(0xFFEF4444)
        Importanza.MEDIA -> Color(0xFFF59E0B)
        Importanza.BASSA -> Color(0xFF10B981)
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private fun Stato.label(): String = when (this) {
    Stato.APERTO -> "Da fare"
    Stato.IN_ATTESA -> "In attesa"
    Stato.COMPLETATO -> "Completata"
}
