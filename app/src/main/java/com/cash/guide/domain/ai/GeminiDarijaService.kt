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

data class ExistingCalculationRowContext(
    val id: String,
    val index: Int, // 1-based index (e.g. 1, 2, 3)
    val label: String,
    val currentAmount: Double
)

data class CalculationAiEntry(
    val label: String,
    val amount: Double,
    val existingRowId: String? = null // if non-null, this is an update to an existing row!
)

data class CalculationAiResult(
    val title: String,
    val entries: List<CalculationAiEntry>
)

object GeminiDarijaService {

    private const val TAG = "GeminiDarijaService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    var currentModel: String = "gemini-flash-lite-latest"

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    private fun getScriptInstruction(script: AiOutputScript): String {
        return when (script) {
            AiOutputScript.ARABIC -> """
                Output ALL item names and titles in natural Moroccan Arabic (الدارجة المغربية المفهومة بالحروف العربية).
                - If the user spoke in French, translate the items accurately into Moroccan Arabic (e.g. "stylos" -> "ستيلويات", "cahiers" -> "دفاتر", "tomates" -> "مطيشة", "pommes de terre" -> "بطاطا", "lait" -> "حليب", "viande" -> "لحم").
                - If the user spoke in Moroccan Darija, keep them in clean Moroccan Arabic letters.
                Titles: "قائمة مشتريات", "حساب سلعة", "مصاريف".
            """.trimIndent()
            AiOutputScript.FRANCO -> """
                Output ALL item names and titles in authentic Moroccan Darija Chati / Franco-Arabe (العرنسية المغربية القحة لي كيهضرو ويكتبو بيها المغاربة فـ WhatsApp وشات).
                RULES FOR AUTHENTIC MOROCCAN FRANCO:
                - Use standard Moroccan SMS/Chat numbers:
                  * 3 = 'ع' (e.g. 3assir, rba3a, 9or3a, ne3na3, za3tar)
                  * 7 = 'ح' (e.g. 7lib, l7em, 7out, teffa7, l7em baqri)
                  * 9 = 'ق' (e.g. 9ahwa, 9ezbour, 9alb)
                  * 5 or kh = 'خ' (e.g. khobz / 5obz, khizzou, khiyar)
                  * ch = 'ش' (e.g. maticha, chocolat, chfleur)
                - Use authentic Moroccan everyday words (NEVER formal Arabic transliteration):
                  * Potatoes: Batata (NOT al-batata)
                  * Tomatoes: Maticha (NOT tamatim)
                  * Carrots: Khizzou (NOT jazar)
                  * Onions: Bssla (NOT basal)
                  * Chicken: Djej (NOT dajaj)
                  * Meat: L7em or L7em baqri (NOT lahm)
                  * Minced meat: Kefta
                  * Bread: Khobz or 5obz
                  * Milk: 7lib (NOT halib)
                  * Butter: Zbda
                  * Oil: Zit or Zit l3oud
                  * Eggs: Lbid (NOT bayd)
                  * Apples: Teffa7
                  * Bananas: Banan
                  * Oranges: Limoun (NOT burtuqal)
                  * Fish: 7out (NOT samak)
                  * Coffee / Tea: 9ahwa / Ataye
                  * Sugar: Sekkar
                  * Cheese: Formaj
                  * Water: Lma / 9er3a d lma
                  * Line references: Star 1, Star 5, Ligne 5, Article 1
                - Quantities: kilo, 2 kilo, nss kilo, rba3a, bakiya, 9er3a, robta.
                - Titles: "La liste d te9diya", "7sab", "Masrouf".
            """.trimIndent()
            AiOutputScript.FRENCH -> """
                Output ALL item names and titles in clean, natural French.
                IMPORTANT:
                - If the user spoke in French (e.g. "dix cahiers à quinze dirhams", "trois stylos à 10 dirhams"):
                  Keep the items cleanly in French and extract the exact labels and prices accurately!
                - If the user spoke in Moroccan Darija, translate the terms into French:
                  * بطاطا -> Pommes de terre
                  * مطيشة -> Tomates
                  * خيزو -> Carottes
                  * بصلة -> Oignons
                  * دجاج -> Poulet
                  * لحم -> Viande
                  * كفتة -> Viande hachée
                  * حليب -> Lait
                  * زبدة -> Beurre
                  * زيت -> Huile
                  * بيض -> Oeufs
                  * تفاح -> Pommes
                  * بنان -> Bananes
                  * ليمون -> Oranges
                  * حوت -> Poisson
                  * قهوة -> Café
                  * أتاي -> Thé
                  * سكر -> Sucre
                  * فرماج -> Fromage
                  * خبز -> Pain
                Quantities: 1 kg, 500 g, 2 L, 1 paquet, 1 bouteille, etc.
                Titles: "Liste de courses", "Calcul des dépenses", "Facture".
            """.trimIndent()
        }
    }

    /**
     * Parses a spoken Darija/Arabic/French sentence into a structured shopping checklist.
     */
    suspend fun parseChecklistFromDarija(
        userSpeech: String,
        outputScript: AiOutputScript = AiOutputScript.ARABIC
    ): ChecklistAiResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is missing")
            return@withContext null
        }

        val scriptRule = getScriptInstruction(outputScript)

        val systemPrompt = """
            You are an expert Moroccan Darija assistant for the notebook app "Sarf".
            The user dictated a list of items or groceries in Moroccan Darija, Arabic, or French.
            Extract ALL items into a clean list, separating each item even if spoken rapidly in a single sentence.
            If the user mentioned quantities (e.g. 2kg, نص كيلو, رابعة, بكية, قرعة, ربطة, 3 حبات, 5 لتر), include the quantity in the item label.
            SMART DUPLICATE & QUANTITY MERGING:
            - If the user mentioned the same item multiple times or added quantity to an item previously mentioned (e.g. "2 كيلو بطاطا... وزيد كيلو د بطاطا" or "2 cahiers... et encore 3 cahiers"), MERGE them into a single clean entry with the combined total quantity (e.g. "3 كيلو بطاطا" or "5 cahiers"). Do not output duplicate items.
            $scriptRule
            Respond ONLY with a valid JSON object matching this schema:
            {
               "title": "short title",
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
            val defaultTitle = when (outputScript) {
                AiOutputScript.FRENCH -> "Liste de courses"
                AiOutputScript.FRANCO -> "La liste d te9diya"
                AiOutputScript.ARABIC -> "قائمة مشتريات"
            }
            val title = json.optString("title", defaultTitle).trim().ifBlank { defaultTitle }
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
     * Parses a spoken Darija/French/Arabic sentence into accounting ledger rows with amounts in Dirhams.
     * Amounts are OPTIONAL: items without specified price will have amount = 0.0.
     */
    suspend fun parseCalculationFromDarija(
        userSpeech: String,
        outputScript: AiOutputScript = AiOutputScript.ARABIC,
        existingRows: List<ExistingCalculationRowContext> = emptyList()
    ): CalculationAiResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is missing")
            return@withContext null
        }

        val scriptRule = getScriptInstruction(outputScript)

        val systemPrompt = """
            You are an expert Moroccan accountant assistant for the app "Sarf".
            The user dictated monetary entries, purchases, or expenses in Moroccan Darija, French, or Arabic.
            Extract ALL items into a clean calculation list with amounts in Dirhams (MAD).

            AMOUNTS & CURRENCY CONVERSIONS:
            - If the user mentions a price for an item, convert it to DIRHAMS (MAD):
              * "ريال" (Riyal): 1 Riyal = 0.05 Dirham. (Example: 100 ريال = 5 DH, 500 ريال = 25 DH, 1000 ريال = 50 DH, 2000 ريال = 100 DH).
              * "فرانك" (Franc): 1 Franc = 0.01 Dirham. (Example: 1000 فرانك = 10 DH).
              * "درهم" (Dirham): 1 Dirham = 1 DH.
            - If the user DOES NOT mention an amount or price for an item, set amount to 0.0. Include every item mentioned.
            SMART DUPLICATE & QUANTITY MERGING:
            - If the user mentioned the same item multiple times or added to a previously mentioned purchase (e.g. "2 كيلو بطاطا بـ 10 دراهم... وزيد كيلو آخر د بطاطا بـ 5 دراهم"), MERGE them into a single entry with the combined total quantity and total amount (e.g. "3 كيلو بطاطا" with amount 15.0). Do not output duplicate rows for the exact same item.
            $scriptRule

            Respond ONLY with a valid JSON object matching this schema:
            {
               "title": "short title",
               "entries": [
                  { "label": "description", "amount": 150.0 }
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
            val defaultTitle = when (outputScript) {
                AiOutputScript.FRENCH -> "Calcul"
                AiOutputScript.FRANCO -> "7sab"
                AiOutputScript.ARABIC -> "حساب"
            }
            val title = json.optString("title", defaultTitle).trim().ifBlank { defaultTitle }
            val entriesArr = json.optJSONArray("entries") ?: JSONArray()
            val entries = mutableListOf<CalculationAiEntry>()
            val defaultLabel = when (outputScript) {
                AiOutputScript.FRENCH -> "Article"
                AiOutputScript.FRANCO -> "Bnd"
                AiOutputScript.ARABIC -> "بند"
            }
            for (i in 0 until entriesArr.length()) {
                val obj = entriesArr.getJSONObject(i)
                val label = obj.optString("label", defaultLabel).trim()
                val amount = obj.optDouble("amount", 0.0)
                if (label.isNotBlank()) {
                    entries.add(
                        CalculationAiEntry(
                            label = label,
                            amount = maxOf(0.0, amount),
                            existingRowId = null
                        )
                    )
                }
            }
            CalculationAiResult(title = title, entries = entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse calculation AI response", e)
            null
        }
    }

    fun resolveRowMatches(
        entries: List<CalculationAiEntry>,
        existingRows: List<ExistingCalculationRowContext>
    ): List<CalculationAiEntry> {
        if (existingRows.isEmpty()) return entries

        return entries.map { entry ->
            if (entry.existingRowId != null && existingRows.any { it.id == entry.existingRowId }) {
                // Valid existingRowId already resolved by LLM
                entry
            } else {
                // Fallback deterministic resolution from label
                val matchedRow = findMatchingRow(entry.label, existingRows)
                if (matchedRow != null) {
                    val finalLabel = if (isGenericRowLabel(entry.label) && !isGenericRowLabel(matchedRow.label)) {
                        matchedRow.label
                    } else {
                        entry.label
                    }
                    entry.copy(
                        label = finalLabel,
                        existingRowId = matchedRow.id
                    )
                } else {
                    entry
                }
            }
        }
    }

    private fun findMatchingRow(
        label: String,
        existingRows: List<ExistingCalculationRowContext>
    ): ExistingCalculationRowContext? {
        val clean = label.trim().lowercase()

        // 1. Extract row number from regex
        val patterns = listOf(
            Regex("""(?:السطر|سطر|نمرة|رقم|لارتيكل|ارتيكل|لا\s*لين|لالين|ligne|la\s+ligne|line|row|star|satr|article)\s*#?\s*([0-9]+)"""),
            Regex("""^#?([0-9]+)$""")
        )
        for (pattern in patterns) {
            val match = pattern.find(clean)
            if (match != null) {
                val num = match.groupValues[1].toIntOrNull()
                if (num != null) {
                    val found = existingRows.find { it.index == num }
                    if (found != null) return found
                }
            }
        }

        // 2. Arabic number words
        val arabicOrdinals = mapOf(
            "الأول" to 1, "الاول" to 1, "الأولى" to 1,
            "الثاني" to 2, "التاني" to 2, "الثانية" to 2,
            "الثالث" to 3, "التالت" to 3, "الثالثة" to 3,
            "الرابع" to 4, "الرابعة" to 4,
            "الخامس" to 5, "الخامسة" to 5,
            "السادس" to 6, "السادسة" to 6,
            "السابع" to 7, "السابعة" to 7,
            "الثامن" to 8, "التامن" to 8, "الثامنة" to 8,
            "التاسع" to 9, "التاسعة" to 9,
            "العاشر" to 10, "العاشرة" to 10
        )
        for ((word, idx) in arabicOrdinals) {
            if (clean.contains(word)) {
                val found = existingRows.find { it.index == idx }
                if (found != null) return found
            }
        }

        // 3. French number words
        val frenchOrdinals = mapOf(
            "premier" to 1, "premiere" to 1, "première" to 1,
            "deuxieme" to 2, "deuxième" to 2,
            "troisieme" to 3, "troisième" to 3,
            "quatrieme" to 4, "quatrième" to 4,
            "cinquieme" to 5, "cinquième" to 5,
            "sixieme" to 6, "sixième" to 6,
            "septieme" to 7, "septième" to 7,
            "huitieme" to 8, "huitième" to 8,
            "neuvieme" to 9, "neuvième" to 9,
            "dixieme" to 10, "dixième" to 10
        )
        for ((word, idx) in frenchOrdinals) {
            if (clean.contains(word)) {
                val found = existingRows.find { it.index == idx }
                if (found != null) return found
            }
        }

        // 4. Exact or close match with existing item label
        val exactMatch = existingRows.find {
            val exLabel = it.label.trim().lowercase()
            !isGenericRowLabel(exLabel) && (exLabel == clean || clean.contains(exLabel) || exLabel.contains(clean))
        }
        if (exactMatch != null) return exactMatch

        return null
    }

    private fun isGenericRowLabel(label: String): Boolean {
        val clean = label.trim().lowercase()
        return clean.isBlank() ||
                clean.startsWith("السطر") ||
                clean.startsWith("سطر") ||
                clean.startsWith("ligne") ||
                clean.startsWith("star") ||
                clean.startsWith("satr") ||
                clean.startsWith("article") ||
                clean.startsWith("row") ||
                clean.startsWith("line")
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String? {
        val modelsToTry = listOf(
            currentModel,
            "gemini-3.5-flash-lite",
            "gemini-2.5-flash",
            "gemini-flash-latest"
        ).distinct()
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
            conn.connectTimeout = 7000
            conn.readTimeout = 8000
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

                // High speed generation config with JSON schema enforcement and zero temperature
                val genConfig = JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                    put("maxOutputTokens", 800)
                }
                put("generationConfig", genConfig)
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
