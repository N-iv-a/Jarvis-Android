package com.jarvis.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jarvis.app.data.security.BiometricAvailability

/**
 * Schermata mostrata quando l'app è bloccata.
 *
 * Comportamenti:
 * - Se il device ha biometria o un credential configurato → triggeriamo
 *   automaticamente il prompt all'apertura, e mostriamo un pulsante "Sblocca"
 *   come fallback se l'utente chiude il prompt.
 * - Se il device NON ha alcun blocco schermo configurato → mostriamo un
 *   messaggio e un pulsante per aprire le impostazioni di sicurezza.
 * - Se l'hardware è rotto/non disponibile → messaggio esplicativo.
 *
 * @param availability stato del sistema biometrico (ricavato dal chiamante).
 * @param onRequestAuth chiamato per (ri)lanciare il prompt. Il chiamante ha
 *   il riferimento a [FragmentActivity] e a [BiometricAuthManager], quindi
 *   è il posto naturale dove orchestrare la chiamata.
 */
@Composable
fun LockScreen(
    availability: BiometricAvailability,
    onRequestAuth: () -> Unit,
) {
    val context = LocalContext.current

    // Evita di rilanciare il prompt ad ogni recomposition: lo facciamo
    // una volta sola all'entrata nella LockScreen, poi solo su click.
    var autoTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(availability) {
        if (availability == BiometricAvailability.AVAILABLE && !autoTriggered) {
            autoTriggered = true
            onRequestAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )

            Text(
                text = "Jarvis è bloccato",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            when (availability) {
                BiometricAvailability.AVAILABLE -> {
                    Text(
                        text = "Autenticati per continuare.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onRequestAuth) {
                        Text("Sblocca")
                    }
                }

                BiometricAvailability.NOT_CONFIGURED -> {
                    Text(
                        text = "Per usare Jarvis devi prima impostare un " +
                            "blocco schermo (PIN, sequenza, password o impronta) " +
                            "nelle impostazioni del telefono.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = {
                        // Apre le impostazioni di sicurezza di sistema.
                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Apri impostazioni")
                    }
                }

                BiometricAvailability.HARDWARE_UNAVAILABLE -> {
                    Text(
                        text = "Il sensore di autenticazione non è disponibile " +
                            "in questo momento. Riprova più tardi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
