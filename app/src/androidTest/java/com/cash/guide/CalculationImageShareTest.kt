package com.cash.guide

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.domain.CalculationImageShareHelper
import com.cash.guide.domain.MoneyUnit
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CalculationImageShareTest {

    @Test
    fun testRenderLongCalculationArabic() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Generate 25 items to test continuous long receipt capture
        val items = (1..25).map { i ->
            CalculationItemEntity(
                id = UUID.randomUUID().toString(),
                calculationId = "test-long-ar",
                label = when (i % 5) {
                    1 -> "كيلو سكر سنيدة"
                    2 -> "زيت لوسيور 5 لتر"
                    3 -> "خنشة دقيق 25 كلغ"
                    4 -> "علبة شاي الصويري"
                    else -> "صابون الكف حبة $i"
                },
                amountCentimes = (i * 1500L), // 15.00 DH to 375.00 DH
                position = i - 1,
                createdAtEpochMs = System.currentTimeMillis(),
                updatedAtEpochMs = System.currentTimeMillis()
            )
        }
        val totalCentimes = items.sumOf { it.amountCentimes }

        val bitmap = CalculationImageShareHelper.renderLongReceiptBitmap(
            context = context,
            title = "سلعة المحل الأسبوعية",
            currency = MoneyUnit.DIRHAM,
            items = items,
            totalCentimes = totalCentimes,
            groupName = "مصاريف المحل",
            createdAtEpochMs = System.currentTimeMillis(),
            isRtl = true
        )

        assertNotNull(bitmap)
        assertTrue(bitmap.width == 1080)
        // With 25 items + header + total + footer, height should be > 2000px
        assertTrue(bitmap.height > 2000)

        // Save to cache / sdcard for visual inspection
        val outputFile = File(context.cacheDir, "test_long_calc_arabic.png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        assertTrue(outputFile.exists() && outputFile.length() > 0)
    }

    @Test
    fun testRenderLongCalculationFrench() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Generate 15 items
        val items = (1..15).map { i ->
            CalculationItemEntity(
                id = UUID.randomUUID().toString(),
                calculationId = "test-long-fr",
                label = when (i % 4) {
                    1 -> "Pack d'eau minérale"
                    2 -> "Café moulu 250g"
                    3 -> "Farine blanche 5kg"
                    else -> "Papier essuie-tout $i"
                },
                amountCentimes = (i * 2250L),
                position = i - 1,
                createdAtEpochMs = System.currentTimeMillis(),
                updatedAtEpochMs = System.currentTimeMillis()
            )
        }
        val totalCentimes = items.sumOf { it.amountCentimes }

        val bitmap = CalculationImageShareHelper.renderLongReceiptBitmap(
            context = context,
            title = "Achats Supermarché",
            currency = MoneyUnit.DIRHAM,
            items = items,
            totalCentimes = totalCentimes,
            groupName = "Maison",
            createdAtEpochMs = System.currentTimeMillis(),
            isRtl = false
        )

        assertNotNull(bitmap)
        assertTrue(bitmap.width == 1080)
        assertTrue(bitmap.height > 1500)

        val outputFile = File(context.cacheDir, "test_long_calc_french.png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        assertTrue(outputFile.exists() && outputFile.length() > 0)
    }
}
