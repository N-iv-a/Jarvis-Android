package com.jarvis.app.data.tasks

import androidx.room.TypeConverter

/**
 * Convertitori per Room: insegnano al DB come serializzare/deserializzare
 * i tipi custom (gli enum).
 *
 * Usiamo la forma String per:
 *   1. Leggibilità se un domani ispezioniamo il DB manualmente.
 *   2. Robustezza ai refactor: se riordiniamo gli enum, i dati esistenti
 *      non cambiano significato (al contrario di ordinal() che salva un int).
 *
 * Registrati sul @Database via @TypeConverters (vedi JarvisDatabase).
 */
class TaskConverters {

    @TypeConverter
    fun importanzaToString(value: Importanza): String = value.name

    @TypeConverter
    fun stringToImportanza(value: String): Importanza = Importanza.valueOf(value)

    @TypeConverter
    fun statoToString(value: Stato): String = value.name

    @TypeConverter
    fun stringToStato(value: String): Stato = Stato.valueOf(value)

    @TypeConverter
    fun subtaskStatusToString(value: SubtaskStatus): String = value.name

    @TypeConverter
    fun stringToSubtaskStatus(value: String): SubtaskStatus = SubtaskStatus.valueOf(value)
}
