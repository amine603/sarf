package com.cash.guide.domain

import android.net.Uri
import android.util.Base64
import com.cash.guide.data.db.ChecklistItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ParsedChecklist(
    val title: String,
    val items: List<Pair<String, Boolean>>
)

object ChecklistLinkHelper {

    private const val HTTPS_BASE_URL = "https://sarf.app/checklist"
    private const val CUSTOM_SCHEME = "sarf"
    private const val CUSTOM_HOST = "checklist"

    private fun encodeBase64Url(bytes: ByteArray): String {
        return try {
            android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }

    private fun decodeBase64Url(str: String): ByteArray {
        return try {
            android.util.Base64.decode(str, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getUrlDecoder().decode(str)
        }
    }

    /**
     * Encodes a checklist into a clickable HTTPS URL suitable for WhatsApp sharing.
     */
    fun createDeepLink(title: String, items: List<ChecklistItemEntity>): String {
        return try {
            val json = JSONObject().apply {
                put("t", title.trim())
                val itemsArr = JSONArray()
                val checkedArr = JSONArray()
                items.forEach { item ->
                    itemsArr.put(item.text)
                    checkedArr.put(if (item.isChecked) 1 else 0)
                }
                put("i", itemsArr)
                put("c", checkedArr)
            }
            val jsonBytes = json.toString().toByteArray(StandardCharsets.UTF_8)
            val encodedData = encodeBase64Url(jsonBytes)
            "$HTTPS_BASE_URL?d=$encodedData"
        } catch (e: Exception) {
            e.printStackTrace()
            val safeTitle = URLEncoder.encode(title, "UTF-8")
            "$HTTPS_BASE_URL?t=$safeTitle"
        }
    }

    /**
     * Parses an incoming deep link URL string (either https://sarf.app/checklist?... or sarf://checklist?...).
     */
    fun parseDeepLink(url: String): ParsedChecklist? {
        val uri = try { java.net.URI(url) } catch (_: Exception) { return null }
        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()

        val isHttpsMatch = (scheme == "https" || scheme == "http") && (host == "sarf.app" || host == "www.sarf.app") && (uri.path?.startsWith("/checklist") == true)
        val isCustomSchemeMatch = scheme == CUSTOM_SCHEME && (host == CUSTOM_HOST || uri.path?.contains(CUSTOM_HOST) == true)

        if (!isHttpsMatch && !isCustomSchemeMatch) {
            return null
        }

        // Query map
        val rawQuery = uri.rawQuery ?: ""
        val queryParams = mutableMapOf<String, MutableList<String>>()
        rawQuery.split("&").forEach { param ->
            val parts = param.split("=", limit = 2)
            if (parts.isNotEmpty()) {
                val key = parts[0]
                val value = if (parts.size > 1) parts[1] else ""
                queryParams.getOrPut(key) { mutableListOf() }.add(value)
            }
        }

        val encodedData = queryParams["d"]?.firstOrNull()
        if (!encodedData.isNullOrBlank()) {
            try {
                val jsonBytes = decodeBase64Url(encodedData)
                val jsonStr = String(jsonBytes, StandardCharsets.UTF_8)
                val json = JSONObject(jsonStr)
                val title = json.optString("t", "Checklist")
                val itemsArr = json.optJSONArray("i") ?: JSONArray()
                val checkedArr = json.optJSONArray("c")

                val itemsList = mutableListOf<Pair<String, Boolean>>()
                for (idx in 0 until itemsArr.length()) {
                    val text = itemsArr.getString(idx)
                    val isChecked = if (checkedArr != null && idx < checkedArr.length()) {
                        checkedArr.getInt(idx) == 1
                    } else {
                        false
                    }
                    if (text.isNotBlank()) {
                        itemsList.add(text to isChecked)
                    }
                }
                return ParsedChecklist(title = title, items = itemsList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val rawTitle = queryParams["t"]?.firstOrNull() ?: queryParams["title"]?.firstOrNull() ?: "Checklist"
        val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }

        val itemsParams = queryParams["i"] ?: queryParams["item"] ?: emptyList()
        val itemsList = itemsParams.mapNotNull {
            val text = try { URLDecoder.decode(it, "UTF-8").trim() } catch (_: Exception) { it.trim() }
            if (text.isNotEmpty()) text to false else null
        }

        return ParsedChecklist(title = title, items = itemsList)
    }

    /**
     * Parses an incoming Android Uri.
     */
    fun parseDeepLink(uri: Uri): ParsedChecklist? {
        return parseDeepLink(uri.toString())
    }
}
