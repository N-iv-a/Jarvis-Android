package com.jarvis.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.jarvis.app.ui.AppLockGate
import com.jarvis.app.ui.navigation.JarvisNavHost
import com.jarvis.app.ui.theme.JarvisTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Unica Activity dell'app.
 *
 * Estende FragmentActivity (non ComponentActivity) perché BiometricPrompt
 * richiede una FragmentActivity per mostrare il dialog di autenticazione.
 * FragmentActivity è comunque un sottotipo di ComponentActivity, quindi
 * tutto il resto (Compose, enableEdgeToEdge, Hilt) continua a funzionare.
 *
 * Filosofia Compose: "single-activity architecture".
 * Ogni schermata è un Composable, la navigazione è gestita da Navigation Compose
 * all'interno di JarvisNavHost — l'Activity resta praticamente vuota.
 *
 * @AndroidEntryPoint permette a Hilt di iniettare dipendenze qui dentro.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge: contenuto disegnato sotto status/navigation bar
        // → look moderno, standard Material 3
        enableEdgeToEdge()

        setContent {
            JarvisApp()
        }
    }
}

/**
 * Root Composable dell'app. Isolato dalla Activity così da poter fare
 * anteprime nello strumento @Preview di Android Studio.
 *
 * L'AppLockGate è il primo livello dopo il tema: garantisce che nessun
 * contenuto sensibile venga composto finché l'utente non si è autenticato.
 */
@Composable
private fun JarvisApp() {
    JarvisTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppLockGate {
                JarvisNavHost()
            }
        }
    }
}
