package com.cash.guide.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityRepositoryTest {

    @Test
    fun hashPin_isDeterministic() {
        val salt = "abcd1234efgh5678"
        val hash1 = SecurityRepository.hashPin("1234", salt)
        val hash2 = SecurityRepository.hashPin("1234", salt)
        assertEquals(hash1, hash2)
    }

    @Test
    fun hashPin_differentPins_produceDifferentHashes() {
        val salt = "abcd1234efgh5678"
        val hash1 = SecurityRepository.hashPin("1234", salt)
        val hash2 = SecurityRepository.hashPin("5678", salt)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun hashPin_differentSalts_produceDifferentHashes() {
        val hash1 = SecurityRepository.hashPin("1234", "saltA")
        val hash2 = SecurityRepository.hashPin("1234", "saltB")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun generateSalt_producesRandomNonEmptyStrings() {
        val salt1 = SecurityRepository.generateSalt()
        val salt2 = SecurityRepository.generateSalt()
        assertTrue(salt1.isNotEmpty())
        assertEquals(32, salt1.length) // 16 bytes = 32 hex characters
        assertNotEquals(salt1, salt2)
        // Verify hex characters only
        assertTrue(salt1.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun hashPin_handlesLeadingZeros() {
        val salt = "test_salt"
        val hash1 = SecurityRepository.hashPin("0000", salt)
        val hash2 = SecurityRepository.hashPin("0001", salt)
        assertEquals(64, hash1.length) // SHA-256 is 256 bits = 64 hex characters
        assertEquals(64, hash2.length)
        assertNotEquals(hash1, hash2)
    }
}
