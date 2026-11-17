package com.example.util

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Bloqueio do app com a digital/rosto ou a senha do próprio celular.
 * Sem biblioteca extra: usa o BiometricPrompt do sistema (Android 10+)
 * e a tela de senha do sistema nas versões anteriores.
 */
object AppLock {
    private const val PREFS = "acerto_prefs"
    private const val KEY = "app_lock"

    /** Tempo em segundo plano depois do qual o app pede desbloqueio de novo. */
    const val RELOCK_AFTER_MS = 30_000L

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY, enabled).apply()
    }

    /** Só dá para bloquear se o celular tiver senha, PIN, padrão ou biometria. */
    fun isAvailable(context: Context): Boolean =
        (context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.isDeviceSecure == true

    @Composable
    fun rememberEnabled(): MutableState<Boolean> {
        val context = LocalContext.current
        return remember(context) { mutableStateOf(isEnabled(context)) }
    }
}
