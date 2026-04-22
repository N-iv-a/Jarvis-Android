// Build file a livello di progetto (root).
// Qui dichiariamo SOLO i plugin usati nei moduli — senza applicarli (apply false).
// Ogni modulo poi li applica se serve.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
