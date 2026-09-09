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

    fun shareAsWhatsAppTextAndLink(
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

        // Draw vertical red margin line
        val marginLinePaint = Paint().apply {
            color = Color.argb(40, 0xD4, 0x5A, 0x5A)
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        val redLineX = if (isRtl) width - marginX * 1.5f else marginX * 1.5f
        canvas.drawLine(redLineX, 0f, redLineX, height.toFloat(), marginLinePaint)

        val textPaint = Paint().apply {
            typeface = primaryFont
            color = inkColor
            isAntiAlias = true
            textSize = 42f
        }

        // Header
        var baselineY = (topPaddingRules + 1) * ruleSpacing - 18f
        val headerTitle = if (title.isBlank()) "Checklist" else "Checklist : $title"

        val titlePaint = Paint().apply {
            typeface = primaryFont
            color = inkColor
            isAntiAlias = true
            textSize = 52f
        }

        val contentStartX = if (isRtl) width - marginX * 2.2f else marginX * 2.2f
        titlePaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        canvas.drawText(headerTitle, contentStartX, baselineY, titlePaint)

        // Date and progress line
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
            textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        canvas.drawText(statusStr, contentStartX, baselineY, subPaint)

        // Draw separator
        val dividerPaint = Paint().apply {
            color = Color.argb(80, 0x24, 0x24, 0x21)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        baselineY += ruleSpacing * 0.4f
        canvas.drawLine(marginX * 2f, baselineY, width - marginX * 2f, baselineY, dividerPaint)

        // Draw Items
        val checkboxSize = 36f
        val checkStrokePaint = Paint().apply {
            color = inkColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val checkFillPaint = Paint().apply {
            color = checkGreenColor
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        items.forEach { item ->
            baselineY += ruleSpacing

            val boxX = if (isRtl) {
                contentStartX - checkboxSize
            } else {
                contentStartX
            }
            val boxY = baselineY - checkboxSize + 6f

            // Draw Checkbox
            val rect = RectF(boxX, boxY, boxX + checkboxSize, boxY + checkboxSize)
            canvas.drawRoundRect(rect, 8f, 8f, checkStrokePaint)

            if (item.isChecked) {
                // Draw checkmark inside
                val p1x = rect.left + checkboxSize * 0.22f
                val p1y = rect.top + checkboxSize * 0.52f
                val p2x = rect.left + checkboxSize * 0.45f
                val p2y = rect.top + checkboxSize * 0.78f
                val p3x = rect.left + checkboxSize * 0.82f
                val p3y = rect.top + checkboxSize * 0.25f

                canvas.drawLine(p1x, p1y, p2x, p2y, checkFillPaint)
                canvas.drawLine(p2x, p2y, p3x, p3y, checkFillPaint)
            }

            // Draw text
            val itemTextX = if (isRtl) {
                boxX - 22f
            } else {
                boxX + checkboxSize + 22f
            }

            val itemPaint = Paint(textPaint).apply {
                color = if (item.isChecked) mutedInkColor else inkColor
                isStrikeThruText = item.isChecked
                textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
            }

            canvas.drawText(item.text, itemTextX, baselineY, itemPaint)
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
