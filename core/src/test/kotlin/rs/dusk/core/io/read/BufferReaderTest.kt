package rs.dusk.core.io.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import java.nio.ByteBuffer

internal class BufferReaderTest {

    private lateinit var buffer: BufferReader

    private fun packet(vararg bytes: Int) {
        buffer = BufferReader(buffer = ByteBuffer.wrap(bytes.map { it.toByte() }.toByteArray()))
    }

    @Test
    fun `Read byte`() {
        //Given
        packet(2, -2)
        //Then
        assertByte(2)
        assertByte(-2)
    }

    @Test
    fun `Read byte add`() {
        //Given
        packet(-126, 126)
        //Then
        assertByte(2, Modifier.ADD)
        assertByte(-2, Modifier.ADD)
    }

    @Test
    fun `Read byte inverse`() {
        //Given
        packet(-2, 2)
        //Then
        assertByte(2, Modifier.INVERSE)
        assertByte(-2, Modifier.INVERSE)
    }

    @Test
    fun `Read byte subtract`() {
        //Given
        packet(126, -126)
        //Then
        assertByte(2, Modifier.SUBTRACT)
        assertByte(-2, Modifier.SUBTRACT)
    }

    @Test
    fun `Read short`() {
        //Given
        packet(0, 2, -1, -2)
        //Then
        assertShort(2)
        assertShort(-2)
    }

    @Test
    fun `Read short add`() {
        //Given
        packet(0, -126, -1, 126)
        //Then
        assertShort(2, Modifier.ADD)
        assertShort(-2, Modifier.ADD)
    }

    @Test
    fun `Read short little endian`() {
        //Given
        packet(2, 0, -2, -1)
        //Then
        assertShort(2, endian = Endian.LITTLE)
        assertShort(-2, endian = Endian.LITTLE)
    }

    @Test
    fun `Read short little endian add`() {
        //Given
        packet(-126, 0, 126, -1)
        //Then
        assertShort(2, Modifier.ADD, Endian.LITTLE)
        assertShort(-2, Modifier.ADD, Endian.LITTLE)
    }

    @Test
    fun `Read int`() {
        //Given
        packet(0, 0, 0, 2, -1, -1, -1, -2)
        //Then
        assertInt(2)
        assertInt(-2)
    }

    @Test
    fun `Read int middle endian inverse`() {
        //Given
        packet(0, 0, 2, 0, -1, -1, -2, -1)
        //Then
        assertInt(2, Modifier.INVERSE, Endian.MIDDLE)
        assertInt(-2, Modifier.INVERSE, Endian.MIDDLE)
    }

    @Test
    fun `Read int little endian`() {
        //Given
        packet(2, 0, 0, 0, -2, -1, -1, -1)
        //Then
        assertInt(2, endian = Endian.LITTLE)
        assertInt(-2, endian = Endian.LITTLE)
    }

    @Test
    fun `Read int inverse middle endian`() {
        //Given
        packet(0, 2, 0, 0, -1, -2, -1, -1)
        //Then
        assertInt(2, endian = Endian.MIDDLE)
        assertInt(-2, endian = Endian.MIDDLE)
    }

    @Test
    fun `Read string`() {
        //Given
        packet(49, 0)
        //Then
        assertEquals("1", buffer.readString())
    }

    @Test
    fun `Read medium`() {
        //Given
        packet(0, 0, 2, -1, -1, -2, -1, 0, -1)
        //Then
        assertEquals(2, buffer.readMedium())
        assertEquals(-2, buffer.readMedium())
        assertEquals(-65281, buffer.readMedium())
    }

    @Test
    fun `Read unsigned medium`() {
        //Given
        packet(-1, 0, -1)
        //Then
        assertEquals(16711935, buffer.readUnsignedMedium())
    }

    @Test
    fun `Read smart`() {
        //Given
        packet(1, -127, -12)
        //Then
        assertEquals(1, buffer.readSmart())
        assertEquals(500, buffer.readSmart())
    }

    @Test
    fun `Read long`() {
        //Given
        packet(0, 0, 0, 0, 0, 0, 0, 2, -1, -1, -1, -1, -1, -1, -1, -2)
        //Then
        assertEquals(2, buffer.readLong())
        assertEquals(-2, buffer.readLong())
    }


    @Test
    fun `Read bit access`() {
        //Given
        packet(-64)
        //Then
        buffer.startBitAccess()
        assertEquals(1, buffer.readBits(1))
        assertEquals(1, buffer.readBits(1))
        assertEquals(0, buffer.readBits(2))
    }

    @Test
    fun `Read longer bit access`() {
        //Given
        packet(-1, -128)
        //Then
        buffer.startBitAccess()
        assertEquals(511, buffer.readBits(9))
    }

    private fun assertByte(value: Int, type: Modifier = Modifier.NONE) {
        assertEquals(value, buffer.readByte(type))
    }

    private fun assertShort(value: Int, type: Modifier = Modifier.NONE, endian: Endian = Endian.BIG) {
        assertEquals(value, buffer.readShort(type, endian))
    }

    private fun assertInt(value: Int, type: Modifier = Modifier.NONE, endian: Endian = Endian.BIG) {
        assertEquals(value, buffer.readInt(type, endian))
    }
}