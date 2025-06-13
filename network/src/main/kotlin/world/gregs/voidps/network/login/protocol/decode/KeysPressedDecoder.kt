package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * key's pressed - Pair<Key, Time>
 */
class KeysPressedDecoder : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.remaining > 0) {
            keys.add(packet.readUByte().toInt() to packet.readUShort().toInt())
        }
        return null
    }
}
