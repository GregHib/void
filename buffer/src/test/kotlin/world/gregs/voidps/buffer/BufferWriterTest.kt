package world.gregs.voidps.buffer

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter

internal class BufferWriterTest {

    private lateinit var buffer: BufferWriter
    private var reader: BufferReader? = null

    @BeforeEach
    fun setup() {
        buffer = BufferWriter()
    }
    
    @Test
    fun skip() {
        // When
        buffer.skip(4)
        buffer.writeByte(1)
        // Then
        for(i in 0 until 4) {
            assertByte(0)
        }
        assertByte(1)
    }

    @Test
    fun `Write bytes position`() {
        // When
        buffer.writeBytes(byteArrayOf(3, 2, 1), 2, 1)
        // Then
        assertByte(1)
    }

    @Test
    fun `Correct position`() {
        // When
        buffer.skip(5)
        // Then
        assertEquals(5, buffer.position())
    }

    @Test
    fun `Write byte`() {
        // When
        buffer.writeByte(2)
        buffer.writeByte(-2)
        // Then
        assertBytes(2, -2)
    }

    @Test
    fun `Write byte add`() {
        // When
        buffer.writeByteAdd(2)
        buffer.writeByteAdd(-2)
        // Then
        assertBytes(-126, 126)
    }

    @Test
    fun `Write byte inverse`() {
        // When
        buffer.writeByteInverse(2)
        buffer.writeByteInverse(-2)
        // Then
        assertBytes(-2, 2)
    }

    @Test
    fun `Write byte subtract`() {
        // When
        buffer.writeByteSubtract(2)
        buffer.writeByteSubtract(-2)
        // Then
        assertBytes(126, -126)
    }

    @Test
    fun `Write short`() {
        // When
        buffer.writeShort(2)
        buffer.writeShort(-2)
        // Then
        assertBytes(0, 2, -1, -2)
    }

    @Test
    fun `Write short add`() {
        // When
        buffer.writeShortAdd(2)
        buffer.writeShortAdd(-2)
        // Then
        assertBytes(0, -126, -1, 126)
    }

    @Test
    fun `Write short little endian`() {
        // When
        buffer.writeShortLittle(2)
        buffer.writeShortLittle(-2)
        // Then
        assertBytes(2, 0, -2, -1)
    }

    @Test
    fun `Write short little endian add`() {
        // When
        buffer.writeShortAddLittle(2)
        buffer.writeShortAddLittle(-2)
        // Then
        assertBytes(-126, 0, 126, -1)
    }

    @Test
    fun `Write int`() {
        // When
        buffer.writeInt(2)
        buffer.writeInt(-2)
        // Then
        assertBytes(0, 0, 0, 2, -1, -1, -1, -2)
    }

    @Test
    fun `Write int middle endian`() {
        // When
        buffer.writeIntMiddle(2)
        buffer.writeIntMiddle(-2)
        // Then
        assertBytes(0, 2, 0, 0, -1, -2, -1, -1)
    }

    @Test
    fun `Write int inverse`() {
        // When
        buffer.writeIntInverse(2)
        buffer.writeIntInverse(-2)
        // Then
        assertBytes(0, 0, 0, -2, -1, -1, -1, 2)
    }

    @Test
    fun `Write int middle endian inverse`() {
        // When
        buffer.writeIntInverseMiddle(2)
        buffer.writeIntInverseMiddle(-2)
        // Then
        assertBytes(0, 0, 2, 0, -1, -1, -2, -1)
    }

    @Test
    fun `Write int little endian`() {
        // When
        buffer.writeIntLittle(2)
        buffer.writeIntLittle(-2)
        // Then
        assertBytes(2, 0, 0, 0, -2, -1, -1, -1)
    }

    @Test
    fun `Write int little endian inverse`() {
        // When
        buffer.writeIntInverseLittle(2)
        buffer.writeIntInverseLittle(-2)
        // Then
        assertBytes(-2, 0, 0, 0, 2, -1, -1, -1)
    }

    @Test
    fun `Write string`() {
        // When
        buffer.writeString("1")
        // Then
        assertBytes(49, 0)
    }

    @Test
    fun `Write long`() {
        // When
        buffer.writeLong(2)
        buffer.writeLong(-2)
        // Then
        assertBytes(0, 0, 0, 0, 0, 0, 0, 2, -1, -1, -1, -1, -1, -1, -1 , -2)
    }

    @Test
    fun `Write medium`() {
        // When
        buffer.writeMedium(2)
        buffer.writeMedium(-2)
        // Then
        assertBytes(0, 0, 2, -1, -1, -2)
    }

    @Test
    fun `Write smart`() {
        // When
        buffer.writeSmart(1)
        buffer.writeSmart(500)
        // Then
        assertBytes(1, -127, -12)
    }

    @Test
    fun `Write bit access`() {
        // When
        buffer.startBitAccess()
        buffer.writeBits(1, 1)
        buffer.writeBits(1, 1)
        buffer.writeBits(2, 0)
        buffer.finishBitAccess()

        // Then
        assertByte(-64)
    }

    @Test
    fun `Write exactly one byte bit access`() {
        // When
        buffer.startBitAccess()
        buffer.writeBits(8, 255)
        buffer.finishBitAccess()
        // Then
        assertByte(-1)
    }

    @Test
    fun `Write offset bit access`() {
        // When
        buffer.startBitAccess()
        buffer.writeBits(9, 511)
        buffer.finishBitAccess()
        // Then
        assertByte(-1)
        assertByte(-128)
    }

    private fun assertByte(value: Int) {
        if(reader == null) {
            reader = BufferReader(buffer.toArray())
        }
        assertEquals(value, reader!!.readByte())
    }

    private fun assertBytes(vararg bytes: Int) {
        assertArrayEquals(bytes.map { it.toByte() }.toByteArray(), buffer.toArray())
    }
}