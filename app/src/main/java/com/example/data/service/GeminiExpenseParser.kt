package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.QuickExpenseAiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GeminiExpenseParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val candidateModels = listOf(
        "gemini-2.5-flash",
        "gemini-3.8-flash",
        "gemini-2.0-flash"
    )

    suspend fun parseExpensePrompt(
        prompt: String,
        currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ): Result<QuickExpenseAiResponse> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // If no valid API key is present, use offline intelligent extraction
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val offlineParsed = parseWithLocalFallback(prompt, currentYear, currentMonth)
            return@withContext if (offlineParsed != null) {
                Result.success(offlineParsed)
            } else {
                Result.failure(
                    IllegalStateException("Chave GEMINI_API_KEY não configurada no painel Secrets do AI Studio.")
                )
            }
        }

        // Try Gemini models with Structured Outputs
        var lastError: Exception? = null

        for (modelName in candidateModels) {
            try {
                val response = callGeminiWithStructuredOutput(modelName, apiKey, prompt, currentYear, currentMonth)
                if (response != null) {
                    return@withContext Result.success(response)
                }
            } catch (e: Exception) {
                Log.w("GeminiExpenseParser", "Failed with model $modelName: ${e.message}")
                lastError = e
            }
        }

        // If API calls failed (e.g. quota, network), attempt offline fallback before giving up
        val fallback = parseWithLocalFallback(prompt, currentYear, currentMonth)
        if (fallback != null) {
            return@withContext Result.success(fallback)
        }

        Result.failure(lastError ?: RuntimeException("Não foi possível analisar a mensagem com o Gemini."))
    }

    private fun callGeminiWithStructuredOutput(
        model: String,
        apiKey: String,
        userPrompt: String,
        currentYear: Int,
        currentMonth: Int
    ): QuickExpenseAiResponse? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userPrompt))
                    })
                })
            }
            put("contents", contentsArray)

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put(
                        "text",
                        "Você é um extrator inteligente de despesas financeiras para divisão de contas e cartões de crédito. " +
                        "A data atual de referência é mês $currentMonth e ano $currentYear. " +
                        "Analise o texto do usuário e extraia exatamente estes campos em JSON: " +
                        "1. 'description': título claro da compra/gasto (ex: 'Compra', 'Supermercado', 'Tênis', 'Panela', 'Lanche'). " +
                        "2. 'total_amount': número decimal com o valor total da compra em reais (ex: 240.0). Se houver centavos, preserve. " +
                        "3. 'installments': número inteiro de parcelas (ex: 3, 9). Se for compra à vista ou não mencionado, use 1. " +
                        "4. 'target_person': nome ou apelido da pessoa a quem a compra se destina ou com quem foi dividida (ex: 'Vó', 'Mãe', 'Lucas'). Se não mencionado, use 'Geral'. " +
                        "5. 'start_month': número inteiro (1 a 12) do mês de início das parcelas. Se o usuário indicar que a compra começou no passado (ex: 'começou há 5 meses', 'desde janeiro'), calcule o mês de início com base na data atual ($currentMonth/$currentYear). Caso contrário, use $currentMonth. " +
                        "6. 'start_year': número inteiro (4 dígitos) do ano de início das parcelas. Ajuste se o cálculo retroativo retroceder ao ano anterior. Caso contrário, use $currentYear."
                    ))
                })
            })

            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", JSONObject().apply {
                    put("type", "OBJECT")
                    val properties = JSONObject().apply {
                        put("description", JSONObject().put("type", "STRING"))
                        put("total_amount", JSONObject().put("type", "NUMBER"))
                        put("installments", JSONObject().put("type", "INTEGER"))
                        put("target_person", JSONObject().put("type", "STRING"))
                        put("start_month", JSONObject().put("type", "INTEGER"))
                        put("start_year", JSONObject().put("type", "INTEGER"))
                    }
                    put("properties", properties)
                    val required = JSONArray().apply {
                        put("description")
                        put("total_amount")
                        put("installments")
                        put("target_person")
                    }
                    put("required", required)
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { httpResponse ->
            val bodyString = httpResponse.body?.string() ?: ""
            if (!httpResponse.isSuccessful) {
                throw RuntimeException("Gemini HTTP ${httpResponse.code}: $bodyString")
            }

            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val text = parts.getJSONObject(0).optString("text", "")
            if (text.isBlank()) return null

            return parseJsonPayload(text, currentYear, currentMonth)
        }
    }

    private fun parseJsonPayload(rawJson: String, currentYear: Int, currentMonth: Int): QuickExpenseAiResponse {
        val trimmed = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(trimmed)
        val desc = json.optString("description", "").ifBlank { "Compra" }
        val amount = json.optDouble("total_amount", 0.0)
        val installments = json.optInt("installments", 1).coerceAtLeast(1)
        val person = json.optString("target_person", "").ifBlank { "Outros" }
        val startM = if (json.has("start_month") && !json.isNull("start_month")) {
            json.optInt("start_month", currentMonth).coerceIn(1, 12)
        } else {
            currentMonth
        }
        val startY = if (json.has("start_year") && !json.isNull("start_year")) {
            json.optInt("start_year", currentYear)
        } else {
            currentYear
        }

        return QuickExpenseAiResponse(
            description = desc.replaceFirstChar { it.uppercase() },
            total_amount = amount,
            installments = installments,
            target_person = person.replaceFirstChar { it.uppercase() },
            start_month = startM,
            start_year = startY
        )
    }

    /**
     * Fallback heuristic parser for local testing or offline use
     * (e.g., "Compra de 240 em 3x pra vó", "panela em 9x que começou há 5 meses pra mãe")
     */
    fun parseWithLocalFallback(
        prompt: String,
        currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ): QuickExpenseAiResponse? {
        val text = prompt.trim()
        if (text.isBlank()) return null

        // 1. Installments extraction (e.g. 3x, 3 parcelas, em 3x)
        val instMatcher = Pattern.compile("(?i)(?:em\\s*)?(\\d+)\\s*(?:x|vezes|parcelas)").matcher(text)
        val installments = if (instMatcher.find()) {
            instMatcher.group(1)?.toIntOrNull() ?: 1
        } else {
            1
        }

        // 2. Person extraction (e.g. pra vó, pro lucas, para mãe)
        val personMatcher = Pattern.compile("(?i)\\b(?:pra|pro|para|com|ao)\\s+([A-Za-zÀ-ÿ0-9_]+)").matcher(text)
        val person = if (personMatcher.find()) {
            personMatcher.group(1)?.replaceFirstChar { it.uppercase() } ?: "Outros"
        } else {
            "Outros"
        }

        // 3. Amount extraction (e.g. 240, 240.50, 240,00, R$ 240)
        val amountMatcher = Pattern.compile("(?i)(?:r\\$?\\s*)?(\\d+(?:[.,]\\d{1,2})?)").matcher(text)
        var amount = 0.0
        while (amountMatcher.find()) {
            val candidateStr = amountMatcher.group(1)?.replace(",", ".") ?: "0"
            val candidateVal = candidateStr.toDoubleOrNull() ?: 0.0
            // Don't mistake the installment number for amount unless it's the only number
            if (candidateVal != installments.toDouble() || amount == 0.0) {
                amount = candidateVal
            }
        }

        // 4. Retroactive start calculation (e.g. "começou há 5 meses", "há 3 meses")
        var startMonth = currentMonth
        var startYear = currentYear
        val monthsAgoMatcher = Pattern.compile("(?i)(?:começou\\s+)?h[aá]\\s+(\\d+)\\s*meses").matcher(text)
        if (monthsAgoMatcher.find()) {
            val monthsAgo = monthsAgoMatcher.group(1)?.toIntOrNull() ?: 0
            val totalMonths = (currentYear * 12 + (currentMonth - 1)) - monthsAgo
            startYear = totalMonths / 12
            startMonth = (totalMonths % 12) + 1
        }

        if (amount <= 0.0) {
            return null
        }

        // 5. Description extraction
        var cleanDesc = text
            .replace(Pattern.compile("(?i)(?:em\\s*)?\\d+\\s*(?:x|vezes|parcelas)").toRegex(), "")
            .replace(Pattern.compile("(?i)\\b(?:pra|pro|para|com|ao)\\s+[A-Za-zÀ-ÿ0-9_]+").toRegex(), "")
            .replace(Pattern.compile("(?i)(?:começou\\s+)?h[aá]\\s+\\d+\\s*meses(?:\\s*atr[aá]s)?").toRegex(), "")
            .replace(Pattern.compile("(?i)(?:r\\$?\\s*)?\\d+(?:[.,]\\d{1,2})?").toRegex(), "")
            .replace(Pattern.compile("(?i)\\b(?:de|em|da|do|que)\\b").toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()

        if (cleanDesc.isBlank()) {
            cleanDesc = "Compra"
        } else {
            cleanDesc = cleanDesc.replaceFirstChar { it.uppercase() }
        }

        return QuickExpenseAiResponse(
            description = cleanDesc,
            total_amount = amount,
            installments = installments,
            target_person = person,
            start_month = startMonth,
            start_year = startYear
        )
    }
}
