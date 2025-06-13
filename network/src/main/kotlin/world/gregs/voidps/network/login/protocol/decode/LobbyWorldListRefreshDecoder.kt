package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

/**
 * Client has requested the lobby world list be refreshed
 */
class LobbyWorldListRefreshDecoder : Decoder(4) {

    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val latency = packet.readInt()
        return null
    }
}
