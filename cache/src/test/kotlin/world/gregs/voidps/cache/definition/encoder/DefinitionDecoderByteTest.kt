package world.gregs.voidps.cache.definition.encoder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.cache.DefinitionDecoder.Companion.byteToChar
import world.gregs.voidps.cache.DefinitionDecoder.Companion.charToByte

class DefinitionDecoderByteTest {
    @Test
    fun `Encode byte to char and back`() {
        for (byte in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            if (byte == 0) {
                continue
            }
            val original = byte.toByte()
            val char = byteToChar(original)
            val roundTrip = try {
                charToByte(char)
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
            charToByte(impossibleChar)
        }
    }

    @Test
    fun `Unmappable characters throws exception`() {
        val impossibleChar = 63.toChar()
        assertThrows<IllegalArgumentException> {
            charToByte(impossibleChar)
        }
    }
}
