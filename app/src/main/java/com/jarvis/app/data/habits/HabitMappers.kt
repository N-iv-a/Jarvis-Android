package com.jarvis.app.data.habits

import com.jarvis.app.domain.habits.Habit
import com.jarvis.app.domain.habits.HabitCheckIn

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    title = title,
    type = type,
    weeklyTarget = weeklyTarget,
    pointsPerWeek = pointsPerWeek,
    description = description,
    ordine = ordine,
    archived = archived,
)

fun HabitCheckInEntity.toDomain(): HabitCheckIn = HabitCheckIn(
    habitId = habitId,
    dayEpoch = dateEpochDay,
    value = value,
)
