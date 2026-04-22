package com.jarvis.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.jarvis.app.data.security.AppLockManager
import com.jarvis.app.data.security.BiometricAuthManager
import com.jarvis.app.data.security.BiometricAvailability
import com.jarvis.app.ui.screens.LockScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel sottile: espone i singleton di sicurezza ai composable in modo
 * compatibile con Hilt. Non c'è logica qui, è solo un "bridge" per evitare
 * di usare manualmente EntryPoints dentro i composable.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    val appLockManager: AppLockManager,
    val biometricAuthManager: BiometricAuthManager,
) : ViewModel()

/**
 * Wrapper che avvolge l'intero contenuto dell'app.
 *
 * - Se `isLocked` è true → mostra [LockScreen] e niente altro
 *   (così il contenuto sensibile non è mai composto/visibile).
 * - Se `isLocked` è false → mostra [content] (la navigazione normale).
 *
 * Per usare BiometricPrompt serve una FragmentActivity: recuperiamo
 * quella corrente dal [LocalContext]. MainActivity estende FragmentActivity.
 */
@Composable
fun AppLockGate(
    viewModel: AppLockViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val isLocked by viewModel.appLockManager.isLocked.collectAsState()

    if (!isLocked) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }

    // La availability è stabile per tutta la vita della LockScreen, possiamo
    // calcolarla una volta sola qui ad ogni recomposition del gate bloccato.
    val availability = viewModel.biometricAuthManager.checkAvailability()

    LockScreen(
        availability = availability,
        onRequestAuth = {
            if (activity != null && availability == BiometricAvailability.AVAILABLE) {
                viewModel.biometricAuthManager.authenticate(
                    activity = activity,
                    onSuccess = { /* AppLockManager.isLocked diventa false, UI si aggiorna da sola */ },
                    onError = { /* Silenzioso: la LockScreen resta. In futuro un toast. */ },
                )
            }
        },
    )
}

/**
 * Risale la catena dei ContextWrapper fino a trovare una FragmentActivity.
 * Necessario perché LocalContext.current può essere un ContextThemeWrapper
 * che avvolge l'Activity.
 */
private tailrec fun android.content.Context.findFragmentActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is android.content.ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
