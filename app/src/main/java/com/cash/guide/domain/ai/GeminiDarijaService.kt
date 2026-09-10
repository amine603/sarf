package com.cash.guide.domain.ai

import android.util.Log
import com.cash.guide.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class ChecklistAiResult(
    val title: String,
    val items: List<String>
)

data class CalculationAiEntry(
    val label: String,
    val amount: Double
)

data class CalculationAiResult(
    val title: String,
    val entries: List<CalculationAiEntry>
)

object GeminiDarijaService {

    private const val TAG = "GeminiDarijaService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    var currentModel: String = "gemini-2.5-flash"

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * Parses a spoken Darija sentence into a structured shopping checklist.
     */
    suspend fun parseChecklistFromDarija(userSpeech: String): ChecklistAiResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is missing")
            return@withContext null
        }

        val systemPrompt = """
            You are an expert Moroccan Darija assistant for a notebook app called "Sarf".
            The user spoke or typed items in Moroccan Darija, Arabic, or French.
            Extract the items into a clean list.
            If the user mentioned quantities (e.g. 2kg, نص كيلو, رابعة, بكية, قرعة, ربطة, 3 حبات), include the quantity in each item label.
            Respond ONLY with a valid JSON object matching this schema:
            {
               "title": "a short title in Arabic/Darija e.g. تقضية or سخرة",
               "items": ["item 1 with quantity", "item 2 with quantity", ...]
            }
            Do not wrap in markdown quotes or codeblocks. Output pure JSON only.
        """.trimIndent()

        val prompt = "$systemPrompt\n\nUser speech: \"$userSpeech\""

        val responseJson = callGeminiApi(prompt, apiKey) ?: return@withContext null

        try {
            val textContent = extractContentText(responseJson) ?: return@withContext null
            val cleanJsonStr = sanitizeJsonString(textContent)
            val json = JSONObject(cleanJsonStr)
            val title = json.optString("title", "قائمة مشتريات").trim().ifBlank { "قائمة مشتريات" }
            val itemsArr = json.optJSONArray("items") ?: JSONArray()
            val items = mutableListOf<String>()
            for (i in 0 until itemsArr.length()) {
                val itemStr = itemsArr.getString(i).trim()
                if (itemStr.isNotBlank()) {
                    items.add(itemStr)
                }
            }
            ChecklistAiResult(title = title, items = items)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse checklist AI response", e)
            null
        }
    }

    /**
     * Parses a spoken Darija sentence into accounting ledger rows with amounts in Dirhams.
     */
    suspend fun parseCalculationFromDarija(userSpeech: String): CalculationAiResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is missing")
            return@withContext null
        }

        val systemPrompt = """
            You are an expert Moroccan accountant assistant for the app "Sarf".
            The user spoke or dictated monetary entries in Moroccan Darija, French, or Arabic.
            Convert all amounts to DIRHAMS (MAD):
            - "ريال" (Riyal): 1 Riyal = 0.05 Dirham. (Example: 100 ريال = 5 DH, 500 ريال = 25 DH, 1000 ريال = 50 DH, 2000 ريال = 100 DH).
            - "فرانك" (Franc): 1 Franc = 0.01 Dirham. (Example: 1000 فرانك = 10 DH).
            - "درهم" (Dirham): 1 Dirham = 1 DH.
            Extract each entry label and its final numeric amount in Dirhams.
            Respond ONLY with a valid JSON object matching this schema:
            {
               "title": "short title in Arabic/Darija e.g. مصاريف or حساب",
               "entries": [
                  { "label": "description", "amount": 150.0 },
                  ...
               ]
            }
            Do not wrap in markdown code blocks. Output pure JSON only.
        """.trimIndent()

        val prompt = "$systemPrompt\n\nUser speech: \"$userSpeech\""

        val responseJson = callGeminiApi(prompt, apiKey) ?: return@withContext null

        try {
            val textContent = extractContentText(responseJson) ?: return@withContext null
            val cleanJsonStr = sanitizeJsonString(textContent)
            val json = JSONObject(cleanJsonStr)
            val title = json.optString("title", "حساب").trim().ifBlank { "حساب" }
            val entriesArr = json.optJSONArray("entries") ?: JSONArray()
            val entries = mutableListOf<CalculationAiEntry>()
            for (i in 0 until entriesArr.length()) {
                val obj = entriesArr.getJSONObject(i)
                val label = obj.optString("label", "بند").trim()
                val amount = obj.optDouble("amount", 0.0)
                if (label.isNotBlank() && amount > 0.0) {
                    entries.add(CalculationAiEntry(label = label, amount = amount))
                }
            }
            CalculationAiResult(title = title, entries = entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse calculation AI response", e)
            null
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String? {
        val modelsToTry = listOf(currentModel, "gemini-1.5-flash", "gemini-2.5-flash-lite")
        for (model in modelsToTry) {
            val result = executeRequest(model, prompt, apiKey)
            if (result != null) return result
        }
        return null
    }

    private fun executeRequest(model: String, prompt: String, apiKey: String): String? {
        val url = URL("$BASE_URL/$model:generateContent?key=$apiKey")
        var conn: HttpURLConnection? = null
        return try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.connectTimeout = 12000
            conn.readTimeout = 15000
            conn.doOutput = true

            val requestBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { os ->
                os.write(requestBody.toString())
                os.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } else {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
                Log.w(TAG, "Gemini call to $model returned code $code: $err")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini model $model", e)
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun extractContentText(jsonStr: String): String? {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            parts.getJSONObject(0).optString("text")
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeJsonString(str: String): String {
        var clean = str.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json").trim()
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```").trim()
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```").trim()
        }
        val firstBrace = clean.indexOf('{')
        val lastBrace = clean.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            clean = clean.substring(firstBrace, lastBrace + 1)
        }
        return clean
    }
}
