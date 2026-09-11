package com.cash.guide.domain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.cash.guide.R
import com.cash.guide.data.db.ChecklistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChecklistShareHelper {

    fun shareAsImage(
        context: Context,
        checklistId: String,
        title: String,
        items: List<ChecklistItemEntity>,
        isRtl: Boolean,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    ) {
        coroutineScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    renderChecklistBitmap(context, title, items, isRtl)
                }
                val uri = withContext(Dispatchers.IO) {
                    saveBitmapToCache(context, bitmap, checklistId)
                }
                launchImageShareIntent(context, uri, title)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, R.string.share_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareAsTextAndLink(
        context: Context,
        title: String,
        items: List<ChecklistItemEntity>
    ) {
        val deepLink = ChecklistLinkHelper.createDeepLink(title, items)
        val sb = StringBuilder()

        sb.append("📋 *Checklist: ${title.ifBlank { "Liste" }}*\n\n")

        if (items.isEmpty()) {
            sb.append("_(Aucun élément)_\n")
        } else {
            items.forEach { item ->
                val mark = if (item.isChecked) "☑" else "☐"
                sb.append("$mark ${item.text}\n")
            }
        }

        sb.append("\n🔗 افتح القائمة وتكوشيها في تطبيق صرف:\n")
        sb.append(deepLink)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(sendIntent, "Partager via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun shareAsWhatsAppTextAndLink(
        context: Context,
        title: String,
        items: List<ChecklistItemEntity>
    ) = shareAsTextAndLink(context, title, items)

    private fun isArabicScript(text: String): Boolean {
        return text.any { c ->
            c in '\u0600'..'\u06FF' ||
            c in '\u0750'..'\u077F' ||
            c in '\u08A0'..'\u08FF' ||
            c in '\uFB50'..'\uFDFF' ||
            c in '\uFE70'..'\uFEFF'
        }
    }

    private fun truncateToWidth(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isEmpty()) "" else "$truncated..."
    }

    private fun renderChecklistBitmap(
        context: Context,
        title: String,
        items: List<ChecklistItemEntity>,
        isRtl: Boolean
    ): Bitmap {
        val width = 1080
        val ruleSpacing = 76f
        val marginX = 64f

        // 1. Script & RTL detection based on actual checklist content
        val hasArabic = isArabicScript(title) || items.any { isArabicScript(it.text) }
        val hasLatin = title.any { (it in 'a'..'z') || (it in 'A'..'Z') } || items.any { it.text.any { c -> (c in 'a'..'z') || (c in 'A'..'Z') } }
        val effectiveRtl = when {
            hasArabic && !hasLatin -> true
            hasLatin && !hasArabic -> false
            hasArabic -> true
            else -> isRtl
        }

        // 2. Bundled Fonts
        val creamFrothFont = runCatching {
            ResourcesCompat.getFont(context, R.font.cream_froth_regular)
                ?: ResourcesCompat.getFont(context, R.font.cream_froth)
        }.getOrNull()
        val creamFrothBold = runCatching {
            ResourcesCompat.getFont(context, R.font.cream_froth_bold)
        }.getOrNull()
        val majazFont = runCatching {
            ResourcesCompat.getFont(context, R.font.majaz_regular)
        }.getOrNull() ?: Typeface.DEFAULT
        val patrickHandFont = runCatching {
            ResourcesCompat.getFont(context, R.font.patrick_hand_regular)
        }.getOrNull() ?: Typeface.DEFAULT

        val primaryFont = if (effectiveRtl) (creamFrothFont ?: majazFont) else patrickHandFont
        val boldFont = if (effectiveRtl) (creamFrothBold ?: creamFrothFont ?: majazFont) else patrickHandFont

        // 3. Colors
        val paperColor = Color.rgb(0xFB, 0xF6, 0xE8)          // Cream Paper
        val inkColor = Color.rgb(0x24, 0x24, 0x21)            // Journal Ink
        val mutedInkColor = Color.rgb(0x75, 0x75, 0x70)       // Muted Ink
        val lineRuleColor = Color.argb(48, 0x1E, 0x5C, 0xA8)  // Ruled Notebook Blue
        val checkGreenColor = Color.rgb(0x1B, 0x7A, 0x4B)     // Emerald Ink
        val rowDotColors = listOf(
            Color.rgb(0x3B, 0x82, 0xB6),
            Color.rgb(0xD6, 0x5D, 0x82),
            Color.rgb(0x5E, 0x8C, 0x3B),
            Color.rgb(0xC7, 0x88, 0x1E),
            Color.rgb(0x7E, 0x5A, 0xA8),
            Color.rgb(0xCC, 0x67, 0x3B)
        )

        // 4. Exact Ruled Grid Structure:
        // Rule 1: Top spacing (empty)
        // Rule 2: App Branding & Date
        // Rule 3: Spacer
        // Rule 4: Title (with watercolor pink wash pill, text baseline sitting on Rule 4)
        // Rule 5: Progress / Subtitle (sitting on Rule 5)
        // Rule 6: Separator line (drawn directly along Rule 6 line)
        // Rule 7: Spacer before items
        // Rules 8 .. (7 + displayItemCount): Items (each sits on its rule)
        // Rule (8 + displayItemCount): Spacer
        // Rule (9 + displayItemCount): Footer Watermark ("Sarf • كناش الحسابات المغربي")
        // Rule (10 + displayItemCount): Bottom padding rule
        val displayItemCount = maxOf(items.size, 1)
        val totalRules = 10 + displayItemCount
        val height = (totalRules * ruleSpacing).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(paperColor)

        // 5. Draw horizontal blue ruled lines across the whole page
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineRuleColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        for (r in 1 until totalRules) {
            val y = r * ruleSpacing
            canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
        }

        // 6. Header: App Branding & Date (Rule 2)
        val rule2Y = 2 * ruleSpacing
        val rule2Baseline = rule2Y - 12f
        val brandText = if (effectiveRtl) "صرف • كناش الحسابات" else "Sarf • Carnet"
        val dateSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateSdf.format(Date())

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldFont
            textSize = 34f
            color = inkColor
            textAlign = if (effectiveRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = patrickHandFont
            textSize = 29f
            color = mutedInkColor
            textAlign = if (effectiveRtl) Paint.Align.LEFT else Paint.Align.RIGHT
        }

        if (effectiveRtl) {
            canvas.drawText(brandText, width - marginX, rule2Baseline, brandPaint)
            canvas.drawText(dateStr, marginX, rule2Baseline, datePaint)
        } else {
            canvas.drawText(brandText, marginX, rule2Baseline, brandPaint)
            canvas.drawText(dateStr, width - marginX, rule2Baseline, datePaint)
        }

        // 7. Title in Pink Wash Pill (Rule 4)
        val rule4Y = 4 * ruleSpacing
        val titleBaseline = rule4Y - 12f
        val headerTitle = if (title.isBlank()) (if (effectiveRtl) "قائمة المهام" else "Checklist") else title

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldFont
            color = inkColor
            textSize = 56f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val rawTitleW = titlePaint.measureText(headerTitle).coerceAtMost(width - marginX * 2 - 40f)
        val pillW = (rawTitleW + 64f).coerceAtMost(width - marginX * 2)
        val pillRect = RectF(
            (width - pillW) / 2f,
            rule4Y - ruleSpacing + 12f,
            (width + pillW) / 2f,
            rule4Y - 4f
        )
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0xF4, 0x8F, 0xB1) // Pink wash
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, 18f, 18f, pillPaint)
        val truncatedTitle = truncateToWidth(titlePaint, headerTitle, pillW - 40f)
        canvas.drawText(truncatedTitle, width / 2f, titleBaseline, titlePaint)

        // 8. Date and Progress Subtitle (Rule 5)
        val rule5Y = 5 * ruleSpacing
        val subBaseline = rule5Y - 12f
        val checkedCount = items.count { it.isChecked }
        val progressText = if (effectiveRtl) {
            if (checkedCount == items.size && items.isNotEmpty()) "جميع العناصر مكتملة (${items.size} / ${items.size}) ✨"
            else "$checkedCount من أصل ${items.size} مكتملة"
        } else {
            if (checkedCount == items.size && items.isNotEmpty()) "Tous les éléments complétés (${items.size}/${items.size}) ✨"
            else "$checkedCount / ${items.size} complétés"
        }
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = primaryFont
            color = mutedInkColor
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(progressText, width / 2f, subBaseline, progressPaint)

        // 9. Separator Line: drawn directly along Rule 6 blue line
        val separatorY = 6 * ruleSpacing
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 0x24, 0x24, 0x21)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(marginX, separatorY, width - marginX, separatorY, dividerPaint)

        // 10. Items Section (Rule 8 to 7 + displayItemCount)
        val checkboxSize = 42f
        val checkStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val checkFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = checkGreenColor
            strokeWidth = 4.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        val itemTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = primaryFont
            textSize = 48f
        }

        val itemNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldFont
            textSize = 46f
            isFakeBoldText = true
        }

        if (items.isEmpty()) {
            val emptyY = 8 * ruleSpacing
            val emptyBaseline = emptyY - 12f
            val emptyText = if (effectiveRtl) "(لا توجد عناصر في هذه القائمة)" else "(Aucun élément dans cette liste)"
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = primaryFont
                color = mutedInkColor
                textSize = 36f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(emptyText, width / 2f, emptyBaseline, emptyPaint)
        } else {
            items.forEachIndexed { index, item ->
                val lineY = (8 + index) * ruleSpacing
                val baselineY = lineY - 12f
                val checkboxY = lineY - checkboxSize - 9f

                val dotColor = rowDotColors[index % rowDotColors.size]
                itemNumPaint.color = dotColor

                if (effectiveRtl) {
                    // RTL: Number on Right, Text on Right, Checkbox on Left
                    val numX = width - marginX - 16f
                    itemNumPaint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("${index + 1}.", numX, baselineY, itemNumPaint)

                    // Checkbox on far Left
                    val boxX = marginX + 16f
                    val rect = RectF(boxX, checkboxY, boxX + checkboxSize, checkboxY + checkboxSize)
                    canvas.drawRoundRect(rect, 8f, 8f, checkStrokePaint)

                    if (item.isChecked) {
                        val p1x = rect.left + checkboxSize * 0.20f
                        val p1y = rect.top + checkboxSize * 0.52f
                        val p2x = rect.left + checkboxSize * 0.42f
                        val p2y = rect.top + checkboxSize * 0.76f
                        val p3x = rect.left + checkboxSize * 0.82f
                        val p3y = rect.top + checkboxSize * 0.22f
                        canvas.drawLine(p1x, p1y, p2x, p2y, checkFillPaint)
                        canvas.drawLine(p2x, p2y, p3x, p3y, checkFillPaint)
                    }

                    // Item Text flowing from Right to Left
                    val textRight = width - marginX - 78f
                    val textLeft = boxX + checkboxSize + 25f
                    val maxTextW = textRight - textLeft

                    val itemPaint = Paint(itemTextPaint).apply {
                        color = if (item.isChecked) mutedInkColor else inkColor
                        textAlign = Paint.Align.RIGHT
                    }
                    val displayText = truncateToWidth(itemPaint, item.text, maxTextW)
                    canvas.drawText(displayText, textRight, baselineY, itemPaint)

                    // Strikethrough line if checked
                    if (item.isChecked) {
                        val textW = itemPaint.measureText(displayText)
                        val strikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.argb(100, 0x24, 0x24, 0x21)
                            strokeWidth = 2.5f
                            strokeCap = Paint.Cap.ROUND
                        }
                        val strikeY = baselineY - 15f
                        canvas.drawLine(textRight - textW - 4f, strikeY, textRight + 4f, strikeY, strikePaint)
                    }
                } else {
                    // LTR: Number on Left, Text on Left, Checkbox on Right
                    val numX = marginX + 16f
                    itemNumPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText("${index + 1}.", numX, baselineY, itemNumPaint)

                    // Checkbox on far Right
                    val boxX = width - marginX - checkboxSize - 16f
                    val rect = RectF(boxX, checkboxY, boxX + checkboxSize, checkboxY + checkboxSize)
                    canvas.drawRoundRect(rect, 8f, 8f, checkStrokePaint)

                    if (item.isChecked) {
                        val p1x = rect.left + checkboxSize * 0.20f
                        val p1y = rect.top + checkboxSize * 0.52f
                        val p2x = rect.left + checkboxSize * 0.42f
                        val p2y = rect.top + checkboxSize * 0.76f
                        val p3x = rect.left + checkboxSize * 0.82f
                        val p3y = rect.top + checkboxSize * 0.22f
                        canvas.drawLine(p1x, p1y, p2x, p2y, checkFillPaint)
                        canvas.drawLine(p2x, p2y, p3x, p3y, checkFillPaint)
                    }

                    // Item Text flowing from Left to Right
                    val textLeft = marginX + 78f
                    val textRight = boxX - 25f
                    val maxTextW = textRight - textLeft

                    val itemPaint = Paint(itemTextPaint).apply {
                        color = if (item.isChecked) mutedInkColor else inkColor
                        textAlign = Paint.Align.LEFT
                    }
                    val displayText = truncateToWidth(itemPaint, item.text, maxTextW)
                    canvas.drawText(displayText, textLeft, baselineY, itemPaint)

                    // Strikethrough line if checked
                    if (item.isChecked) {
                        val textW = itemPaint.measureText(displayText)
                        val strikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.argb(100, 0x24, 0x24, 0x21)
                            strokeWidth = 2.5f
                            strokeCap = Paint.Cap.ROUND
                        }
                        val strikeY = baselineY - 15f
                        canvas.drawLine(textLeft - 4f, strikeY, textLeft + textW + 4f, strikeY, strikePaint)
                    }
                }
            }
        }

        // 11. Footer Brand Watermark
        val footerRule = 9 + displayItemCount
        val footerBaseline = footerRule * ruleSpacing - 12f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = primaryFont
            color = mutedInkColor
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Sarf • كناش الحسابات المغربي", width / 2f, footerBaseline, footerPaint)

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, checklistId: String): Uri {
        val shareDir = File(context.cacheDir, "shared_checklists")
        if (!shareDir.exists()) {
            shareDir.mkdirs()
        }
        val file = File(shareDir, "checklist_${checklistId}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun launchImageShareIntent(context: Context, uri: Uri, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Partager la Checklist").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
