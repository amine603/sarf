package com.cash.guide

import com.cash.guide.data.db.ChecklistItemEntity
import com.cash.guide.domain.ChecklistLinkHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistLinkHelperTest {

    @Test
    fun createAndParseDeepLinkWithArabicItems() {
        val items = listOf(
            ChecklistItemEntity(
                id = "1",
                checklistId = "cl-1",
                text = "مطيشة",
                isChecked = false,
                position = 0,
                createdAtEpochMs = 1000L
            ),
            ChecklistItemEntity(
                id = "2",
                checklistId = "cl-1",
                text = "بطاطا",
                isChecked = true,
                position = 1,
                createdAtEpochMs = 1001L
            ),
            ChecklistItemEntity(
                id = "3",
                checklistId = "cl-1",
                text = "حليب",
                isChecked = false,
                position = 2,
                createdAtEpochMs = 1002L
            )
        )

        val link = ChecklistLinkHelper.createDeepLink("تقضية الجمعة", items)
        assertTrue("Link should start with HTTPS base", link.startsWith("https://sarf.app/checklist?d="))

        val parsed = ChecklistLinkHelper.parseDeepLink(link)
        assertNotNull(parsed)
        assertEquals("تقضية الجمعة", parsed!!.title)
        assertEquals(3, parsed.items.size)
        assertEquals("مطيشة" to false, parsed.items[0])
        assertEquals("بطاطا" to true, parsed.items[1])
        assertEquals("حليب" to false, parsed.items[2])
    }

    @Test
    fun parseCustomSchemeLink() {
        val link = "sarf://checklist?t=Souk&i=Pain&i=Lait"
        val parsed = ChecklistLinkHelper.parseDeepLink(link)
        assertNotNull(parsed)
        assertEquals("Souk", parsed!!.title)
        assertEquals(2, parsed.items.size)
        assertEquals("Pain" to false, parsed.items[0])
        assertEquals("Lait" to false, parsed.items[1])
    }
}
