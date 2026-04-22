package com.jarvis.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette dell'app — tema scuro indigo/slate ispirato al vecchio Jarvis web.
 *
 * Material 3 usa "tonal palettes": invece di scegliere ogni colore a mano,
 * definiamo alcuni colori chiave e lasciamo che il sistema generi i toni
 * intermedi automaticamente. Qui ci limitiamo ai colori "seed" principali.
 */

// --- Dark theme (default) ---
val IndigoPrimaryDark = Color(0xFF8B8CF6)        // accent principale — bottoni, FAB, azioni
val IndigoOnPrimaryDark = Color(0xFF1B1B3F)      // testo SU un primary (nero/scuro)
val IndigoContainerDark = Color(0xFF3A3B7A)      // contenitori con tinta primary
val IndigoOnContainerDark = Color(0xFFDFDFFF)    // testo SU container primary

val SlateBackgroundDark = Color(0xFF0F172A)      // sfondo schermata
val SlateSurfaceDark = Color(0xFF1E293B)         // card, dialog, sheet
val SlateSurfaceVariantDark = Color(0xFF334155)  // separatori, sfondi secondari

val TextPrimaryDark = Color(0xFFF1F5F9)          // testo principale
val TextSecondaryDark = Color(0xFF94A3B8)        // testo secondario, hint

val ErrorDark = Color(0xFFEF4444)
val SuccessDark = Color(0xFF10B981)              // non-standard Material, lo usiamo per stati custom
val WarningDark = Color(0xFFF59E0B)

// --- Light theme (fallback — per ora non lo useremo, l'app parte sempre in dark) ---
val IndigoPrimaryLight = Color(0xFF4F46E5)
val IndigoOnPrimaryLight = Color(0xFFFFFFFF)
val IndigoContainerLight = Color(0xFFE0E7FF)
val IndigoOnContainerLight = Color(0xFF1E1B4B)

val SlateBackgroundLight = Color(0xFFF8FAFC)
val SlateSurfaceLight = Color(0xFFFFFFFF)
val SlateSurfaceVariantLight = Color(0xFFE2E8F0)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
