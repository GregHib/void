package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * key's pressed - Pair<Key, Time>
 */
class KeysPressedDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction? {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.remaining > 0) {
            keys.add(packet.readUByte().toInt() to packet.readUShort().toInt())
        }
        return null
    }

}
