package com.jarvis.app.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.app.data.habits.HabitType
import com.jarvis.app.domain.habits.Habit
import com.jarvis.app.domain.habits.WeeklyProgress
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Schermata "Abitudini" — la timeline.
 *
 * Layout (dall'alto verso il basso):
 *   - TopBar: titolo, icona "Regole", saldo punti.
 *   - LazyColumn di settimane (corrente in cima, scorsa sotto).
 *     Ogni settimana è una Card con:
 *       - header: range date + "vinte X/Y"
 *       - 7 colonne giorno (label lun-dom)
 *       - 1 riga per abitudine con N pallini cliccabili (verde/rosso/vuoto)
 *
 * UX: niente più bottom sheet. Ogni pallino è un toggle one-tap diretto.
 * Pallini su date future sono disabilitati. Le settimane mostrate sono
 * SEMPRE editabili (visibilità = editabilità).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onOpenRules: () -> Unit,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abitudini", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onOpenRules) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Regole",
                        )
                    }
                    PointsBalanceChip(state.pointsBalance)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold

        if (state.habits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nessuna abitudine.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.weeks, key = { it.start.toEpochDay() }) { week ->
                    WeekCard(
                        week = week,
                        habits = state.habits,
                        onToggle = { habit, date -> viewModel.toggleDot(habit, date) },
                    )
                }
            }
        }
    }
}

// ---------------- Building blocks ----------------

@Composable
private fun PointsBalanceChip(points: Int) {
    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = points.toString(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WeekCard(
    week: WeekRow,
    habits: List<Habit>,
    onToggle: (Habit, LocalDate) -> Unit,
) {
    val end = week.start.plusDays(6)
    val fmt = WEEK_HEADER_FMT
    val wonCount = week.progressByHabit.values.count { it.isWon }
    val totalCount = habits.size

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ---------- Header ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${week.start.format(fmt)} – ${end.format(fmt)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Vinte $wonCount/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ---------- Header colonne giorno (lun-dom) ----------
            DayHeaderRow(weekStart = week.start)

            Spacer(Modifier.height(4.dp))

            // ---------- Una riga per abitudine ----------
            habits.forEach { habit ->
                HabitWeekRowView(
                    habit = habit,
                    progress = week.progressByHabit[habit.id],
                    cellsByDay = week.cellsByHabit[habit.id] ?: emptyMap(),
                    weekStart = week.start,
                    onToggle = onToggle,
                )
            }
        }
    }
}

// DateTimeFormatter è immutabile e thread-safe: una sola istanza top-level.
private val WEEK_HEADER_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)

@Composable
private fun DayHeaderRow(weekStart: LocalDate) {
    val today = LocalDate.now()
    Row(modifier = Modifier.fillMaxWidth()) {
        // Spazio per la "label abitudine" (allineato con HabitWeekRowView).
        Spacer(Modifier.width(LABEL_WIDTH))
        for (offset in 0..6) {
            val date = weekStart.plusDays(offset.toLong())
            val isToday = date == today
            val isFuture = date > today
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.dayOfWeek.shortLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun HabitWeekRowView(
    habit: Habit,
    progress: WeeklyProgress?,
    cellsByDay: Map<Int, Boolean>,
    weekStart: LocalDate,
    onToggle: (Habit, LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(LABEL_WIDTH)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            progress?.let {
                Text(
                    text = miniProgressLabel(habit, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = miniProgressColor(it),
                )
            }
        }
        for (offset in 0..6) {
            val date = weekStart.plusDays(offset.toLong())
            val isFuture = date > today
            val checked = cellsByDay[offset] == true
            // Box cliccabile su cui sta il pallino. Hit area generosa
            // (32dp) così il dito non deve essere preciso al millimetro.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clickable(enabled = !isFuture) { onToggle(habit, date) },
                contentAlignment = Alignment.Center,
            ) {
                DayDot(
                    checked = checked,
                    isFuture = isFuture,
                    isCap = habit.type == HabitType.WEEKLY_CAP,
                )
            }
        }
    }
}

@Composable
private fun DayDot(checked: Boolean, isFuture: Boolean, isCap: Boolean) {
    val color = when {
        isFuture -> Color.Transparent
        !checked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
        // CAP: ogni spunta è un "ho ceduto" → rosso. POSITIVE: verde.
        isCap -> Color(0xFFEF4444)
        else -> Color(0xFF10B981)
    }
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isFuture) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                shape = CircleShape,
            ),
    )
}

// ---------------- Helpers UI ----------------

private val LABEL_WIDTH = 110.dp

private fun DayOfWeek.shortLabel(): String =
    getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
        .take(3)
        .replaceFirstChar { it.uppercase() }

private fun miniProgressLabel(habit: Habit, progress: WeeklyProgress): String {
    return if (habit.type == HabitType.WEEKLY_CAP) {
        "${progress.count}/${progress.target} max"
    } else {
        "${progress.count}/${progress.target}"
    }
}

@Composable
private fun miniProgressColor(progress: WeeklyProgress): Color = when {
    progress.isLost -> Color(0xFFEF4444)
    progress.isWon -> Color(0xFF10B981)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
