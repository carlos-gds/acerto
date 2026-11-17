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

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun get(context: Context): String = prefs(context).getString(KEY, "").orEmpty().trim()

    fun set(context: Context, value: String) {
        prefs(context).edit().putString(KEY, value.trim()).apply()
    }

    /** Nome de quem recebe, como aparece no banco (vai no PIX copia e cola). */
    fun getName(context: Context): String = prefs(context).getString(KEY_NAME, "").orEmpty().trim()

    /** Cidade de quem recebe (vai no PIX copia e cola). */
    fun getCity(context: Context): String = prefs(context).getString(KEY_CITY, "").orEmpty().trim()

    fun setReceiver(context: Context, name: String, city: String) {
        prefs(context).edit().putString(KEY_NAME, name.trim()).putString(KEY_CITY, city.trim()).apply()
    }

    /** Dados do PIX para a mensagem de cobrança. */
    data class Info(val key: String, val name: String, val city: String)

    fun info(context: Context) = Info(get(context), getName(context), getCity(context))

    /** Estado observável da chave (atualiza a UI quando a chave muda em Ajustes). */
    @Composable
    fun rememberPixKey(): State<String> {
        val context = LocalContext.current
        val state = remember(context) { mutableStateOf(get(context)) }
        DisposableEffect(context) {
            val p = prefs(context)
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY || key == KEY_NAME || key == KEY_CITY) state.value = get(context)
            }
            p.registerOnSharedPreferenceChangeListener(listener)
            onDispose { p.unregisterOnSharedPreferenceChangeListener(listener) }
        }
        return state
    }

    /** Tipo provável da chave, só para orientar o usuário (aceita qualquer formato). */
    fun describe(key: String): String {
        val k = key.trim()
        if (k.isEmpty()) return ""
        if ("@" in k) return "E-mail"
        if (Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").matches(k)) {
            return "Chave aleatória"
        }
        if (k.startsWith("+")) return "Celular"
        val onlyNumberChars = k.all { it.isDigit() || it in ".-/() " }
        val digits = k.count { it.isDigit() }
        return when {
            !onlyNumberChars -> "Chave PIX"
            digits == 14 -> "CNPJ"
            digits == 11 -> "CPF ou celular"
            digits == 10 || digits == 12 || digits == 13 -> "Celular"
            else -> "Chave PIX"
        }
    }
}
