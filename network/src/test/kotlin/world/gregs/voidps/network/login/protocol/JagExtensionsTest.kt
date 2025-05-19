package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JagExtensionsTest {

    @Test
    fun `Read unsigned byte`() = runTest {
        val data = byteArrayOf(0xFF.toByte())
        val channel = ByteReadChannel(data)
        val result = channel.readUByte()
        assertEquals(255, result)
    }

    @Test
    fun `Read unsigned short`() = runTest {
        val data = byteArrayOf(0x12, 0x34)
        val channel = ByteReadChannel(data)
        val result = channel.readUShort()
        assertEquals(0x1234, result)
    }

    @Test
    fun `Read medium`() = runTest {
        val data = byteArrayOf(0x01, 0x02, 0x03)
        val channel = ByteReadChannel(data)
        val result = channel.readMedium()
        assertEquals(0x010203, result)
    }

    @Test
    fun `Read unsigned medium`() = runTest {
        val data = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0xFD.toByte())
        val channel = ByteReadChannel(data)
        val result = channel.readUMedium()
        assertEquals(0xFFFEFD, result)
    }

    @Test
    fun `Read and write boolean`() = runTest {
        val channel = ByteChannel(autoFlush = true)
        channel.writeByte(true)
        channel.writeByte(false)
        channel.close()
        val packet = ByteReadPacket(channel.readRemaining().readBytes())
        assertTrue(packet.readBoolean())
        assertFalse(packet.readBoolean())
    }

    @Test
    fun `Read and write byte add`() = runTest {
        val channel = ByteChannel(autoFlush = true)
        channel.writeByteAdd(10)
        channel.close()
        val packet = ByteReadPacket(channel.readRemaining().readBytes())
        assertEquals(10, packet.readByteAdd())
    }

    @Test
    fun `Read and write string`() = runTest {
        val channel = ByteChannel(autoFlush = true)
        val text = "Hello"
        channel.writeString(text)
        channel.close()
        val packet = ByteReadPacket(channel.readRemaining().readBytes())
        assertEquals(text, packet.readString())
    }

    @Test
    fun `Read byte inverse`() {
        val data = byteArrayOf(0x01)
        val packet = ByteReadPacket(data)
        assertEquals(-1, packet.readByteInverse())
    }

    @Test
    fun `Read smart short`() {
        val data = byteArrayOf(0x7F)
        val packet = ByteReadPacket(data)
        assertEquals(127, packet.readSmart())
    }

    @Test
    fun `Read smart int`() {
        val data = byteArrayOf(0x80.toByte(), 0x00)
        val packet = ByteReadPacket(data)
        assertEquals(0, packet.readSmart())
    }

    @Test
    fun `Read and write short add little`() = runTest {
        val channel = ByteChannel(autoFlush = true)
        val value = 0x1234
        channel.writeShortAddLittle(value)
        channel.close()
        val array = channel.readRemaining().readBytes()
        val packet = ByteReadPacket(array)
        assertEquals(value, packet.readShortAddLittle())
    }

    @Test
    fun `Read signed short add little`() = runTest {
        val channel = ByteChannel(autoFlush = true)
        val value = -1
        channel.writeShortAddLittle(value)
        channel.close()
        val array = channel.readRemaining().readBytes()
        val packet = ByteReadPacket(array)
        assertEquals(value, packet.readShortAddLittle())
    }
}