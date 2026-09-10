package com.cash.guide

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GeminiDarijaServiceTest {

    @Test
    fun parseChecklistJsonSanitization() {
        val markdownJson = """
            ```json
            {
               "title": "تقضية السوق",
               "items": ["2kg مطيشة", "بكية أتاي السبع", "3 خبزات", "بيض"]
            }
            ```
        """.trimIndent()

        var clean = markdownJson.trim()
        if (clean.startsWith("```json")) clean = clean.removePrefix("```json").trim()
        if (clean.endsWith("```")) clean = clean.removeSuffix("```").trim()
        val firstBrace = clean.indexOf('{')
        val lastBrace = clean.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1) {
            clean = clean.substring(firstBrace, lastBrace + 1)
        }

        val json = JSONObject(clean)
        assertEquals("تقضية السوق", json.getString("title"))
        val items = json.getJSONArray("items")
        assertEquals(4, items.length())
        assertEquals("2kg مطيشة", items.getString(0))
        assertEquals("بكية أتاي السبع", items.getString(1))
    }

    @Test
    fun parseCalculationJsonWithRiyalConversion() {
        val rawJson = """
            {
               "title": "مصاريف اليوم",
               "entries": [
                  { "label": "سلعة", "amount": 150.0 },
                  { "label": "كرا", "amount": 25.0 },
                  { "label": "مازوط", "amount": 40.0 }
               ]
            }
        """.trimIndent()

        val json = JSONObject(rawJson)
        assertEquals("مصاريف اليوم", json.getString("title"))
        val entries = json.getJSONArray("entries")
        assertEquals(3, entries.length())
        assertEquals("سلعة", entries.getJSONObject(0).getString("label"))
        assertEquals(150.0, entries.getJSONObject(0).getDouble("amount"), 0.001)
        assertEquals("كرا", entries.getJSONObject(1).getString("label"))
        assertEquals(25.0, entries.getJSONObject(1).getDouble("amount"), 0.001)
    }

    @Test
    fun parseCalculationJsonWithOptionalAmounts() {
        val rawJson = """
            {
               "title": "تقضية",
               "entries": [
                  { "label": "خضرة", "amount": 0.0 },
                  { "label": "لحم", "amount": 120.0 },
                  { "label": "ترانسبور", "amount": 0.0 }
               ]
            }
        """.trimIndent()

        val json = JSONObject(rawJson)
        val entries = json.getJSONArray("entries")
        assertEquals(3, entries.length())
        assertEquals("خضرة", entries.getJSONObject(0).getString("label"))
        assertEquals(0.0, entries.getJSONObject(0).getDouble("amount"), 0.001)
        assertEquals("لحم", entries.getJSONObject(1).getString("label"))
        assertEquals(120.0, entries.getJSONObject(1).getDouble("amount"), 0.001)
        assertEquals("ترانسبور", entries.getJSONObject(2).getString("label"))
        assertEquals(0.0, entries.getJSONObject(2).getDouble("amount"), 0.001)
    }

    @Test
    fun resolveRowMatchesWithArabicRowNumbers() {
        val existingRows = (1..6).map { idx ->
            com.cash.guide.domain.ai.ExistingCalculationRowContext(
                id = "row_$idx",
                index = idx,
                label = "السطر $idx",
                currentAmount = 0.0
            )
        }

        val aiEntries = listOf(
            com.cash.guide.domain.ai.CalculationAiEntry(label = "السطر 5", amount = 15.0, existingRowId = null),
            com.cash.guide.domain.ai.CalculationAiEntry(label = "في السطر 6", amount = 77.0, existingRowId = null),
            com.cash.guide.domain.ai.CalculationAiEntry(label = "بند جديد", amount = 30.0, existingRowId = null)
        )

        val resolved = com.cash.guide.domain.ai.GeminiDarijaService.resolveRowMatches(aiEntries, existingRows)
        assertEquals(3, resolved.size)
        assertEquals("row_5", resolved[0].existingRowId)
        assertEquals(15.0, resolved[0].amount, 0.001)

        assertEquals("row_6", resolved[1].existingRowId)
        assertEquals(77.0, resolved[1].amount, 0.001)

        assertEquals(null, resolved[2].existingRowId)
        assertEquals(30.0, resolved[2].amount, 0.001)
    }

    @Test
    fun resolveRowMatchesWithFrenchAndFrancoRowNumbers() {
        val existingRows = listOf(
            com.cash.guide.domain.ai.ExistingCalculationRowContext("id_1", 1, "Pommes", 0.0),
            com.cash.guide.domain.ai.ExistingCalculationRowContext("id_2", 2, "Lait", 0.0),
            com.cash.guide.domain.ai.ExistingCalculationRowContext("id_5", 5, "Ligne 5", 0.0),
            com.cash.guide.domain.ai.ExistingCalculationRowContext("id_6", 6, "Star 6", 0.0)
        )

        val aiEntries = listOf(
            com.cash.guide.domain.ai.CalculationAiEntry(label = "Ligne 5", amount = 15.0, existingRowId = null),
            com.cash.guide.domain.ai.CalculationAiEntry(label = "Star 6", amount = 77.0, existingRowId = null),
            com.cash.guide.domain.ai.CalculationAiEntry(label = "Pommes", amount = 22.0, existingRowId = null)
        )

        val resolved = com.cash.guide.domain.ai.GeminiDarijaService.resolveRowMatches(aiEntries, existingRows)
        assertEquals("id_5", resolved[0].existingRowId)
        assertEquals(15.0, resolved[0].amount, 0.001)

        assertEquals("id_6", resolved[1].existingRowId)
        assertEquals(77.0, resolved[1].amount, 0.001)

        assertEquals("id_1", resolved[2].existingRowId)
        assertEquals("Pommes", resolved[2].label)
        assertEquals(22.0, resolved[2].amount, 0.001)
    }
}
