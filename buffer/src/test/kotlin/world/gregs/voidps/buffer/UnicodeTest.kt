package world.gregs.voidps.buffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UnicodeTest {
    @Test
    fun `Encode byte to char and back`() {
        for (byte in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            if (byte == 0) {
                continue
            }
            val original = byte.toByte().toInt()
            val char = Unicode.byteToChar(original).toChar()
            val roundTrip = try {
                Unicode.charToByte(char)
            } catch (e: IllegalArgumentException) {
                null
            }

            if (char == '?') {
                continue
            }

            assertEquals(original, roundTrip, "Round-trip failed for byte: $original (char: $char)")
        }
    }

    @Test
    fun `Invalid characters throws exception`() {
        val impossibleChar = '\u4E00'
        assertThrows<IllegalArgumentException> {
            Unicode.charToByte(impossibleChar)
        }
    }

    @Test
    fun `Unmappable characters throws exception`() {
        val impossibleChar = 63.toChar()
        assertThrows<IllegalArgumentException> {
            Unicode.charToByte(impossibleChar)
        }
    }
}