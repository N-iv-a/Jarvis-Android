package com.jarvis.app.data.habits

import androidx.room.TypeConverter

/**
 * Convertitori per Room per i tipi custom della feature Habits.
 * Li teniamo separati dai TaskConverters per non avere un "mega-converter"
 * con metodi di feature diverse.
 */
class HabitConverters {

    @TypeConverter
    fun habitTypeToString(value: HabitType): String = value.name

    @TypeConverter
    fun stringToHabitType(value: String): HabitType = HabitType.valueOf(value)
}
