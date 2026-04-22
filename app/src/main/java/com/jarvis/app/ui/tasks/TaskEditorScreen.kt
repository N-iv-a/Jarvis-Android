package com.jarvis.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.app.data.tasks.Importanza
import com.jarvis.app.data.tasks.Stato
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Schermata di creazione/modifica di una task.
 *
 * Il ViewModel decide automaticamente la modalità: se l'argument `taskId` è
 * presente e > 0 carica la task da DB, altrimenti parte da uno state vuoto.
 *
 * `onSaved` viene chiamato dopo il salvataggio riuscito — la navigazione
 * fa pop dello stack e torna alla lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.id == null) "Nuova task" else "Modifica task")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.titolo,
                onValueChange = { viewModel.onTitoloChange(it) },
                label = { Text("Titolo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.descrizione,
                onValueChange = { viewModel.onDescrizioneChange(it) },
                label = { Text("Descrizione (opzionale)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Section("Importanza") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Importanza.entries.forEach { imp ->
                        FilterChip(
                            selected = state.importanza == imp,
                            onClick = { viewModel.onImportanzaChange(imp) },
                            label = { Text(imp.label()) },
                        )
                    }
                }
            }

            Section("Stato") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stato.entries.forEach { st ->
                        FilterChip(
                            selected = state.stato == st,
                            onClick = { viewModel.onStatoChange(st) },
                            label = { Text(st.shortLabel()) },
                        )
                    }
                }
            }

            DeadlineField(
                deadline = state.deadline,
                onChange = { viewModel.onDeadlineChange(it) },
            )

            PinDaysField(
                value = state.pinDaysBefore,
                enabled = state.deadline != null,
                onChange = { viewModel.onPinDaysBeforeChange(it) },
            )

            Spacer(Modifier.width(0.dp))

            Button(
                onClick = { viewModel.save() },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Salva")
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

/**
 * Campo deadline con DatePicker di Material 3. Per pulire la data c'è un
 * pulsante "Nessuna".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineField(
    deadline: LocalDate?,
    onChange: (LocalDate?) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Section("Scadenza") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = deadline?.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(Locale.ITALIAN),
                    ) ?: "Imposta data…",
                )
            }
            if (deadline != null) {
                TextButton(onClick = { onChange(null) }) { Text("Nessuna") }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadline
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val newDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onChange(newDate)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Annulla") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/**
 * Selettore "giorni prima della scadenza per accendere il pin".
 * Disabilitato se non c'è deadline (non avrebbe senso).
 */
@Composable
private fun PinDaysField(
    value: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val options = listOf(0, 1, 3, 7, 14, 30)

    Section("Pin giorni prima") {
        Box {
            TextButton(
                enabled = enabled,
                onClick = { menuOpen = true },
            ) {
                Text(if (value == 0) "Solo il giorno stesso" else "$value giorni prima")
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = {
                            Text(if (opt == 0) "Solo il giorno stesso" else "$opt giorni prima")
                        },
                        onClick = {
                            onChange(opt)
                            menuOpen = false
                        },
                    )
                }
            }
        }
    }
}

private fun Importanza.label(): String = when (this) {
    Importanza.ALTA -> "Alta"
    Importanza.MEDIA -> "Media"
    Importanza.BASSA -> "Bassa"
}

private fun Stato.shortLabel(): String = when (this) {
    Stato.APERTO -> "Da fare"
    Stato.IN_ATTESA -> "In attesa"
    Stato.COMPLETATO -> "Completata"
}
