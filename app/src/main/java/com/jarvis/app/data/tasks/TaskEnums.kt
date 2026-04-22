package com.jarvis.app.data.tasks

/**
 * Importanza di una task. Sostituisce la matrice di Eisenhower del vecchio
 * Jarvis: più semplice e intuitivo.
 *
 * L'ordine dichiarato (ALTA, MEDIA, BASSA) è significativo: usiamo [ordinal]
 * per ordinare le task dalla più importante alla meno importante.
 */
enum class Importanza {
    ALTA,
    MEDIA,
    BASSA,
}

/**
 * Stato di avanzamento della task.
 *
 * - APERTO: task attiva, da fare.
 * - IN_ATTESA: bloccata da terzi (es. aspetto risposta Comune). Non è
 *   "completata" ma nemmeno "aperta" nel senso pratico di cose che posso fare ora.
 * - COMPLETATO: finita.
 */
enum class Stato {
    APERTO,
    IN_ATTESA,
    COMPLETATO,
}

/**
 * Stato di una sottotask. Solo due valori: voluto.
 * Il vecchio Jarvis usa emoji ⏳ (pending) e ✅ (done).
 */
enum class SubtaskStatus {
    PENDING,
    DONE,
}
