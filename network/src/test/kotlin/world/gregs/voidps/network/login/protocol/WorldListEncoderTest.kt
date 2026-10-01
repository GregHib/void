package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.IsaacCipher
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.protocol.encode.WorldFlag
import world.gregs.voidps.network.login.protocol.encode.sendWorldList

class WorldListEncoderTest {

    @Test
    fun `World list is read in the same order as the client`() = runTest {
        val channel = ByteChannel(true)
        val client = Client(channel, IsaacCipher(IntArray(4)), null, "")

        client.sendWorldList(world = 160, flags = WorldFlag.MEMBERS, activity = "Activity", address = "localhost", players = 12, location = "UK")

        val bytes = ByteArray(channel.availableForRead)
        channel.readAvailable(bytes)
        val packet = Buffer().apply { write(bytes) }
        assertEquals(Protocol.WORLD_LIST_FULL, packet.readUByte().toInt())
        assertEquals(packet.size.toInt() - 2, packet.readUShort().toInt())
        assertEquals(1, packet.readUByte().toInt()) // Complete
        assertEquals(2, packet.readUByte().toInt()) // Version
        assertEquals(1, packet.readUByte().toInt()) // Includes world info
        assertEquals(1, packet.readSmart()) // Location count
        assertEquals(0, packet.readSmart()) // Country
        assertEquals("UK", packet.readVersionedString())
        assertEquals(160, packet.readSmart()) // Lowest
        assertEquals(160, packet.readSmart()) // Highest
        assertEquals(1, packet.readSmart()) // Count
        assertEquals(0, packet.readSmart()) // Offset
        assertEquals(0, packet.readUByte().toInt()) // Location index
        assertEquals(WorldFlag.MEMBERS, packet.readInt())
        assertEquals("Activity", packet.readVersionedString())
        assertEquals("localhost", packet.readVersionedString())
        assertEquals(0, packet.readInt()) // Checksum
        assertEquals(0, packet.readSmart()) // Offset
        assertEquals(12, packet.readUShort().toInt()) // Players
        assertEquals(0L, packet.size)
    }

    private fun Buffer.readVersionedString(): String {
        assertEquals(0, readByte().toInt())
        return readString()
    }

    private fun Buffer.readInt(): Int = readByteArray(4).fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff) }
}
