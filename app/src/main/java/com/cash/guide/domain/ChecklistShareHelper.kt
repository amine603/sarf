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

    private fun renderChecklistBitmap(
        context: Context,
        title: String,
        items: List<ChecklistItemEntity>,
        isRtl: Boolean
    ): Bitmap {
        val width = 1080
        val ruleSpacing = 74f
        val marginX = 60f

        // Fonts
        val majazFont = runCatching {
            ResourcesCompat.getFont(context, R.font.majaz_regular)
        }.getOrNull() ?: Typeface.DEFAULT
        val patrickHandFont = runCatching {
            ResourcesCompat.getFont(context, R.font.patrick_hand_regular)
        }.getOrNull() ?: Typeface.DEFAULT
        val manropeBold = runCatching {
            ResourcesCompat.getFont(context, R.font.manrope_bold)
        }.getOrNull() ?: Typeface.DEFAULT_BOLD

        val primaryFont = if (isRtl) majazFont else patrickHandFont

        // Colors
        val paperColor = Color.rgb(0xFB, 0xF6, 0xE8)          // Cream Paper
        val inkColor = Color.rgb(0x24, 0x24, 0x21)            // Journal Ink
        val mutedInkColor = Color.rgb(0x75, 0x75, 0x70)       // Muted Ink
        val lineRuleColor = Color.argb(45, 0x1E, 0x5C, 0xA8)  // Ruled Notebook Blue
        val checkGreenColor = Color.rgb(0x1B, 0x7A, 0x4B)     // Emerald Ink
        val rowDotColors = listOf(
            Color.rgb(0x3B, 0x82, 0xB6),
            Color.rgb(0xD6, 0x5D, 0x82),
            Color.rgb(0x5E, 0x8C, 0x3B),
            Color.rgb(0xC7, 0x88, 0x1E),
            Color.rgb(0x7E, 0x5A, 0xA8),
            Color.rgb(0xCC, 0x67, 0x3B)
        )

        // Calculate dynamic height
        val topPaddingRules = 3
        val headerRules = 2
        val itemRules = maxOf(items.size, 4)
        val footerRules = 3
        val totalRules = topPaddingRules + headerRules + itemRules + footerRules + 2
        val height = (totalRules * ruleSpacing).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(paperColor)

        // Draw horizontal blue ruled lines
        val linePaint = Paint().apply {
            color = lineRuleColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        var curY = ruleSpacing
        while (curY < height) {
            canvas.drawLine(0f, curY, width.toFloat(), curY, linePaint)
            curY += ruleSpacing
        }

        // Header Title in Pink Pill
        var baselineY = (topPaddingRules + 1) * ruleSpacing - 18f
        val headerTitle = if (title.isBlank()) "Checklist" else title

        val titlePaint = Paint().apply {
            typeface = primaryFont
            color = inkColor
            isAntiAlias = true
            textSize = 50f
            textAlign = Paint.Align.CENTER
        }

        val titleWidth = titlePaint.measureText(headerTitle)
        val pillRect = RectF(
            (width - titleWidth) / 2f - 30f,
            baselineY - 44f,
            (width + titleWidth) / 2f + 30f,
            baselineY + 14f
        )
        val pillPaint = Paint().apply {
            color = Color.argb(90, 0xF4, 0x8F, 0xB1) // Pink pill
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(pillRect, 20f, 20f, pillPaint)
        canvas.drawText(headerTitle, width / 2f, baselineY, titlePaint)

        // Date and progress line (Centered)
        baselineY += ruleSpacing
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val checkedCount = items.count { it.isChecked }
        val statusStr = "$dateStr • $checkedCount / ${items.size} faits"

        val subPaint = Paint().apply {
            typeface = manropeBold
            color = mutedInkColor
            isAntiAlias = true
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(statusStr, width / 2f, baselineY, subPaint)

        // Draw separator
        val dividerPaint = Paint().apply {
            color = Color.argb(80, 0x24, 0x24, 0x21)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        baselineY += ruleSpacing * 0.4f
        canvas.drawLine(40f, baselineY, width - 40f, baselineY, dividerPaint)

        // Draw Items
        val checkboxSize = 40f
        val checkStrokePaint = Paint().apply {
            color = inkColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val checkFillPaint = Paint().apply {
            color = checkGreenColor
            strokeWidth = 4.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            typeface = primaryFont
            color = inkColor
            isAntiAlias = true
            textSize = 42f
            textAlign = Paint.Align.LEFT
        }

        val numberPaint = Paint().apply {
            typeface = primaryFont
            isAntiAlias = true
            textSize = 42f
            textAlign = Paint.Align.CENTER
        }

        items.forEachIndexed { index, item ->
            baselineY += ruleSpacing

            // 1. Draw number on the left
            val dotColor = rowDotColors[index % rowDotColors.size]
            numberPaint.color = dotColor
            canvas.drawText("${index + 1}", 75f, baselineY, numberPaint)

            // 2. Draw Checkbox on far right
            val boxX = width - 110f
            val boxY = baselineY - checkboxSize + 6f
            val rect = RectF(boxX, boxY, boxX + checkboxSize, boxY + checkboxSize)

            // Checkbox stroke (no colored background fill)
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

            // 3. Draw text starting right next to line numbers (NO red line)
            val itemTextX = 125f
            val itemPaint = Paint(textPaint).apply {
                color = if (item.isChecked) mutedInkColor else inkColor
                textAlign = Paint.Align.LEFT
            }
            val maxTextWidth = boxX - itemTextX - 25f
            val displayText = if (itemPaint.measureText(item.text) > maxTextWidth) {
                var truncated = item.text
                while (truncated.isNotEmpty() && itemPaint.measureText("$truncated...") > maxTextWidth) {
                    truncated = truncated.dropLast(1)
                }
                "$truncated..."
            } else {
                item.text
            }

            canvas.drawText(displayText, itemTextX, baselineY, itemPaint)

            // Soft pencil black strikethrough line ONLY across the text width
            if (item.isChecked) {
                val textW = itemPaint.measureText(displayText)
                val strikePaint = Paint().apply {
                    color = Color.argb(90, 0x24, 0x24, 0x21)
                    strokeWidth = 2.5f
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                val strikeY = baselineY - 14f
                canvas.drawLine(itemTextX - 4f, strikeY, itemTextX + textW + 4f, strikeY, strikePaint)
            }
        }

        // Footer brand
        baselineY += ruleSpacing * 1.5f
        val footerPaint = Paint().apply {
            typeface = primaryFont
            color = mutedInkColor
            isAntiAlias = true
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Sarf • كناش الحسابات المغربي", width / 2f, baselineY, footerPaint)

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
