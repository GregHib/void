package world.gregs.voidps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.Unicode
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter

class UnicodeTest {

    @Test
    fun `Encode byte to char and back`() {
        for (byte in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            if (byte == 0) {
                continue
            }
            val original = byte.toByte()
            val char = Unicode.byteToChar(original.toInt()).toChar()
            val roundTrip = try {
                Unicode.charToByte(char)
            } catch (e: IllegalArgumentException) {
                null
            }

            if (char == '?') {
                continue
            }

            assertEquals(original.toInt(), roundTrip, "Round-trip failed for byte: $original (char: $char)")
        }
    }

    @Test
    fun `Read and write non-utf8 chars`() {
        val array = ByteArray(64)
        val writer = ArrayWriter(buffer = array)
        val reader = ArrayReader(array)
        for (char in Unicode.table) {
            if (char.code == 0) {
                continue
            }
            writer.writeChar(char)
            val roundTrip = try {
                reader.readChar()
            } catch (e: IllegalArgumentException) {
                null
            }

            if (roundTrip == 64) {
                continue
            }

            assertEquals(char, roundTrip?.toChar(), "Round-trip failed for char: ${char.code}")
        }
    }
}