package rs.dusk.core.io.read

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.BufferWriter

internal class BufferWriterTest {

    private lateinit var buffer: BufferWriter

    @BeforeEach
    fun setup() {
        buffer = BufferWriter()
    }
    
    @Test
    fun skip() {
        // When
        skip(4)
        byte(1)
        // Then
        for(i in 0 until 4) {
            assertByte(0)
        }
        assertByte(1)
    }

    @Test
    fun `Write bytes position`() {
        // When
        bytes(byteArrayOf(3, 2, 1), 2, 1)
        // Then
        assertByte(1)
    }

    @Test
    fun `Correct position`() {
        // When
        skip(5)
        // Then
        assertEquals(5, buffer.position())
    }

    @Test
    fun `Write byte`() {
        // When
        byte(2)
        byte(-2)
        // Then
        assertBytes(2, -2)
    }

    @Test
    fun `Write byte add`() {
        // When
        byte(2, Modifier.ADD)
        byte(-2, Modifier.ADD)
        // Then
        assertBytes(-126, 126)
    }

    @Test
    fun `Write byte inverse`() {
        // When
        byte(2, Modifier.INVERSE)
        byte(-2, Modifier.INVERSE)
        // Then
        assertBytes(-2, 2)
    }

    @Test
    fun `Write byte subtract`() {
        // When
        byte(2, Modifier.SUBTRACT)
        byte(-2, Modifier.SUBTRACT)
        // Then
        assertBytes(126, -126)
    }

    @Test
    fun `Write short`() {
        // When
        short(2)
        short(-2)
        // Then
        assertBytes(0, 2, -1, -2)
    }

    @Test
    fun `Write short add`() {
        // When
        short(2, Modifier.ADD)
        short(-2, Modifier.ADD)
        // Then
        assertBytes(0, -126, -1, 126)
    }

    @Test
    fun `Write short little endian`() {
        // When
        short(2, endian = Endian.LITTLE)
        short(-2, endian = Endian.LITTLE)
        // Then
        assertBytes(2, 0, -2, -1)
    }

    @Test
    fun `Write short little endian add`() {
        // When
        short(2, Modifier.ADD, Endian.LITTLE)
        short(-2, Modifier.ADD, Endian.LITTLE)
        // Then
        assertBytes(-126, 0, 126, -1)
    }

    @Test
    fun `Write int`() {
        // When
        int(2)
        int(-2)
        // Then
        assertBytes(0, 0, 0, 2, -1, -1, -1, -2)
    }

    @Test
    fun `Write int middle endian`() {
        // When
        int(2, endian = Endian.MIDDLE)
        int(-2, endian = Endian.MIDDLE)
        // Then
        assertBytes(0, 2, 0, 0, -1, -2, -1, -1)
    }

    @Test
    fun `Write int middle endian inverse`() {
        // When
        int(2, Modifier.INVERSE, Endian.MIDDLE)
        int(-2, Modifier.INVERSE, Endian.MIDDLE)
        // Then
        assertBytes(0, 0, 2, 0, -1, -1, -2, -1)
    }

    @Test
    fun `Write int little endian`() {
        // When
        int(2, endian = Endian.LITTLE)
        int(-2, endian = Endian.LITTLE)
        // Then
        assertBytes(2, 0, 0, 0, -2, -1, -1, -1)
    }

    @Test
    fun `Write int little endian inverse`() {
        // When
        buffer.writeInt(2, Modifier.INVERSE, Endian.LITTLE)
        buffer.writeInt(-2, Modifier.INVERSE, Endian.LITTLE)
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
        start()
        bits(1, 1)
        bits(1, 1)
        bits(2, 0)
        finish()

        // Then
        assertByte(-64)
    }

    @Test
    fun `Write exactly one byte bit access`() {
        // When
        start()
        bits(8, 255)
        finish()
        // Then
        assertByte(-1)
    }

    @Test
    fun `Write offset bit access`() {
        // When
        start()
        bits(9, 511)
        finish()
        // Then
        assertByte(-1)
        assertByte(-128)
    }

    private fun start() {
        buffer.startBitAccess()
    }

    private fun finish() {
        buffer.finishBitAccess()
    }

    private fun skip(count: Int) {
        buffer.skip(count)
    }

    private fun bits(count: Int, value: Int) {
        buffer.writeBits(count, value)
    }

    private fun bytes(array: ByteArray, offset: Int, length: Int) {
        buffer.writeBytes(array, offset, length)
    }

    private fun byte(value: Int, type: Modifier = Modifier.NONE) {
        buffer.writeByte(value, type)
    }

    private fun byte(value: Int) {
        buffer.writeByte(value)
    }

    private fun short(value: Int, type: Modifier = Modifier.NONE, endian: Endian = Endian.BIG) {
        buffer.writeShort(value, type, endian)
    }

    private fun int(value: Int, type: Modifier = Modifier.NONE, endian: Endian = Endian.BIG) {
        buffer.writeInt(value, type, endian)
    }

    private fun short(value: Int) {
        buffer.writeShort(value)
    }

    private fun assertByte(value: Int) {
        assertEquals(value, buffer.buffer.readByte().toInt())
    }

    private fun assertBytes(vararg bytes: Int) {
        assertArrayEquals(bytes.map { it.toByte() }.toByteArray(), buffer.toArray())
    }
}