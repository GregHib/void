package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * Player has changed their online status while in the lobby
 */
class LobbyOnlineStatusDecoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val first = packet.readByte()
        val status = packet.readByte()
        val second = packet.readByte()
        return null
    }
}
