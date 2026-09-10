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
}
