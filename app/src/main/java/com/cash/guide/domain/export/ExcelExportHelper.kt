package com.cash.guide.domain.export

import android.content.Context
import com.cash.guide.R
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyUnit
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExportHelper {

    private const val UTF8_BOM = "\uFEFF"

    /**
     * Exports a single calculation to a .csv file with UTF-8 BOM for Microsoft Excel.
     */
    fun exportSingleCalculation(
        context: Context,
        calculationWithItems: CalculationWithItems,
        groupName: String? = null
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val calc = calculationWithItems.calculation
        val currency = runCatching { MoneyUnit.valueOf(calc.currency) }.getOrDefault(MoneyUnit.DIRHAM)
        val currencyStr = if (currency == MoneyUnit.DIRHAM) {
            context.getString(R.string.currency_dirham)
        } else {
            context.getString(R.string.currency_rial)
        }

        val dateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanTitle = calc.title.trim().replace(Regex("[^a-zA-Z0-9\\p{IsArabic}]"), "_").take(25)
            .ifBlank { "calculation" }
        val file = File(exportDir, "hssabi_${cleanTitle}_${dateSuffix}.csv")

        val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(calc.createdAtEpochMs))
        val totalFormatted = MoneyMath.fromCentimes(calculationWithItems.totalCentimes, currency)

        OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { writer ->
            writer.write(UTF8_BOM)

            // Metadata summary block
            writer.write("${escape(context.getString(R.string.export_col_calc_title))},${escape(calc.title.ifBlank { context.getString(R.string.editor_new_title) })}\n")
            writer.write("${escape(context.getString(R.string.export_col_date))},${escape(dateFormatted)}\n")
            if (!groupName.isNullOrBlank()) {
                writer.write("${escape(context.getString(R.string.export_col_group))},${escape(groupName)}\n")
            }
            writer.write("${escape(context.getString(R.string.export_col_currency))},${escape(currencyStr)}\n")
            writer.write("${escape(context.getString(R.string.export_col_calc_total))},${escape(totalFormatted)}\n")
            if (!calc.note.isNullOrBlank()) {
                writer.write("${escape(context.getString(R.string.export_col_note))},${escape(calc.note)}\n")
            }
            writer.write("\n")

            // Items table header
            val headers = listOf(
                context.getString(R.string.export_col_number),
                context.getString(R.string.export_col_item),
                context.getString(R.string.export_col_expression),
                context.getString(R.string.export_col_amount),
                context.getString(R.string.export_col_currency)
            )
            writer.write(headers.joinToString(",") { escape(it) } + "\n")

            // Item rows
            val items = calculationWithItems.items.sortedBy { it.position }
            items.forEachIndexed { index, item ->
                val amountStr = MoneyMath.fromCentimes(item.amountCentimes, currency)
                val row = listOf(
                    (index + 1).toString(),
                    item.label.ifBlank { "${context.getString(R.string.share_article_prefix)} ${index + 1}" },
                    item.rawExpression ?: "",
                    amountStr,
                    currencyStr
                )
                writer.write(row.joinToString(",") { escape(it) } + "\n")
            }

            // Total summary row
            writer.write("\n")
            val totalRow = listOf(
                "",
                context.getString(R.string.share_total_label),
                "",
                totalFormatted,
                currencyStr
            )
            writer.write(totalRow.joinToString(",") { escape(it) } + "\n")
            writer.flush()
        }

        return file
    }

    /**
     * Exports all calculations into a comprehensive flat CSV sheet compatible with Excel, Sheets, etc.
     */
    fun exportAllCalculations(
        context: Context,
        calculations: List<CalculationWithItems>,
        groupMap: Map<String, String> = emptyMap()
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val dateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "hssabi_all_calculations_${dateSuffix}.csv")

        OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { writer ->
            writer.write(UTF8_BOM)

            // Comprehensive column headers
            val headers = listOf(
                context.getString(R.string.export_col_date),
                context.getString(R.string.export_col_group),
                context.getString(R.string.export_col_calc_title),
                context.getString(R.string.export_col_number),
                context.getString(R.string.export_col_item),
                context.getString(R.string.export_col_expression),
                context.getString(R.string.export_col_amount),
                context.getString(R.string.export_col_currency),
                context.getString(R.string.export_col_calc_total),
                context.getString(R.string.export_col_note)
            )
            writer.write(headers.joinToString(",") { escape(it) } + "\n")

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            calculations.forEach { calcWithItems ->
                val calc = calcWithItems.calculation
                val currency = runCatching { MoneyUnit.valueOf(calc.currency) }.getOrDefault(MoneyUnit.DIRHAM)
                val currencyStr = if (currency == MoneyUnit.DIRHAM) {
                    context.getString(R.string.currency_dirham)
                } else {
                    context.getString(R.string.currency_rial)
                }

                val dateStr = dateFormat.format(Date(calc.createdAtEpochMs))
                val groupName = calc.groupId?.let { groupMap[it] } ?: "-"
                val title = calc.title.ifBlank { context.getString(R.string.editor_new_title) }
                val totalStr = MoneyMath.fromCentimes(calcWithItems.totalCentimes, currency)
                val note = calc.note ?: ""

                val items = calcWithItems.items.sortedBy { it.position }
                if (items.isEmpty()) {
                    val emptyRow = listOf(
                        dateStr,
                        groupName,
                        title,
                        "-",
                        "-",
                        "",
                        "0",
                        currencyStr,
                        totalStr,
                        note
                    )
                    writer.write(emptyRow.joinToString(",") { escape(it) } + "\n")
                } else {
                    items.forEachIndexed { index, item ->
                        val amountStr = MoneyMath.fromCentimes(item.amountCentimes, currency)
                        val row = listOf(
                            dateStr,
                            groupName,
                            title,
                            (index + 1).toString(),
                            item.label.ifBlank { "${context.getString(R.string.share_article_prefix)} ${index + 1}" },
                            item.rawExpression ?: "",
                            amountStr,
                            currencyStr,
                            totalStr,
                            note
                        )
                        writer.write(row.joinToString(",") { escape(it) } + "\n")
                    }
                }
            }
            writer.flush()
        }

        return file
    }

    private fun escape(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (containsSpecial) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
