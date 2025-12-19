package world.gregs.voidps.buffer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.Reader

abstract class ReaderTest {

    lateinit var buffer: Reader

    abstract fun packet(vararg bytes: Int)

    @Test
    fun `Read byte`() {
        // Given
        packet(2, -2)
        // Then
        assertEquals(2, buffer.readByte())
        assertEquals(-2, buffer.readByte())
    }

    @Test
    fun `Read byte add`() {
        // Given
        packet(-126, 126)
        // Then
        assertEquals(2, buffer.readByteAdd())
        assertEquals(-2, buffer.readByteAdd())
    }

    @Test
    fun `Read byte inverse`() {
        // Given
        packet(-2, 2)
        // Then
        assertEquals(2, buffer.readByteInverse())
        assertEquals(-2, buffer.readByteInverse())
    }

    @Test
    fun `Read byte subtract`() {
        // Given
        packet(126, -126)
        // Then
        assertEquals(2, buffer.readByteSubtract())
        assertEquals(-2, buffer.readByteSubtract())
    }

    @Test
    fun `Read short`() {
        // Given
        packet(0, 2, -1, -2)
        // Then
        assertEquals(2, buffer.readShort())
        assertEquals(-2, buffer.readShort())
    }

    @Test
    fun `Read short add`() {
        // Given
        packet(0, -126, -1, 126)
        // Then
        assertEquals(2, buffer.readShortAdd())
        assertEquals(-2, buffer.readShortAdd())
    }

    @Test
    fun `Read short little endian`() {
        // Given
        packet(2, 0, -2, -1)
        // Then
        assertEquals(2, buffer.readShortLittle())
        assertEquals(-2, buffer.readShortLittle())
    }

    @Test
    fun `Read short little endian add`() {
        // Given
        packet(-126, 0, 126, -1)
        // Then
        assertEquals(2, buffer.readShortAddLittle())
        assertEquals(-2, buffer.readShortAddLittle())
    }

    @Test
    fun `Read unsigned short`() {
        // Given
        packet(0, 2, -1, -2)
        // Then
        assertEquals(2, buffer.readUnsignedShort())
        assertEquals(65534, buffer.readUnsignedShort())
    }

    @Test
    fun `Read unsigned short little`() {
        // Given
        packet(2, 0, -2, -1)
        // Then
        assertEquals(2, buffer.readUnsignedShortLittle())
        assertEquals(65534, buffer.readUnsignedShortLittle())
    }

    @Test
    fun `Read int`() {
        // Given
        packet(0, 0, 0, 2, -1, -1, -1, -2)
        // Then
        assertEquals(2, buffer.readInt())
        assertEquals(-2, buffer.readInt())
    }

    @Test
    fun `Read int middle endian inverse`() {
        // Given
        packet(0, 0, 2, 0, -1, -1, -2, -1)
        // Then
        assertEquals(2, buffer.readIntInverseMiddle())
        assertEquals(-2, buffer.readIntInverseMiddle())
    }

    @Test
    fun `Read int little endian`() {
        // Given
        packet(2, 0, 0, 0, -2, -1, -1, -1)
        // Then
        assertEquals(2, buffer.readIntLittle())
        assertEquals(-2, buffer.readIntLittle())
    }

    @Test
    fun `Read unsigned int middle endian`() {
        // Given
        packet(0, 2, 0, 0, 0, 0, 2, 0, -2, -1, -1, -1, -1, -1, -1, -2)
        // Then
        assertEquals(2, buffer.readUnsignedIntMiddle())
        assertEquals(33554432, buffer.readUnsignedIntMiddle())
        assertEquals(-257, buffer.readUnsignedIntMiddle())
        assertEquals(-65537, buffer.readUnsignedIntMiddle())
    }

    @Test
    fun `Read string`() {
        // Given
        packet(49, 0)
        // Then
        assertEquals("1", buffer.readString())
    }

    @Test
    fun `Read medium`() {
        // Given
        packet(0, 0, 2, -1, -1, -2, -1, 0, -1)
        // Then
        assertEquals(2, buffer.readMedium())
        assertEquals(-2, buffer.readMedium())
        assertEquals(-65281, buffer.readMedium())
    }

    @Test
    fun `Read unsigned medium`() {
        // Given
        packet(-1, 0, -1)
        // Then
        assertEquals(16711935, buffer.readUnsignedMedium())
    }

    @Test
    fun `Read smart`() {
        // Given
        packet(1, -127, -12)
        // Then
        assertEquals(1, buffer.readSmart())
        assertEquals(500, buffer.readSmart())
    }

    @Test
    fun `Read big smart`() {
        // Given
        packet(-3, -1, -1, -1, 127, -1, 48, 57, 0, -68, 97, 78)
        // Then
        assertEquals(2113929215, buffer.readBigSmart())
        assertEquals(-1, buffer.readBigSmart())
        assertEquals(12345, buffer.readBigSmart())
    }

    @Test
    fun `Read long`() {
        // Given
        packet(0, 0, 0, 0, 0, 0, 0, 2, -1, -1, -1, -1, -1, -1, -1, -2)
        // Then
        assertEquals(2, buffer.readLong())
        assertEquals(-2, buffer.readLong())
    }

    @Test
    fun `Read bit access`() {
        // Given
        packet(-64)
        // Then
        buffer.startBitAccess()
        assertEquals(1, buffer.readBits(1))
        assertEquals(1, buffer.readBits(1))
        assertEquals(0, buffer.readBits(2))
    }

    @Test
    fun `Read longer bit access`() {
        // Given
        packet(-1, -128)
        // Then
        buffer.startBitAccess()
        assertEquals(511, buffer.readBits(9))
    }

    @Test
    fun `Read special char`() {
        packet(-32)
        // When
        val string = buffer.readString()
        // Then
        assertEquals("\u00E0", string)
    }
}
