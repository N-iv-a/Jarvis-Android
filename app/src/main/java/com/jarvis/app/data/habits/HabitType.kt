package com.jarvis.app.data.habits

/**
 * Tipologia di abitudine. Determina la regola di valutazione settimanale.
 *
 * Volutamente RIDOTTI a 2 tipi (prima erano 5). Il check-in è sempre
 * binario: 1 = "ho fatto il bravo oggi", 0 = "non ce l'ho fatta".
 * Niente più input numerici (minuti, sigarette ecc.) — la fatica di
 * digitare un numero ogni sera affossava l'uso reale.
 *
 * - POSITIVE_DAILY: cose che VOGLIO fare. Es. lettura, dieta, allenamento.
 *   Anche "rispetto il limite di sigarette" rientra qui, perché di fatto
 *   ogni sera mi chiedo "sono stato bravo?" e spunto.
 *   La settimana è VINTA se giorni_spuntati >= weeklyTarget.
 *
 * - WEEKLY_CAP: cose che CAPITANO ma voglio limitare. Es. incontri Bridget,
 *   alcolici. Spunto i giorni in cui sono successe.
 *   La settimana è VINTA se giorni_spuntati <= weeklyTarget.
 *   (NB: una settimana CAP può "rompersi" se sforo il cap → punti negati,
 *   gestione nel PointsService.)
 */
enum class HabitType {
    POSITIVE_DAILY,
    WEEKLY_CAP,
}
