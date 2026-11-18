package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Chave PIX do usuário, guardada só neste aparelho (SharedPreferences),
 * fora do banco Room e fora do backup. Usada no texto de cobrança.
 */
object PixKeyStore {
    private const val PREFS = "acerto_prefs"
    private const val KEY = "pix_key"
    private const val KEY_NAME = "pix_name"
    private const val KEY_CITY = "pix_city"
    private const val KEY_BANK = "pix_bank"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun get(context: Context): String = prefs(context).getString(KEY, "").orEmpty().trim()

    fun set(context: Context, value: String) {
        prefs(context).edit().putString(KEY, value.trim()).apply()
    }

    /** Nome de quem recebe, como aparece no banco (vai na cobrança). */
    fun getName(context: Context): String = prefs(context).getString(KEY_NAME, "").orEmpty().trim()

    /** Cidade salva em versões anteriores (o copia e cola usa "BRASIL" quando não há). */
    fun getCity(context: Context): String = prefs(context).getString(KEY_CITY, "").orEmpty().trim()

    /** Banco onde está a chave (só para mostrar na cobrança). */
    fun getBank(context: Context): String = prefs(context).getString(KEY_BANK, "").orEmpty().trim()

    fun setReceiver(context: Context, name: String, bank: String) {
        prefs(context).edit().putString(KEY_NAME, name.trim()).putString(KEY_BANK, bank.trim()).apply()
    }

    /** Estado observável da chave (atualiza a UI quando a chave muda em Ajustes). */
    @Composable
    fun rememberPixKey(): State<String> {
        val context = LocalContext.current
        val state = remember(context) { mutableStateOf(get(context)) }
        DisposableEffect(context) {
            val p = prefs(context)
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY || key == KEY_NAME || key == KEY_CITY || key == KEY_BANK) state.value = get(context)
            }
            p.registerOnSharedPreferenceChangeListener(listener)
            onDispose { p.unregisterOnSharedPreferenceChangeListener(listener) }
        }
        return state
    }

    /** Tipo da chave, detectado pelo formato (CPF, CNPJ, Celular, E-mail, Chave aleatória). */
    fun describe(key: String): String = PixPayload.detect(key)?.label ?: ""
}
